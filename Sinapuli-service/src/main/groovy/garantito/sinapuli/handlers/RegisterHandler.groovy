package garantito.sinapuli.handlers

import static ratpack.handlebars.Template.handlebarsTemplate
import ratpack.form.Form
import ratpack.session.Session
import ratpack.session.store.SessionStorage

import ratpack.groovy.handling.GroovyContext
import ratpack.groovy.handling.GroovyHandler

import javax.inject.Inject

import garantito.sinapuli.ValidationException
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
        render handlebarsTemplate('register.html', offerer: new Offerer())
      }
      post {
        def form = context.parse(Form.class)
        def offerer

        try {
          offerer = new Offerer(
            name: form.name,
            username: form.username,
            publicKey: form.publicKey,
            password: form.password,
            repeatPassword: form.repeatPassword)

          offerer = repoOfferer.create(offerer)

          println "Oferente registrado: " + offerer
          redirect '/login'

        } catch (ValidationException e) {
          render handlebarsTemplate("register.html", error: e.message, offerer: offerer)
        }
      }
    }
  }
}

