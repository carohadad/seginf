package garantito.tsa

import java.security.KeyStore
import com.google.inject.Inject

class KeyProvider {
  @Inject
  KeyStore keyStore

  def getPrivateKey() {
    keyStore.getKey('tsa', 'garantito'.toCharArray()) 
  }

  def getCertificate() {
    keyStore.getCertificate('tsa')
  }
}

