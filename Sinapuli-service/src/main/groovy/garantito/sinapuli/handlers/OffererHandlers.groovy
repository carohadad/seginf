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
        def project = repoProject.get(pathTokens.asInt('id'))
        render handlebarsTemplate("open-projects/show.html",
          buildModel(context, project: project))
      }

      get(':id/document') {
        def project = repoProject.get(pathTokens.asInt('id'))
        response.headers.add "Content-disposition", "filename=\"${project.tenderFilename}\""
        response.send project.tenderContentType, project.tender
      }

      handler(':id/offer') {
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

