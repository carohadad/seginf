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
  }

  @Override
  public String toString() {
    "<TenderOffer ${id}, offerer=${offerer.id}, project=${project.id}>"
  }
}
