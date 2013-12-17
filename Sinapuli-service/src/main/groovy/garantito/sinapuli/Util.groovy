package garantito.sinapuli

import ratpack.handling.Context
import ratpack.session.store.SessionStorage

import org.joda.time.*

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

  static Date parseUserDateTime(value) {
    if (value instanceof Date) {
      value
    } else if (value instanceof String) {
      DateTime.parse(value).toDate()
    } else {
      throw new IllegalArgumentException('invalid date')
    }
  }
}

