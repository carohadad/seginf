package garantito.tsa

import spock.lang.*
import org.bouncycastle.tsp.*
import java.security.*


class TSAModuleTest extends spock.lang.Specification {

  static class DummySerialGenerator implements SerialNumberGenerator {
    BigInteger cache = BigInteger.ONE

    synchronized BigInteger next() {
      cache++
    }
  }

  private buildTSAModule() {
    def keyProvider = new KeyProvider()
    keyProvider.keyStore = loadKeyStore()

    def module = new TSAModule()
    module.keyProvider = keyProvider

    module.serialNumberGen = new DummySerialGenerator()

    module
  }

  private def loadKeyStore() {
    def keyStore = KeyStore.getInstance("JKS")
    def keyStoreStream = getClass().classLoader.getResourceAsStream('test.jks')
    keyStore.load(keyStoreStream, 'garantito'.toCharArray())
    keyStore
  }

  private def loadTSQ(filename) {
    def stream = getClass().classLoader.getResourceAsStream(filename)
    new TimeStampRequest(stream)
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

    then:
    resp != null && resp.failInfo == null
  }

  def "should generate tokens for sample requests"() {
    setup:
    def tsa = buildTSAModule()

    when:
    def tsq = loadTSQ(name)
    def tsr = tsa.generate(tsq)

    then:
    tsr != null && tsr.failInfo == null

    where:
    name << ["tsa-client.tsq", "tsa-pdf-signer.tsq", "tsa-client-no-policy.tsq"]
  }
}

