import spock.lang.*
import org.bouncycastle.tsp.*


class TSAModuleTest extends spock.lang.Specification {

  def "request validation: SHA1 should be valid"() {
    setup:
    TimeStampRequestGenerator reqGen = new TimeStampRequestGenerator();
    TimeStampRequest requestValid = reqGen.generate(TSPAlgorithms.SHA1, new byte[20], BigInteger.valueOf(100));
    TSAModule tsa = new TSAModule()

    when:
    tsa.validate(requestValid)

    then:
    notThrown(TSPException)
  }

  def "request validation: MD5 should be invalid"() {
    setup:
    println "setup"
    TimeStampRequestGenerator reqGen = new TimeStampRequestGenerator();
    TimeStampRequest requestInvalid = reqGen.generate(TSPAlgorithms.MD5, new byte[20], BigInteger.valueOf(100));
    TSAModule tsa = new TSAModule()

    when:
    tsa.validate(requestInvalid)

    then:
    thrown(TSPException)
  }
}
