import ratpack.groovy.Groovy
import static ratpack.groovy.Groovy.*
import ratpack.session.Session
import ratpack.session.SessionModule
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
import garantito.sinapuli.helpers.*
import garantito.sinapuli.tsa.*

import java.security.KeyStore

String DATABASE_URL = "jdbc:h2:data/SinapuliDB;DB_CLOSE_DELAY=-1"
String TSA_URL = "http://localhost:5050/timestamp"

def loadKeyStore() {
  def keyStore = KeyStore.getInstance("JKS")
  keyStore.load(new File('sinapuli.jks').newInputStream(), 'garantito'.toCharArray())
  keyStore
}


ratpack {    	
	modules {
    bind IsoDateTimeHelper
    bind UserDateTimeHelper
    bind StatusLabelHelper
    bind StatusCssHelper

    bind KeyStore, loadKeyStore()

    register new H2Module('', '', DATABASE_URL)
    register new SessionModule()
		register new MapSessionsModule(100, 15)
    register new HandlebarsModule()

    register new TSAModule(TSA_URL, null, null)
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
  }
}

