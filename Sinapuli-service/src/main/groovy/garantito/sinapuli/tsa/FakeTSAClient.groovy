package garantito.sinapuli.tsa

import com.google.inject.Inject
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.x509.AlgorithmIdentifier
import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder
import org.bouncycastle.operator.bc.BcDigestCalculatorProvider
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import org.bouncycastle.tsp.*

import garantito.sinapuli.KeyProvider


class FakeTSAClient implements TSAClient {
  @Inject
  KeyProvider keyProvider

  def acceptedAlgorithms = [TSPAlgorithms.SHA256] as Set

  def date

  static final def DEFAULT_POLICY = new ASN1ObjectIdentifier("1.3.6.1.4.1.13762.3")

  @Override
  public byte[] getToken(byte[] imprint) {
    def algoFinder = new DefaultDigestAlgorithmIdentifierFinder()
    def algoIdentifier = algoFinder.find("SHA-256")

    // Setup the time stamp request
    def tsqGenerator = new TimeStampRequestGenerator()
    tsqGenerator.certReq = true
    tsqGenerator.reqPolicy = DEFAULT_POLICY
    def nonce = BigInteger.valueOf(System.currentTimeMillis())
    def request = tsqGenerator.generate(algoIdentifier.objectId, imprint, nonce)

    def response = generate(request)
    response.encoded
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
    def date = this.date ?: new Date()
    def serialNumber = new BigInteger(160, new Random())

    def calcProv = new BcDigestCalculatorProvider()
    def contentSigner = buildContentSigner(privateKey)

    def certHolder = buildCertHolder()
    def sigBuilder = new JcaSignerInfoGeneratorBuilder(calcProv)
    def signerInfoGen = sigBuilder.build(contentSigner, certHolder)

    def digestCalculator = calcProv.get(new AlgorithmIdentifier(TSPAlgorithms.SHA256))
    def tsaPolicy = request.reqPolicy ?: DEFAULT_POLICY

    def tokenGen = new TimeStampTokenGenerator(signerInfoGen, digestCalculator, tsaPolicy)
    def resGen = new TimeStampResponseGenerator(tokenGen, acceptedAlgorithms)
    def response = resGen.generate(request, serialNumber, date)

    response
  }
}

