package garantito.sinapuli.helpers

import ratpack.handlebars.NamedHelper
import com.github.jknack.handlebars.Options

import java.text.DateFormat
import java.text.SimpleDateFormat

class IsoDateTimeHelper implements NamedHelper<Date> {
  @Override
  public String getName() {
    "isoDateTime"
  }

  public CharSequence apply(Date context, Options options) throws IOException {
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ")
    df.setTimeZone(TimeZone.default)
    df.format(context)
  }
}

