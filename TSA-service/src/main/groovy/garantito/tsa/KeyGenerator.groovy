package garantito.tsa

import java.security.KeyPairGenerator
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DEROctetString
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x509.Extension
import org.bouncycastle.asn1.x509.ExtendedKeyUsage
import org.bouncycastle.asn1.x509.X509Extension
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.bouncycastle.cert.X509v3CertificateBuilder

//
// NOTA: esta clase no se usa. Queda para referencias futuras.
//
class KeyGenerator {
  def generateKeyPair() {
    def keygen = KeyPairGenerator.getInstance("RSA")
    keygen.initialize(2048)
    keygen.generateKeyPair()
  }

  def generateCertificate(keyPair) {
    def name = new X500Name("cn=garantito, o=uba, c=ar")
    def certSerial = BigInteger.ONE
    Date notBefore = new Date(System.currentTimeMillis() - 24 * 3600 * 1000)
    Date notAfter = new Date(System.currentTimeMillis() + 365 * 24 * 3600 * 1000)
    def publicKeyInfo = new SubjectPublicKeyInfo(ASN1Sequence.getInstance(keyPair.public.encoded))

    def certBuilder = new X509v3CertificateBuilder(name, certSerial, notBefore, notAfter, name, publicKeyInfo)

    def extendedKeyUsage = new ExtendedKeyUsage(KeyPurposeId.id_kp_timeStamping)
    def extension = new X509Extension(true, new DEROctetString(extendedKeyUsage))
    certBuilder = certBuilder.addExtension(Extension.extendedKeyUsage, true, extension.parsedValue)

    def contentSigner = buildContentSigner(keyPair)
    def certHolder = certBuilder.build(contentSigner)

    certHolder
  }
}

