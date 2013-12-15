package garantito.tsa

import com.google.inject.Inject
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.x509.AlgorithmIdentifier
import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder
import org.bouncycastle.operator.bc.BcDigestCalculatorProvider
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import org.bouncycastle.tsp.TimeStampRequest
import org.bouncycastle.tsp.TimeStampResponse
import org.bouncycastle.tsp.TimeStampTokenGenerator
import org.bouncycastle.tsp.TimeStampResponseGenerator
import org.bouncycastle.tsp.TSPAlgorithms

class TSAModule {
  @Inject
  KeyProvider keyProvider

  def acceptedAlgorithms = [TSPAlgorithms.SHA256] as Set

  def validate(TimeStampRequest request) {
    request.validate(acceptedAlgorithms, null, null)
  }

  def buildContentSigner(privateKey) {
    new JcaContentSignerBuilder("SHA256withRSA").build(privateKey)
  }

  def getPrivateKey() {
    keyProvider.privateKey
  }

  def buildCertHolder() {
    new X509CertificateHolder(keyProvider.certificate.encoded)
  }

  def generate(request) {
    def date = new Date()
    def serialNumber = new BigInteger(160, new Random())

    def calcProv = new BcDigestCalculatorProvider()

    def contentSigner = buildContentSigner(privateKey)

    def certHolder = buildCertHolder()
    def sigBuilder = new JcaSignerInfoGeneratorBuilder(calcProv)
    def signerInfoGen = sigBuilder.build(contentSigner, certHolder)

    def digestCalculator = calcProv.get(new AlgorithmIdentifier(TSPAlgorithms.SHA256))
    def tsaPolicy = request.reqPolicy ?: new ASN1ObjectIdentifier("1.3.6.1.4.1.13762.3")

    def tokenGen = new TimeStampTokenGenerator(signerInfoGen, digestCalculator, tsaPolicy)

    def resGen = new TimeStampResponseGenerator(tokenGen, acceptedAlgorithms)

    def response = resGen.generate(request, serialNumber, date)

    response
  }

  def encode(response) {
    response.encoded.encodeBase64().toString()
  }
}
