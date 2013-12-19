package garantito.sinapuli.helpers

import ratpack.handlebars.NamedHelper
import com.github.jknack.handlebars.Options

import java.text.DateFormat
import java.text.SimpleDateFormat

import static garantito.sinapuli.model.Project.*

class StatusCssHelper implements NamedHelper<Status> {
  @Override
  public String getName() {
    "statusCss"
  }

  public CharSequence apply(Status context, Options options) throws IOException {
    switch (context) {
    case Status.PENDING:
      return "default"
    case Status.OPEN:
      return "success"
    case Status.CLOSED:
      return "warning"
    case Status.FINISHED:
      return "danger"
    default:
      return "default"
    }
  }
}

