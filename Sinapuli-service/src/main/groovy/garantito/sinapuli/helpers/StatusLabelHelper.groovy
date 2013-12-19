package garantito.sinapuli.helpers

import ratpack.handlebars.NamedHelper
import com.github.jknack.handlebars.Options

import java.text.DateFormat
import java.text.SimpleDateFormat

import static garantito.sinapuli.model.Project.*

class StatusLabelHelper implements NamedHelper<Status> {
  @Override
  public String getName() {
    "statusLabel"
  }

  public CharSequence apply(Status context, Options options) throws IOException {
    switch (context) {
    case Status.PENDING:
      return "Pendiente"
    case Status.OPEN:
      return "Abierto"
    case Status.CLOSED:
      return "Cerrado"
    case Status.FINISHED:
      return "Finalizado"
    default:
      return "Desconocido"
    }
  }
}

