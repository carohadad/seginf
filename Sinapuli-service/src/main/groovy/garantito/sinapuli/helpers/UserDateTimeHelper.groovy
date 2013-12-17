package garantito.sinapuli.helpers

import ratpack.handlebars.NamedHelper
import com.github.jknack.handlebars.Options

import java.text.DateFormat
import java.text.SimpleDateFormat

class UserDateTimeHelper implements NamedHelper<Date> {
  @Override
  public String getName() {
    "userDateTime"
  }

  public CharSequence apply(Date context, Options options) throws IOException {
    DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm")
    df.setTimeZone(TimeZone.default)
    df.format(context)
  }
}

