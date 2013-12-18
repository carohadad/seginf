package garantito.sinapuli

import ratpack.handling.Context
import ratpack.session.store.SessionStorage

import org.joda.time.*
import java.security.MessageDigest
import java.security.SecureRandom
import java.security.cert.CertificateFactory
import java.security.cert.CertificateException
import java.security.cert.X509Certificate

class Util {
	public static final int PASSWORD_HASHING_ROUNDS = 1000
  public static final int SALT_BYTES = 8

  static def buildModel(Context context) {
    buildModel([:], context)
  }

  static def buildModel(Map model, Context context) {
    def session = context.get(SessionStorage)
    def authModel = [ 
      auth: [
        loggedIn: session.auth,
        username: session.username,
        isAdmin: (session.role == 'admin')
      ]
    ]
    authModel + model
  }

  static Date parseUserDateTime(value) {
    if (value instanceof Date) {
      value
    } else if (value instanceof String) {
      DateTime.parse(value).toDate()
    } else {
      throw new IllegalArgumentException('invalid date')
    }
  }

  static boolean isBlank(String value) {
    value == null || value.trim() == ''
  }

  static boolean isEmpty(byte[] value) {
    value == null || value.length == 0
  }

  static boolean isNull(value) {
    value == null
  }

  /**
   * From a password, a number of iterations and a salt,
   * returns the corresponding digest
   * @param iterationNb int The number of iterations of the algorithm
   * @param password String The password to encrypt
   * @param salt byte[] The salt
   * @return byte[] The digested password
   * @throws NoSuchAlgorithmException If the algorithm doesn't exist
   */
  private static byte[] hashPasswordRounds(int rounds, String plain, byte[] salt) {
    def digest = MessageDigest.getInstance("SHA-256")
    digest.reset()
    digest.update(salt)
    byte[] input = digest.digest(plain.getBytes("UTF-8"))
    for (int i = 0; i < rounds; i++) {
      digest.reset()
      input = digest.digest(input)
    }
    input
  }

  public static String encryptPassword(String plain) {
    // Uses a secure Random not a simple Random
    def random = SecureRandom.getInstance("SHA1PRNG")
    byte[] salt = new byte[SALT_BYTES]
    random.nextBytes(salt)

    encryptPassword(plain, salt)
  }

  public static String encryptPassword(String plain, byte[] salt) {
    byte[] hashed = hashPasswordRounds(PASSWORD_HASHING_ROUNDS, plain, salt)    
    salt.encodeHex().toString() + hashed.encodeHex().toString()
  }

  public static byte[] passwordSalt(String password) {
    password[0..SALT_BYTES*2-1].decodeHex()
  }

  public static X509Certificate importCertificate(String pem) {
    def cf = CertificateFactory.getInstance('X.509')
    try {
      cf.generateCertificate(new ByteArrayInputStream(pem.bytes))
    } catch (CertificateException e) {
      null
    }
  }
}

