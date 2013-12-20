package garantito.sinapuli

import java.security.MessageDigest

class Digester {
  MessageDigest md

  Digester() {
    md = MessageDigest.getInstance('SHA-256')
  }

  public byte[] digest(byte[] content) {
    md.reset()
    md.update(content)
    md.digest()
  }
}

