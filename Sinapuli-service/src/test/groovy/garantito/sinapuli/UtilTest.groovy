package garantito.sinapuli

import spock.lang.Specification

class UtilTest extends Specification {
  def "encryptPassword hashes a password"() {
    setup:
    def plain = 'password'

    when:
    def hashed = Util.encryptPassword(plain)

    then:
    hashed != plain
  }

  def "encryptPassword uses different salts"() {
    setup:
    def plain = 'password'

    when:
    def hashed1 = Util.encryptPassword(plain)
    def hashed2 = Util.encryptPassword(plain)

    then:
    hashed1 != hashed2
  }

  def "encryptPassword with salt produces same result"() {
    setup:
    def plain = 'password'

    when:
    def hashed = Util.encryptPassword(plain)

    then:
    hashed == Util.encryptPassword(plain, Util.passwordSalt(hashed))
  }
}


