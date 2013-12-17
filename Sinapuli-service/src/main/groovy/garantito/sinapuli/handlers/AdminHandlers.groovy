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

import java.security.MessageDigest
import java.text.SimpleDateFormat

import garantito.sinapuli.model.*
import static garantito.sinapuli.Util.*

class AdminHandlers extends GroovyHandler {
  def handlers = { ->
    get { ProjectRepository repoProject, OffererRepository repoOfferer ->
      render handlebarsTemplate("projects/index.html", 
        buildModel(context, projectList: repoProject.list()))
    }

    prefix('offerer') { OffererRepository repoOfferer ->
      get("edit/:id"){		  
        println "getting Offerer with ID " + pathTokens.id
        def offerer = repoOfferer.get((pathTokens.id).toInteger())
            
        render groovyTemplate("/offerer/edit.html", id: offerer.id, name: offerer.name) 
      }		

      get("delete/:id"){
        render groovyTemplate("/offerer/delete.html", id: pathTokens.id)
      }

      post ("update/:id") {
        def form = context.parse(form())

        // Update is a save with an id	
        repoOfferer.update(form.id.toInteger(), form.name)

        redirect "/" 
      }											

      post ("delete/:id") {
        println "Now deleting offerer with ID: ${pathTokens.id}"
        repoOfferer.delete(pathTokens.id.toInteger())

        redirect "/" 			
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
            def errorMessage
            def project = new Project(
              name: form.name, 
              description: form.description, 
              startTenderDate: form.startTenderDate, 
              endTenderDate: form.endTenderDate)
            project.tender = uploaded.bytes
            project.tenderContentType = uploaded.contentType

            if (project.endTenderDate < project.startTenderDate) {
              errorMessage = "Las fechas ingresadas son inválidas"
            } else {
              project = repoProject.create(project)

              println "Proyecto ${project.name} creado con id ${project.id}"
              redirect "/projects"
              return
            }
            render handlebarsTemplate("projects/new.html", buildModel(context, error: errorMessage, project: project))
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
            
        response.send project.tenderContentType, project.tender
      }

      get ("offerts/:id") {
        def project = repoProject.get((pathTokens.id).toInteger())
        render groovyTemplate("/project/offerts.html", project: project, offertsList:repoTenderOffer.listWithproject(project.id))
      }		

      get("delete/:id/:name"){
        render groovyTemplate("/project/delete.html", id: pathTokens.id, name: pathTokens.name)
      }

      post ("update/:id") {
        def form = context.parse(form())

        // Update is a save with an id	
        repoProject.update(form.id.toInteger(), form.name)

        redirect "/" 
      }											

      post ("delete") {
        def form = context.parse(form())
        println "Now deleting project with ID: ${pathTokens.id}"
        repoProject.delete(form.projectId.toInteger())
        redirect "/"						
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

