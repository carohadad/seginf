package garantito.sinapuli

import static ratpack.handlebars.Template.handlebarsTemplate
import static ratpack.form.Forms.form
import ratpack.session.Session
import ratpack.session.store.SessionStorage

import ratpack.groovy.handling.GroovyContext
import ratpack.groovy.handling.GroovyHandler

import javax.inject.Inject

class AuthHandlers extends GroovyHandler {
  private final OffererRepository repoOfferer

  @Inject
  AuthHandlers(OffererRepository repo) {
    this.repoOfferer = repo
  }

  @Override
  protected void handle(GroovyContext context) {
    context.with {
      def path = request.path

      if (path == 'login') {
        handleLogin(context)

      } else if (path == 'logout') {
        get(Session).terminate()
        redirect '/'

      } else {
        // default route
        if (!get(SessionStorage).auth) {
          redirect '/login'
          return
        } else {
          next()
        }
      }
    }
  }

  private def handleLogin(context) {
    context.with {
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
  }
}

