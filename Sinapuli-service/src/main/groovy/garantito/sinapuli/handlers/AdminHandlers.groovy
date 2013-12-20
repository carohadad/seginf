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

import com.google.inject.*

class AdminHandlers extends GroovyHandler {
  @Inject
  ProjectRepository repoProject

  @Inject
  OffererRepository repoOfferer

  @Inject
  TenderOfferRepository repoOffers

  def projectListWithCounts() {
    def projects = repoProject.list()
    projects.each { project ->
      if (!project.pending) {
        project.offerCount = repoOffers.countForProjectId(project.id)
      }
    }
  }

  def handlers = { ->
    get {
      render handlebarsTemplate("projects/index.html",
        buildModel(context, projectList: projectListWithCounts()))
    }

    prefix('offerers') {
      get {
        render handlebarsTemplate('offerers/index.html',
          buildModel(context, offerersList: repoOfferer.list()))
      }

      handler(":id") {
        def id = pathTokens.asInt('id')
        byMethod {
          get {
            println "getting Offerer with ID ${id}"
            def offerer = repoOfferer.get(id)

            render handlebarsTemplate("offerers/show.html", 
              buildModel(context, offerer: offerer, certificate: offerer.certificate))
          }
        }
      }

      get(':id/key') {
        def id = pathTokens.asInt('id')
        def offerer = repoOfferer.get(id)
        response.headers.add 'Content-disposition', "filename=offerer-${id}.pem"
        response.send 'application/x-pem-file', offerer.publicKey
      }

      post(":id/delete") {
        def id = pathTokens.asInt('id')
        println "Now deleting offerer with ID: ${id}"
        repoOfferer.delete(id)
        redirect "/offerers"
      }
    }

    prefix('projects') {
      handler('') {
        byMethod {
          get {
            render handlebarsTemplate("projects/index.html",
              buildModel(context, projectList: projectListWithCounts()))
          }

          post {
            def form = context.parse(form())
            def uploaded = form.file('file')
            def project

            try {
              project = new Project(
                name: form.name,
                description: form.description,
                startTenderDate: form.startTenderDate,
                endTenderDate: form.endTenderDate)
              project.tender = uploaded.bytes
              project.tenderContentType = uploaded.contentType
              project.tenderFilename = uploaded.fileName

              project = repoProject.create(project)

              println "Proyecto ${project.name} creado con id ${project.id}"
              redirect "/projects"

            } catch (ValidationException e) {
              render handlebarsTemplate("projects/new.html",
                buildModel(context, error: e.message, project: project))
            }
          }
        }
      }

      get("new") {
        render handlebarsTemplate("projects/new.html", buildModel(context, project: new Project()))
      }

      handler(':id') {
        byMethod {
          get {
            println "getting Project with ID " + pathTokens.id
            def project = repoProject.get(pathTokens.id.toInteger())
            project.offers = repoOffers.listForProjectId(project.id)

            render handlebarsTemplate("projects/show.html", buildModel(context, project: project))
          }
        }
      }

      get(':id/document') {
        println "getting document for Project with ID " + pathTokens.id
        def project = repoProject.get(pathTokens.id.toInteger())

        response.headers.add "Content-disposition", "filename=\"${project.tenderFilename}\""
        response.send project.tenderContentType, project.tender
      }

      post(':id/delete') {
        println "deleting Project with ID ${pathTokens.id}"
        repoProject.delete(pathTokens.id.toInteger())
        redirect '/projects'
      }
    }

    prefix('offers') {
      get(':id/document') {
        def offer = repoOffers.get(pathTokens.asInt('id'))
        if (offer.complete) {
          response.headers.add "Content-disposition", "filename=\"${offer.documentFilename}\""
          response.send offer.documentType, offer.document
        } else {
          response.status 400, "Falta el documento de la oferta"
          response.send "text/plain", "La oferta está incompleta"
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

