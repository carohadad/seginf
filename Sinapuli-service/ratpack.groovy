@GrabResolver("https://oss.jfrog.org/artifactory/repo")
@GrabResolver("http://mvnrepository.com/artifact/")

@Grab("com.j256.ormlite:ormlite-jdbc:4.47")
@Grab("com.j256.ormlite:ormlite-core:4.47")
@Grab("com.h2database:h2:1.3.174")
@Grab("io.ratpack:ratpack-groovy:0.9.0-SNAPSHOT")

import static ratpack.groovy.Groovy.*

//def repoOfferer = new OffererRepository();

ratpack {    	

	OffererRepository repoOfferer = OffererRepository.instance

	handlers {

		// default route
		get {
			render groovyTemplate("index.html", offererList:repoOfferer.list())
		}

		// Form for creating new entries
		get ("new") {
		    render groovyTemplate("new.html")
		}

		// http://localhost:5050/edit/23
		get("edit/:id"){		  
			println "getting Offerer with ID " + pathTokens.id
			def offerer = repoOfferer.get((pathTokens.id).toInteger())
				  
			render groovyTemplate("edit.html", id: offerer.id, name: offerer.name) 
		}		

		// http://localhost:5050/delete/2
		get("delete/:id"){
			render groovyTemplate("delete.html", id: pathTokens.id)
		}

		// --------------------------------------------------------------------
		// POSTs
		// --------------------------------------------------------------------

		// Data posted from a form
		post ("submit") {
			def form = request.form
	
			def offerer = repoOfferer.create(form.name)

			def message = "Just created Offerer " + offerer.name + " with id " + offerer.id
			println message
	
			render groovyTemplate("index.html", offererList:repoOfferer.list())			
		}									

		// http://localhost:5050/update/offerer/1
		post ("update/offerer/:id") {
			def form = request.form

			// Update is a save with an id	
			repoOfferer.update(form.id.toInteger(), form.name)

			render groovyTemplate("index.html", offererList:repoOfferer.list())
		}											

		// http://localhost:5050/delete/offerer/1
		post ("delete/offerer/:id") {
			println "Now deleting offerer with ID: ${pathTokens.id}"
			repoOfferer.delete(pathTokens.id.toInteger())

			render groovyTemplate("index.html", offererList:repoOfferer.list())			
		}									

		assets "public"
	}
}

