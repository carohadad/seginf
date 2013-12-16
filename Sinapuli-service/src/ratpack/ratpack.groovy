import ratpack.groovy.Groovy
import static ratpack.groovy.Groovy.*
import static ratpack.form.Forms.form
import ratpack.session.Session
import ratpack.session.store.MapSessionsModule
import ratpack.session.store.SessionStorage
import ratpack.session.store.SessionStore

import ratpack.util.Action
import ratpack.groovy.handling.GroovyContext
import ratpack.groovy.handling.GroovyChain
import ratpack.handling.Handler
import ratpack.launch.LaunchConfig
import ratpack.registry.Registry

import ratpack.handlebars.HandlebarsModule
import static ratpack.handlebars.Template.handlebarsTemplate

import ratpack.h2.H2Module

import garantito.sinapuli.*
import garantito.sinapuli.model.*
import garantito.sinapuli.handlers.*

String DATABASE_URL = "jdbc:h2:data/SinapuliDB;DB_CLOSE_DELAY=-1"

ratpack {    	
	modules {
    register new H2Module('', '', DATABASE_URL)
		register new MapSessionsModule(100, 15)
    register new HandlebarsModule()

    register new SinapuliModule()
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

		handler 'register', registry.get(RegisterHandler)

    handler registry.get(AuthHandlers)

    handler {
      def session = context.get(SessionStorage)
      def handler
      if (session.role == 'admin') {
        handler = registry.get(AdminHandlers)
      } else {
        handler = registry.get(OffererHandlers)
      }
      insert([handler] as Handler[])
    }

    //handler('upload') {
    //  byMethod {
    //  get {
    //    render groovyTemplate('/upload.html')      
    //  }

    //  post {
    //    def f = context.parse(form())
    //    def uploaded = f.file('file')
    //    render groovyTemplate('/upload-result.html', filename: uploaded.fileName, content: uploaded.text)
    //  }
    //  }
    //}
  }
}

