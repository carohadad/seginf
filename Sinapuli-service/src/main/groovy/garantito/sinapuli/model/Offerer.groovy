package garantito.sinapuli.model

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.field.DataType
import com.j256.ormlite.table.DatabaseTable

import groovy.transform.EqualsAndHashCode

import garantito.sinapuli.ValidationException
import static garantito.sinapuli.Util.*

@EqualsAndHashCode
@DatabaseTable(tableName = "offerers")
class Offerer {
  @DatabaseField(generatedId = true)
  Integer id

  @DatabaseField(canBeNull = false)
  String name

  @DatabaseField(canBeNull = false, unique = true)
  String username

  @DatabaseField(canBeNull = false)
  String password

  @DatabaseField(dataType = DataType.LONG_STRING)
  String publicKey

  private String plainPassword
  private String plainRepeatPassword

  Offerer() {
    // all persisted classes must define a no-arg constructor with at least package visibility
  }

  @Override
  public String toString() {
    "<Offerer ${id}, username=${username}>"
  }

  public void validate() {
    if (isBlank(username)) {
      throw new ValidationException('El nombre de usuario no puede estar vacío')
    }
    if (isBlank(name)) {
      throw new ValidationException('El nombre no puede estar vacío')
    }
    if (isBlank(publicKey)) {
      throw new ValidationException('La clave pública no puede estar vacía')
    }
    if (isBlank(password)) {
      throw new ValidationException('La contraseña no puede estar vacía')
    }
    if ((plainPassword != null || plainRepeatPassword != null) && 
      plainRepeatPassword != plainPassword) {
      throw new ValidationException('Las contraseñas no coinciden')
    }
  }

  public void setPassword(String value) {
    this.plainPassword = value
    if (value != null) {
      this.password = encryptPassword(value)
    } else {
      this.password = null
    }
  }

  public void setRepeatPassword(String value) {
    this.plainRepeatPassword = value
  }

  public boolean checkPassword(String plain) {
    password == encryptPassword(plain, passwordSalt(password))
  }
}

