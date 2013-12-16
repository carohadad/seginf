import static ratpack.groovy.Groovy.*
import static ratpack.form.Forms.form
import ratpack.session.Session
import ratpack.session.store.MapSessionsModule
import ratpack.session.store.SessionStorage
import ratpack.session.store.SessionStore
import ratpack.groovy.handling.GroovyContext

import ratpack.handlebars.HandlebarsModule
import ratpack.handlebars.NamedHelper
import static ratpack.handlebars.Template.handlebarsTemplate

import ratpack.h2.H2Module

import java.security.MessageDigest
import java.text.SimpleDateFormat

import garantito.sinapuli.*
import garantito.sinapuli.model.*
import garantito.sinapuli.handlers.*

def withAuthModel(Map model, session) {
  model.auth = [
    loggedIn: session.auth,
    username: session.username,
    role: session.role
  ]
  model
}

String DATABASE_URL = "jdbc:h2:data/SinapuliDB;DB_CLOSE_DELAY=-1"

ratpack {    	
	modules {
    register new H2Module('', '', DATABASE_URL)
		register new MapSessionsModule(100, 15)
    register new HandlebarsModule()
    register new SinapuliModule()
	}
	
	handlers { OffererRepository repoOfferer, TenderOfferRepository repoTenderOffer, ProjectRepository repoProject ->

		prefix('css') {
			assets "public/css"
		}
		prefix('images') {
			assets 'public/images'
		}
		prefix('scripts') {
			assets 'public/scripts'
		}

		handler('register') {
			byMethod {
				get {
					render handlebarsTemplate('register.html')
				}
				post {
					
					def form = parse(form())
					def offerer = new Offerer()

					//valida que ingresa los datos
					if( form.username?.trim() && 						
						form.password?.trim() &&
						form.repeat_password?.trim() &&
						form.publicKey?.trim()) {
  						
  						//valida que password y repeat_password sean iguales  						
						if(form.password == form.repeat_password){

							offerer.name = form.name
							offerer.publicKey = form.publicKey
							offerer.username = form.username							
							offerer.password = form.password

							offerer = repoOfferer.create(offerer)
							println "Oferente registrado: " + offerer
							redirect '/login'

						} else {
							render handlebarsTemplate("register.html", error: "Las contraseñas no coinciden")			
						}  						

					} else {						
						render handlebarsTemplate("register.html", error: "Le falto ingresar alguno de los campos")			
					}					
				}
			}
		}

		/*
		handler('upload') {
		  byMethod {
			get {
			  render groovyTemplate('/upload.html')      
			}

			post {
			  def f = context.parse(form())
			  def uploaded = f.file('file')
			  render groovyTemplate('/upload-result.html', filename: uploaded.fileName, content: uploaded.text)
			}
		  }
		}
		*/

    handler registry.get(AuthHandlers)

		get {
			def session = get(SessionStorage)
			render handlebarsTemplate("index.html", 
            withAuthModel(session, 
              projectList:repoProject.list(), 
              offererList:repoOfferer.list(), 
              tenderOfferList:repoTenderOffer.list()))
		}

		/*
		get("offerer/new"){
			render groovyTemplate("/offerer/new.html") 
		}
		*/

		get("offerer/edit/:id"){		  
			println "getting Offerer with ID " + pathTokens.id
			def offerer = repoOfferer.get((pathTokens.id).toInteger())
				  
			render groovyTemplate("/offerer/edit.html", id: offerer.id, name: offerer.name) 
		}		

		get("offerer/delete/:id"){
			render groovyTemplate("/offerer/delete.html", id: pathTokens.id)
		}

		get ("project/new") {
		    render groovyTemplate("/project/new.html")
		}

		get("project/edit/:id"){		  
			println "getting Project with ID " + pathTokens.id
			def project = repoProject.get((pathTokens.id).toInteger())
				  
			render groovyTemplate("/project/edit.html", id: project.id, name: project.name)
		}

		get ("project/offerts/:id") {
			def project = repoProject.get((pathTokens.id).toInteger())
		    	render groovyTemplate("/project/offerts.html", project: project, offertsList:repoTenderOffer.listWithproject(project.id))
		}		

		get("project/delete/:id/:name"){
			render groovyTemplate("/project/delete.html", id: pathTokens.id, name: pathTokens.name)
		}

		get ("tenderOffer/new/:id") {
			println "getting project with ID " + pathTokens.id
			def project = repoProject.get((pathTokens.id).toInteger())
		    	render groovyTemplate("/tenderOffer/new.html", project: project)
		}

		get("tenderOffer/edit/:id"){		  
			println "getting Offerer with ID " + pathTokens.id
			def tenderOffer = repoTenderOffer.get((pathTokens.id).toInteger())
				  
			render groovyTemplate("/tenderOffer/edit.html", id: tenderOffer.id, hash: tenderOffer.hash) 
		}		

		get("tenderOffer/delete/:id"){
			render groovyTemplate("/tenderOffer/delete.html", id: pathTokens.id)
		}


		// --------------------------------------------------------------------
		// POSTs
		// --------------------------------------------------------------------

		/*
		post ("offerer/submit") {
			def form = context.parse(form())
	
			def offerer = repoOfferer.create(form.name)

			def message = "Just created Offerer " + offerer.name + " with id " + offerer.id
			println message


			redirect "/" 
		}									
		*/		
	
		post ("update/offerer/:id") {
			def form = context.parse(form())

			// Update is a save with an id	
			repoOfferer.update(form.id.toInteger(), form.name)


			redirect "/" 
		}											

		post ("delete/offerer/:id") {
			println "Now deleting offerer with ID: ${pathTokens.id}"
			repoOfferer.delete(pathTokens.id.toInteger())


			redirect "/" 			
		}	

		post ("project/submit") {
			def form = context.parse(form())
	
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd/mm/yyyy");
			Date startTenderDate = dateFormat.parse(form.startTenderDate);
			Date endTenderDate = dateFormat.parse(form.endTenderDate);
			
			def uploaded = form.file('file');

			if(endTenderDate < startTenderDate ) {

				render groovyTemplate("/project/new.html", error: "las fechas ingresadas son invalidas") 
			    
			}else{
				def project = repoProject.create(form.name, 
								form.description, 
								startTenderDate, 
								endTenderDate, 
								uploaded.getBytes())

				def message = "Just created project " + project.name + " with id " + project.id
				println message
				redirect "/" 
			}
		}	
		
		post ("update/project/:id") {
			def form = context.parse(form())

			// Update is a save with an id	
			repoProject.update(form.id.toInteger(), form.name)

			redirect "/" 
		}											

		post ("project/delete") {
			def form = context.parse(form())
			println "Now deleting project with ID: ${pathTokens.id}"
			repoProject.delete(form.projectId.toInteger())
			redirect "/"						
		}		

		post ("tenderOffer/submit") {
			def form = context.parse(form())

			def offerer = repoOfferer.create(form.email)
			println "CREADO offerer " + offerer.id
			def project = repoProject.get(form.idproject.toInteger())
	
			def tenderOffer = repoTenderOffer.create(form.hash, offerer, project)

			redirect "/"		
		}									

		post ("update/tenderOffer/:id") {
			def form = context.parse(form())

			// Update is a save with an id	
			repoTenderOffer.update(form.id.toInteger(), form.hash)

			redirect "/" 			
			
		}											

		post ("delete/tenderOffer/:id") {
			println "Now deleting trendeOffer with ID: ${pathTokens.id}"
			repoTenderOffer.delete(pathTokens.id.toInteger())

			redirect "/" 
		}									
	}
}
