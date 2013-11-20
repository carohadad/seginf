@GrabResolver("https://oss.jfrog.org/artifactory/repo")
@GrabResolver("http://mvnrepository.com/artifact/")

@Grab("com.j256.ormlite:ormlite-jdbc:4.47")
@Grab("com.j256.ormlite:ormlite-core:4.47")
@Grab("com.h2database:h2:1.3.174")
@Grab("io.ratpack:ratpack-groovy:0.9.0-SNAPSHOT")

import static ratpack.groovy.Groovy.*

ratpack {    	

	OffererRepository repoOfferer = OffererRepository.instance
	TenderOfferRepository repoTenderOffer = TenderOfferRepository.instance

	handlers {

		// default route
		get {
			render groovyTemplate("index.html", offererList:repoOfferer.list(), tenderOfferList:repoTenderOffer.list())			
		}

		get ("offerer/new") {
		    render groovyTemplate("/offerer/new.html")
		}

		get("offerer/edit/:id"){		  
			println "getting Offerer with ID " + pathTokens.id
			def offerer = repoOfferer.get((pathTokens.id).toInteger())
				  
			render groovyTemplate("/offerer/edit.html", id: offerer.id, name: offerer.name) 
		}		

		get("offerer/delete/:id"){
			render groovyTemplate("/offerer/delete.html", id: pathTokens.id)
		}

		get ("tenderOffer/new") {
		    render groovyTemplate("/tenderOffer/new.html")
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

		post ("offerer/submit") {
			def form = request.form
	
			def offerer = repoOfferer.create(form.name)

			def message = "Just created Offerer " + offerer.name + " with id " + offerer.id
			println message
	
			render groovyTemplate("index.html", offererList:repoOfferer.list(), tenderOfferList:repoTenderOffer.list())			
		}									

		post ("update/offerer/:id") {
			def form = request.form

			// Update is a save with an id	
			repoOfferer.update(form.id.toInteger(), form.name)

			render groovyTemplate("index.html", offererList:repoOfferer.list(), tenderOfferList:repoTenderOffer.list())			
		}											

		post ("delete/offerer/:id") {
			println "Now deleting offerer with ID: ${pathTokens.id}"
			repoOfferer.delete(pathTokens.id.toInteger())

			render groovyTemplate("index.html", offererList:repoOfferer.list(), tenderOfferList:repoTenderOffer.list())			
		}									

		post ("tenderOffer/submit") {
			def form = request.form
	
			def tenderOffer = repoTenderOffer.create(form.hash)

			def message = "Just created TenderOffer " + tenderOffer.hash + " with id " + tenderOffer.id
			println message
	
			render groovyTemplate("index.html", offererList:repoOfferer.list(), tenderOfferList:repoTenderOffer.list())			
		}									

		post ("update/tenderOffer/:id") {
			def form = request.form

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

