package garantito.sinapuli.model

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.field.DataType
import com.j256.ormlite.table.DatabaseTable

import groovy.transform.EqualsAndHashCode

import org.joda.time.*
import static garantito.sinapuli.Util.*
import garantito.sinapuli.ValidationException

import org.joda.time.DateTime
import org.joda.time.Period

@EqualsAndHashCode
@DatabaseTable(tableName = "projects")
class Project {
  public static final Period CLOSING_PERIOD = Period.hours(48)

  public static enum Status {
    PENDING,
    OPEN,
    CLOSED,
    FINISHED
  }

  @DatabaseField(generatedId = true)
  Integer id

  @DatabaseField()
  String name

  @DatabaseField(dataType = DataType.LONG_STRING)
  String description

  @DatabaseField()
  Date creationDate

  @DatabaseField()
  Date startTenderDate

  @DatabaseField()
  Date endTenderDate

  @DatabaseField(dataType = DataType.BYTE_ARRAY)
  byte[] tender

  @DatabaseField
  String tenderContentType

  @DatabaseField
  String tenderFilename

  long offerCount
  def offers

  Project() {
    creationDate = new Date()
    startTenderDate = new DateTime().plus(Period.days(1)).withTime(10,0,0,0).toDate()
    endTenderDate = new DateTime(startTenderDate).plus(Period.days(3)).toDate()
  }

  @Override
  public String toString() {
    "<Project ${id}, name=${name}>"
  }

  public void setStartTenderDate(value) {
    this.startTenderDate = parseUserDateTime(value)
  }
  
  public void setEndTenderDate(value) {
    this.endTenderDate = parseUserDateTime(value)
  }

  public Date getFinishTenderDate() {
    new DateTime(endTenderDate).plus(CLOSING_PERIOD).toDate()
  }

  public long getOfferCount() {
    if (this.offers == null) {
      this.offerCount
    } else {
      this.offers.size()
    }
  }

  public void validate() {
    if (isBlank(name)) {
      throw new ValidationException('El nombre no puede estar vacío')
    }
    if (isBlank(description)) {
      throw new ValidationException('La descripción no puede estar vacía')
    }
    if (isNull(startTenderDate) || isNull(endTenderDate)) {
      throw new ValidationException('Falta completar las fechas')
    }
    if (startTenderDate >= endTenderDate) {
      throw new ValidationException('La fecha de cierre no puede ser anterior a la de apertura')
    }
    if (isEmpty(tender)) {
      throw new ValidationException('Falta el documento del pliego') 
    }
  }

  public Status getStatus() {
    def now = new Date()

    if (now < startTenderDate) {
      Status.PENDING
    } else if (now < endTenderDate) {
      Status.OPEN
    } else if (now < finishTenderDate) {
      Status.CLOSED
    } else {
      Status.FINISHED
    }
  }

  public boolean isOpen() {
    status == Status.OPEN
  }
  public boolean isClosed() {
    status == Status.CLOSED
  }
  public boolean isPending() {
    status == Status.PENDING
  }
  public boolean isFinished() {
    status == Status.FINISHED
  }
}

