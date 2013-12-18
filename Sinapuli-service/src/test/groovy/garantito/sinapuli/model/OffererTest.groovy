package garantito.sinapuli.model

import garantito.sinapuli.*
import spock.lang.Specification

class OffererTest extends Specification {
  def "hash encrypts password on set"() {
    when:
    def offerer = new Offerer(password: 'foo')

    then:
    offerer.password != 'foo'
  }

  def "can set password and repeat password"() {
    setup:
    def offerer = new Offerer()

    when:
    offerer.password = 'password'
    offerer.repeatPassword = 'password'

    then:
    notThrown(Exception)
  }

  def "can check password"() {
    when:
    def offerer = new Offerer(password: 'foo')

    then:
    offerer.checkPassword('foo')
    !offerer.checkPassword('bar')
  }

  def "validates passwords match"() {
    setup:
    def offerer = new Offerer()

    when:
    offerer.with {
      name = 'Oferente'
      username = 'of'
      password = 'foo'
      repeatPassword = 'foo'
      publicKey = validPublicKey()
    }
    offerer.validate()

    then:
    notThrown(ValidationException)

    when:
    offerer.repeatPassword = 'bar'
    offerer.validate()

    then:
    thrown(ValidationException)

    when:
    offerer.repeatPassword = null
    offerer.validate()

    then:
    thrown(ValidationException)
  }

  def "validates public key"() {
    setup:
    def offerer = new Offerer()

    when:
    offerer.with {
      name = 'Oferente'
      username = 'of'
      password = 'foo'
      repeatPassword = 'foo'
      publicKey = validPublicKey()
    }
    offerer.validate()

    then:
    notThrown(ValidationException)

    when:
    offerer.publicKey = 'invalid certificate'
    offerer.validate()

    then:
    thrown(ValidationException)
  }

  private String validPublicKey() {
    getClass().classLoader.getResourceAsStream('cert.pem').text
  }
}

