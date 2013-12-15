package garantito.tsa

import spock.lang.*
import org.bouncycastle.tsp.*
import java.security.*


class TSAModuleTest extends spock.lang.Specification {

  private buildTSAModule() {
    new TSAModule(loadKeyStore())
  }

  def "request validation: SHA256 should be valid"() {
    setup:
    TimeStampRequestGenerator reqGen = new TimeStampRequestGenerator();
    TimeStampRequest requestValid = reqGen.generate(TSPAlgorithms.SHA256, new byte[32], BigInteger.valueOf(100));
    TSAModule tsa = buildTSAModule()

    when:
    tsa.validate(requestValid)

    then:
    notThrown(TSPException)
  }

  def "request validation: MD5 should be invalid"() {
    setup:
    TimeStampRequestGenerator reqGen = new TimeStampRequestGenerator();
    TimeStampRequest requestInvalid = reqGen.generate(TSPAlgorithms.MD5, new byte[20], BigInteger.valueOf(100));
    TSAModule tsa = buildTSAModule()

    when:
    tsa.validate(requestInvalid)

    then:
    thrown(TSPException)
  }

  def "should generate time stamp"() {
    setup:
    TSAModule tsa = buildTSAModule()
    def reqGen = new TimeStampRequestGenerator()
    def request = reqGen.generate(TSPAlgorithms.SHA256, new byte[32])

    when:
    def resp = tsa.generate(request)
    println tsa.encode(resp)

    then:
    resp != null
  }

  private def loadKeyStore() {
    def keyStore = KeyStore.getInstance("JKS")
    def keyStoreStream = getClass().classLoader.getResourceAsStream('test.jks')
    keyStore.load(keyStoreStream, 'garantito'.toCharArray())
    keyStore
  }

}

