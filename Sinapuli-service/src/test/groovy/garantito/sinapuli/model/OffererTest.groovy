package garantito.sinapuli.model

import spock.lang.Specification

class OffererTest extends Specification {
  def "different offerers have different hash codes"() {
    when:
    def off1 = new Offerer("oferente 1")
    def off2 = new Offerer("oferente 2")

    then:
    off1.hashCode() != off2.hashCode()
  }
}

