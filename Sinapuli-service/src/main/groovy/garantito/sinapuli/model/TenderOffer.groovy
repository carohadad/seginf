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
  public static final int SHA256_LENGTH = 256/8

  @DatabaseField(generatedId = true)
  Integer id

  @DatabaseField
  Date offerDate

  @DatabaseField
  String hash

  @DatabaseField(dataType = DataType.LONG_STRING)
  String hashSignature

  @DatabaseField(dataType = DataType.BYTE_ARRAY)
  byte[] document

  @DatabaseField
  String documentType

  @DatabaseField
  String documentFilename

  @DatabaseField
  Date completeDate

  @DatabaseField(dataType = DataType.LONG_STRING)
  String recepitToken

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
    try {
      def hashBytes = hash.decodeHex()
      if (hashBytes.length != SHA256_LENGTH) {
        throw new ValidationException("El hash no es SHA256 válido")
      }
    } catch (NumberFormatException e) {
      throw new ValidationException("El hash no es SHA256 válido")
    }
    if (isBlank(hashSignature)) {
      throw new ValidationException("La firma del hash no puede estar vacía")
    }
    if (offerer == null) {
      throw new ValidationException("El oferente no puede ser nulo")
    }
    if (project == null) {
      throw new ValidationException("El proyecto no puede ser nulo")
    }
    if (completeDate != null && completeDate < offerDate) {
      throw new ValidationException("La fecha de completado de la oferta no puede ser anterior a la de realización")
    }
  }

  public boolean isComplete() {
    document != null && document.length > 0 && completeDate != null
  }
}
