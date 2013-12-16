package garantito.sinapuli

import ratpack.handling.Context
import ratpack.session.store.SessionStorage

class Util {
  static def withAuthModel(Map model, Context context) {
    def session = context.get(SessionStorage)
    def authModel = [
      loggedIn: session.auth,
      username: session.username,
      role: session.role
    ]
    authModel + model
  }
}

