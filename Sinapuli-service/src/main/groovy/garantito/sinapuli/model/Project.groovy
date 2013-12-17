package garantito.sinapuli.model

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.field.DataType
import com.j256.ormlite.table.DatabaseTable

import groovy.transform.EqualsAndHashCode

import org.joda.time.*
import static garantito.sinapuli.Util.*
import garantito.sinapuli.ValidationException


@EqualsAndHashCode
@DatabaseTable(tableName = "projects")
class Project {

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
    if (startTenderDate > endTenderDate) {
      throw new ValidationException('La fecha de cierre no puede ser anterior a la de apertura')
    }
    if (isEmpty(tender)) {
      throw new ValidationException('Falta el documento del pliego') 
    }
  }
}

