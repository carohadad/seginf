package garantito.sinapuli

import java.security.KeyStore
import com.google.inject.Inject

class KeyProvider {
  @Inject
  KeyStore keyStore

  def getPrivateKey() {
    keyStore.getKey('sinapuli', 'garantito'.toCharArray()) 
  }

  def getCertificate() {
    keyStore.getCertificate('sinapuli')
  }
}

