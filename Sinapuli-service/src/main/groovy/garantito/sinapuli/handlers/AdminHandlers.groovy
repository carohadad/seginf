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

class AdminHandlers extends GroovyHandler {
  def handlers = { ->
    get { ProjectRepository repoProject ->
      render handlebarsTemplate("projects/index.html",
        buildModel(context, projectList: repoProject.list()))
    }

    prefix('offerers') { OffererRepository repoOfferer ->
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

    prefix('projects') { ProjectRepository repoProject ->
      handler('') {
        byMethod {
          get {
            render handlebarsTemplate('projects/index.html',
              buildModel(context, projectList: repoProject.list()))
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

    prefix('tenderOffer') { TenderOfferRepository repoTenderOffer ->
      get("new/:id") {
        println "getting project with ID " + pathTokens.id
        def project = repoProject.get((pathTokens.id).toInteger())
            render groovyTemplate("/tenderOffer/new.html", project: project)
      }

      get("edit/:id") {
        println "getting Offerer with ID " + pathTokens.id
        def tenderOffer = repoTenderOffer.get((pathTokens.id).toInteger())

        render groovyTemplate("/tenderOffer/edit.html", id: tenderOffer.id, hash: tenderOffer.hash)
      }

      get("delete/:id") {
        render groovyTemplate("/tenderOffer/delete.html", id: pathTokens.id)
      }

      post ("submit") {
        def form = context.parse(form())

        def offerer = repoOfferer.create(form.email)
        println "CREADO offerer " + offerer.id
        def project = repoProject.get(form.idproject.toInteger())

        def tenderOffer = repoTenderOffer.create(form.hash, offerer, project)

        redirect "/"
      }

      post ("update/:id") {
        def form = context.parse(form())

        // Update is a save with an id
        repoTenderOffer.update(form.id.toInteger(), form.hash)

        redirect "/"
      }

      post ("delete/:id") {
        println "Now deleting trendeOffer with ID: ${pathTokens.id}"
        repoTenderOffer.delete(pathTokens.id.toInteger())

        redirect "/"
      }
    }
  }

  @Override
  protected void handle(GroovyContext context) {
    Handler handler = Groovy.chain(context.get(LaunchConfig), context, handlers)
    context.insert([handler] as Handler[])
  }
}

