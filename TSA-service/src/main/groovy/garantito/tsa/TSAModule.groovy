package garantito.tsa

import java.security.*
import org.bouncycastle.asn1.*
import org.bouncycastle.asn1.x500.*
import org.bouncycastle.asn1.x509.*
import org.bouncycastle.cert.*
import org.bouncycastle.cms.*
import org.bouncycastle.cms.jcajce.*
import org.bouncycastle.operator.*
import org.bouncycastle.operator.bc.*
import org.bouncycastle.operator.jcajce.*
import org.bouncycastle.tsp.*
import org.bouncycastle.util.io.pem.*

class TSAModule {

  KeyStore keyStore
  def acceptedAlgorithms = [TSPAlgorithms.SHA1] as Set

  public TSAModule() {
    this(null)
  }

  public TSAModule(keyStore) {
    this.keyStore = keyStore
  }

  def validate(TimeStampRequest request) {
    request.validate(acceptedAlgorithms, null, null)
  }

//  def generateKeyPair() {
//    def keygen = KeyPairGenerator.getInstance("RSA")
//    keygen.initialize(2048)
//    keygen.generateKeyPair()
//  }
//
//  def generateCertificate(keyPair) {
//    def name = new X500Name("cn=garantito, o=uba, c=ar")
//    def certSerial = BigInteger.ONE
//    Date notBefore = new Date(System.currentTimeMillis() - 24 * 3600 * 1000)
//    Date notAfter = new Date(System.currentTimeMillis() + 365 * 24 * 3600 * 1000)
//    def publicKeyInfo = new SubjectPublicKeyInfo(ASN1Sequence.getInstance(keyPair.public.encoded))
//
//    def certBuilder = new X509v3CertificateBuilder(name, certSerial, notBefore, notAfter, name, publicKeyInfo)
//
//    def extendedKeyUsage = new ExtendedKeyUsage(KeyPurposeId.id_kp_timeStamping)
//    def extension = new X509Extension(true, new DEROctetString(extendedKeyUsage))
//    certBuilder = certBuilder.addExtension(Extension.extendedKeyUsage, true, extension.parsedValue)
//
//    def contentSigner = buildContentSigner(keyPair)
//    def certHolder = certBuilder.build(contentSigner)
//
//    certHolder
//  }

  def buildContentSigner(privateKey) {
    new JcaContentSignerBuilder("SHA1withRSA").build(privateKey)
  }

  def getPrivateKey() {
    keyStore.getKey('tsa', 'garantito'.toCharArray()) 
  }

  def buildCertHolder() {
    def certificate = keyStore.getCertificate('tsa')
    new X509CertificateHolder(certificate.encoded)
  }

  def generate(request) {
    def date = new Date()
    def serialNumber = new BigInteger(160, new Random())

    def calcProv = new BcDigestCalculatorProvider()

    def contentSigner = buildContentSigner(privateKey)

    def certHolder = buildCertHolder()
    def sigBuilder = new JcaSignerInfoGeneratorBuilder(calcProv)
    def signerInfoGen = sigBuilder.build(contentSigner, certHolder)

    def digestCalculator = calcProv.get(new AlgorithmIdentifier(TSPAlgorithms.SHA1))
    def tsaPolicy = null

    def tokenGen = new TimeStampTokenGenerator(signerInfoGen, digestCalculator, tsaPolicy)

    def resGen = new TimeStampResponseGenerator(tokenGen, acceptedAlgorithms)

    def response = resGen.generate(request, serialNumber, date)

    response
  }

  def encode(response) {
    response.encoded.encodeBase64().toString()
  }
}
