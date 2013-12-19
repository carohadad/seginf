package garantito.sinapuli.handlers

import ratpack.groovy.Groovy
import static ratpack.groovy.Groovy.*
import static ratpack.form.Forms.form
import ratpack.session.Session
import ratpack.session.store.SessionStorage

import ratpack.groovy.handling.GroovyContext
import ratpack.groovy.handling.GroovyHandler
import ratpack.handling.Handler
import ratpack.launch.LaunchConfig

import static ratpack.handlebars.Template.handlebarsTemplate

import garantito.sinapuli.model.*
import garantito.sinapuli.ValidationException
import static garantito.sinapuli.Util.*


class OffererHandlers extends GroovyHandler {
  def handlers = {
    get { ProjectRepository repoProject ->
      render handlebarsTemplate("open-projects/index.html",
        buildModel(context, projectList: repoProject.listOpenOrPending()))
    }

    prefix('projects') { ProjectRepository repoProject ->
      get { 
        render handlebarsTemplate("open-projects/index.html",
          buildModel(context, projectList: repoProject.listOpenOrPending()))
      }

      get(':id') {
        def repoOffers = get(TenderOfferRepository)
        def session = get(SessionStorage)
        def project = repoProject.get(pathTokens.asInt('id'))
        def offerList = repoOffers.listForProjectAndOfferer(project.id, session.offererId)
        render handlebarsTemplate("open-projects/show.html",
          buildModel(context, project: project, offers: offerList))
      }

      get(':id/document') {
        def project = repoProject.get(pathTokens.asInt('id'))
        response.headers.add "Content-disposition", "filename=\"${project.tenderFilename}\""
        response.send project.tenderContentType, project.tender
      }

      handler(':id/offer') { OffererRepository repoOfferer, TenderOfferRepository repoOffers ->
        byMethod {
          get {
            def project = repoProject.get(pathTokens.asInt('id'))
            def model
            if (project.open) {
              model = buildModel(context, project: project)
            } else {
              model = buildModel(context, project: project, error: "La licitación no está abierta")
            }
            render handlebarsTemplate("open-projects/offer.html", model)
          }
          post {
            def session = get(SessionStorage)
            def offerer = repoOfferer.get(session.offererId)
            def project = repoProject.get(pathTokens.asInt('id'))
            if (!project.open) {
              def model = buildModel(context, project: project, error: "La licitación no está abierta")
              render handlebarsTemplate("open-projects/offer.html", model)
              return
            }

            def form = context.parse(form())
            def offer
            try {
              offer = new TenderOffer(
                hash: form.hash,
                hashSignature: form.hashSignature,
                project: project,
                offerer: offerer)

              offer = repoOffers.create(offer)

              redirect "/offers/${offer.id}"

            } catch (ValidationException e) {
              def model = buildModel(context, project: project, offer: offer, validationError: e.message)
              render handlebarsTemplate("open-projects/offer.html", model)
            }
          }
        }
      }
    }
    prefix('offers') { TenderOfferRepository repoOffers ->
      get { 
        def session = get(SessionStorage)
        def list = repoOffers.listForOffererId(session.offererId)
        render handlebarsTemplate("offers/index.html", buildModel(context, offerList: list))
      }

      handler(':id') {
        byMethod {
          get {
            def session = get(SessionStorage)
            def offer = repoOffers.get(pathTokens.asInt('id'))
            if (offer.offerer.id == session.offererId) {
              render handlebarsTemplate("offers/show.html", buildModel(context, offer: offer))
            } else {
              redirect "/projects/${offer.project.id}"
            }
          }
          post {
            def session = get(SessionStorage)
            def offer = repoOffers.get(pathTokens.asInt('id'))
            if (offer.offerer.id != session.offerer_id) {
              redirect "/projects/${offer.project.id}"
              return
            }
            if (offer.complete) {
              redirect "/offers/${offer.id}"
              return
            }
            if (!offer.project.closed || offer.complete) {
              def message
              if (offer.complete) {
                message = "La oferta ya fue completada"
              } else if (offer.project.open) {
                message = "La licitación aún no cerró"
              } else {
                message = "El límite de tiempo para completar la oferta expiró"
              }
              render handlebarsTemplate("offers/show.html", buildModel(context, offer: offer, error: message))
              return
            }

            def form = context.parse(form())
            def uploaded = form.file('document')

            try {
              offer.document = uploaded.bytes
              offer.documentType = uploaded.contentType
              offer.documentFilename = uploaded.fileName

              offer = repoOffers.update(offer)

              redirect "/offers/${offer.id}"

            } catch (ValidationException e) {
              render handlebarsTemplate("offers/show.html", buildModel(context, offer: offer, error: e.message))
            }
          }
        }
      }
    }
  }

  @Override
  protected void handle(GroovyContext context) {
    Handler handler = Groovy.chain(context.get(LaunchConfig), context, handlers)
    context.insert([handler] as Handler[])
  }
}

