package garantito.sinapuli.model

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.field.DataType
import com.j256.ormlite.table.DatabaseTable

import groovy.transform.EqualsAndHashCode

import garantito.sinapuli.ValidationException
import static garantito.sinapuli.Util.*

@EqualsAndHashCode
@DatabaseTable(tableName = "tenderOffers")
class TenderOffer {

  @DatabaseField(generatedId = true)
  Integer id

  @DatabaseField
  Date offerDate

  @DatabaseField
  String hash

  @DatabaseField(dataType = DataType.BYTE_ARRAY)
  byte[] document

  @DatabaseField
  String documentType

  @DatabaseField
  String documentFilename

  @DatabaseField(foreign = true, foreignAutoRefresh = true)
  Offerer offerer

  @DatabaseField(foreign = true, foreignAutoRefresh = true)
  Project project

  TenderOffer() {
    offerDate = new Date()
  }

  @Override
  public String toString() {
    "<TenderOffer ${id}, offerer=${offerer.id}, project=${project.id}>"
  }

  public void validate() {
    if (offerDate == null) {
      throw new ValidationException("La fecha de la oferta no puede ser nula")
    }
    if (isBlank(hash)) {
      throw new ValidationException("El hash de la oferta no puede estar vacío")
    }
    if (offerer == null) {
      throw new ValidationException("El oferente no puede ser nulo")
    }
    if (project == null) {
      throw new ValidationException("El proyecto no puede ser nulo")
    }
  }
}
