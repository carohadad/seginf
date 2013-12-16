package garantito.sinapuli.handlers

import static ratpack.handlebars.Template.handlebarsTemplate
import static ratpack.form.Forms.form
import ratpack.session.Session
import ratpack.session.store.SessionStorage

import ratpack.groovy.handling.GroovyContext
import ratpack.groovy.handling.GroovyHandler

import javax.inject.Inject

import garantito.sinapuli.model.Offerer
import garantito.sinapuli.model.OffererRepository

class RegisterHandler extends GroovyHandler {
  private final OffererRepository repoOfferer

  @Inject
  RegisterHandler(OffererRepository repo) {
    this.repoOfferer = repo
  }

  @Override
  protected void handle(GroovyContext context) {
    context.byMethod {
      get {
        render handlebarsTemplate('register.html')
      }
      post {
        def form = parse(form())
        def offerer = new Offerer()
        def errorMessage

        // valida que ingresa los datos
        if (form.username?.trim() && 
          form.password?.trim() &&
          form.repeat_password?.trim() &&
          form.publicKey?.trim()) {

          // valida que password y repeat_password sean iguales 
          if (form.password == form.repeat_password) {
            offerer.name = form.name
            offerer.publicKey = form.publicKey
            offerer.username = form.username
            offerer.password = form.password

            offerer = repoOfferer.create(offerer)
            println "Oferente registrado: " + offerer
            redirect '/login'
            return

          } else {
            errorMessage = "Las contraseñas no coinciden"
          }

        } else {	
          errorMessage = "Le faltó ingresar alguno de los campos"
        }

        render handlebarsTemplate("register.html", error: errorMessage)
      }
    }
  }
}

