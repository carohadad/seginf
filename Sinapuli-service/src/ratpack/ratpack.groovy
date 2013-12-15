import static ratpack.groovy.Groovy.*
import static ratpack.form.Forms.form
import ratpack.session.Session
import ratpack.session.store.MapSessionsModule
import ratpack.session.store.SessionStorage
import ratpack.session.store.SessionStore

import ratpack.handlebars.HandlebarsModule
import static ratpack.handlebars.Template.handlebarsTemplate

import java.security.MessageDigest
import java.text.SimpleDateFormat

import garantito.sinapuli.*

ratpack {    	

	OffererRepository repoOfferer = OffererRepository.instance
	TenderOfferRepository repoTenderOffer = TenderOfferRepository.instance
	ProyectRepository repoProyect = ProyectRepository.instance

	modules {
		register new MapSessionsModule(100, 15)
    register new HandlebarsModule()
	}
	
	handlers {

		prefix('css') {
			assets "public/css"
		}
		prefix('images') {
			assets 'public/images'
		}
		prefix('scripts') {
			assets 'public/scripts'
		}

		handler('login') {
			byMethod {
				get {
					render handlebarsTemplate("login.html")
				}
				post {
					def form = parse(form())
					def session = get(SessionStorage)
					if (form.username == 'admin' && form.password == 'admin') {
						session.auth = true
						session.username = form.username
						session.role = 'admin'
					} else {
						if (repoOfferer.authenticate(form.username, form.password)) {
							session.auth = true
							session.username = form.username
							session.role = 'offerer'
						} else {
							render handlebarsTemplate("login.html", error: "Usuario o contraseña no válidos")
              return
						} 
					}
					if (session.auth) {
						redirect '/'
					} else {
						redirect '/login'
					}
				}
			}
		}

		get('logout') {
			get(Session).terminate()
			redirect '/'
		}

		handler('register') {
			byMethod {
				get {
					render groovyTemplate('register.html')
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
							render groovyTemplate("register.html", error: "Las contraseñas no coinciden")			
						}  						

					} else {						
						render groovyTemplate("register.html", error: "Le falto ingresar alguno de los campos")			
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

		handler {
			// default route
			if (!get(SessionStorage).auth) {
				redirect '/login'
				return
			}
			next()
		}
		
		get {
			def session = get(SessionStorage)
			render groovyTemplate("index.html", 
						loggedIn: session.auth, 
						loginName: session.username, 
						proyectList:repoProyect.list(), 
						offererList:repoOfferer.list(), 
						tenderOfferList:repoTenderOffer.list())			
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

		get ("proyect/new") {
		    render groovyTemplate("/proyect/new.html")
		}

		get("proyect/edit/:id"){		  
			println "getting Proyect with ID " + pathTokens.id
			def proyect = repoProyect.get((pathTokens.id).toInteger())
				  
			render groovyTemplate("/proyect/edit.html", id: proyect.id, name: proyect.name)
		}

		get ("proyect/offerts/:id") {
			def proyect = repoProyect.get((pathTokens.id).toInteger())
		    	render groovyTemplate("/proyect/offerts.html", proyect: proyect, offertsList:repoTenderOffer.listWithProyect(proyect.id))
		}		

		get("proyect/delete/:id/:name"){
			render groovyTemplate("/proyect/delete.html", id: pathTokens.id, name: pathTokens.name)
		}

		get ("tenderOffer/new/:id") {
			println "getting Proyect with ID " + pathTokens.id
			def proyect = repoProyect.get((pathTokens.id).toInteger())
		    	render groovyTemplate("/tenderOffer/new.html", proyect: proyect)
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

		post ("proyect/submit") {
			def form = context.parse(form())
	
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd/mm/yyyy");
			Date startTenderDate = dateFormat.parse(form.startTenderDate);
			Date endTenderDate = dateFormat.parse(form.endTenderDate);
			
			def uploaded = form.file('file');

			if(endTenderDate < startTenderDate ) {

				render groovyTemplate("/proyect/new.html", error: "las fechas ingresadas son invalidas") 
			    
			}else{
				def proyect = repoProyect.create(form.name, 
								form.description, 
								startTenderDate, 
								endTenderDate, 
								uploaded.getBytes())

				def message = "Just created Proyect " + proyect.name + " with id " + proyect.id
				println message
				redirect "/" 
			}
		}	
		
		post ("update/proyect/:id") {
			def form = context.parse(form())

			// Update is a save with an id	
			repoProyect.update(form.id.toInteger(), form.name)

			redirect "/" 
		}											

		post ("proyect/delete") {
			def form = context.parse(form())
			println "Now deleting proyect with ID: ${pathTokens.id}"
			repoProyect.delete(form.proyectId.toInteger())
			redirect "/"						
		}		

		post ("tenderOffer/submit") {
			def form = context.parse(form())

			def offerer = repoOfferer.create(form.email)
			println "CREADO offerer " + offerer.id
			def proyect = repoProyect.get(form.idProyect.toInteger())
	
			def tenderOffer = repoTenderOffer.create(form.hash, offerer, proyect)

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
