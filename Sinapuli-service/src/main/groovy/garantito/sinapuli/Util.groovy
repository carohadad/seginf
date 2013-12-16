package garantito.sinapuli

import ratpack.handling.Context
import ratpack.session.store.SessionStorage

class Util {
  static def buildModel(Context context) {
    buildModel([:], context)
  }

  static def buildModel(Map model, Context context) {
    def session = context.get(SessionStorage)
    def authModel = [ 
      auth: [
        loggedIn: session.auth,
        username: session.username,
        isAdmin: (session.role == 'admin')
      ]
    ]
    authModel + model
  }
}

