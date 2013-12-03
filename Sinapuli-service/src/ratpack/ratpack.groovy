import static ratpack.groovy.Groovy.*
import static ratpack.form.Forms.form

import java.text.*
import java.util.*
import garantito.sinapuli.*

ratpack {    	

	OffererRepository repoOfferer = OffererRepository.instance
	TenderOfferRepository repoTenderOffer = TenderOfferRepository.instance
	ProyectRepository repoProyect = ProyectRepository.instance

	handlers {

		// default route
		get {
			render groovyTemplate("index.html", proyectList:repoProyect.list(), offererList:repoOfferer.list(), tenderOfferList:repoTenderOffer.list())			
		}

		get ("index"){
			render groovyTemplate("index.html", proyectList:repoProyect.list())			
		}
		

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
				  
			render groovyTemplate("/proyect/edit.html", proyect: proyect) 
		}

		get ("proyect/offerts/:id") {
			def proyect = repoProyect.get((pathTokens.id).toInteger())
		    	render groovyTemplate("/proyect/offerts.html", proyect: proyect, offertsList:repoTenderOffer.listWithProyect(proyect.id))
		}		

		get("proyect/delete/:id/:nombre"){
			render groovyTemplate("/proyect/delete.html", id: pathTokens.id, nombre: pathTokens.nombre)
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

		// --------------------------------------------------------------------
		// POSTs
		// --------------------------------------------------------------------

		post ("offerer/submit") {
			def form = context.parse(form())
	
			def offerer = repoOfferer.create(form.name)

			def message = "Just created Offerer " + offerer.name + " with id " + offerer.id
			println message
	
			render groovyTemplate("index.html", offererList:repoOfferer.list(), tenderOfferList:repoTenderOffer.list())			
		}									

		post ("update/offerer/:id") {
			def form = context.parse(form())

			// Update is a save with an id	
			repoOfferer.update(form.id.toInteger(), form.name)

			render groovyTemplate("index.html", offererList:repoOfferer.list(), tenderOfferList:repoTenderOffer.list())			
		}											

		post ("delete/offerer/:id") {
			println "Now deleting offerer with ID: ${pathTokens.id}"
			repoOfferer.delete(pathTokens.id.toInteger())

			render groovyTemplate("index.html", offererList:repoOfferer.list(), tenderOfferList:repoTenderOffer.list())			
		}	

		post ("proyect/submit") {
			def form = context.parse(form())
	
			String dateString = "2001/03/09";
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd/mm/yyyy");
			Date convertedDate = dateFormat.parse(form.fechaInicioLicitacion);
			
			def proyect = repoProyect.create(form.name, form.empresa, form.descripcion, convertedDate, 48)

			def message = "Just created Proyect " + proyect.nombre + " with id " + proyect.id
			println message
			redirect "/index" 
			//render groovyTemplate("index.html", proyectList:repoProyect.list())			
		}	

		post ("proyect/delete") {
			def form = context.parse(form())
			println "Now deleting proyect with ID: ${pathTokens.id}"
			repoProyect.delete(form.proyectId.toInteger())
			redirect "/index"
			//repoProyect.delete(pathTokens.id.toInteger())

			//render groovyTemplate("index.html", proyectList:repoProyect.list())			
		}
							

		post ("tenderOffer/submit") {
			def form = context.parse(form())

			def offerer = repoOfferer.create(form.email)
			println "CREADO offerer " + offerer.id
			def proyect = repoProyect.get(form.idProyect.toInteger())
	
			def tenderOffer = repoTenderOffer.create(form.hash, offerer, "un documento", proyect)
			//def message = "Importante: " + tenderOffer.getProyect().getNombre()
			//def message = "Just created TenderOffer " + tenderOffer.hash + " with id " + tenderOffer.id + " for proyect id " + tenderOffer.proyect.id+ " y el ofertante id " + tenderOffer.offerer.id
			//println message
			redirect "/index"		
		}									

		post ("update/tenderOffer/:id") {
			def form = context.parse(form())

			// Update is a save with an id	
			repoTenderOffer.update(form.id.toInteger(), form.hash)

			render groovyTemplate("index.html", offererList:repoOfferer.list(), tenderOfferList:repoTenderOffer.list())			
		}											

		post ("delete/tenderOffer/:id") {
			println "Now deleting trendeOffer with ID: ${pathTokens.id}"
			repoTenderOffer.delete(pathTokens.id.toInteger())

			render groovyTemplate("index.html", offererList:repoOfferer.list(), tenderOfferList:repoTenderOffer.list())			
		}									
		assets "public"
	}
}

