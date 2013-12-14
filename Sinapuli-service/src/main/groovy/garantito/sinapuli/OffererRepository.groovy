package garantito.sinapuli

import com.j256.ormlite.jdbc.JdbcConnectionSource
import com.j256.ormlite.support.ConnectionSource
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager
import com.j256.ormlite.table.TableUtils

import java.sql.SQLException;
import java.util.List;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;
import java.security.MessageDigest;
import java.io.IOException;
import java.util.Arrays;
import java.security.SecureRandom;


@Singleton(lazy = true, strict = false)
public class OffererRepository {

	String DATABASE_URL = "jdbc:h2:~/seginf/Sinapuli-service/SinapuliDB;DB_CLOSE_DELAY=-1";
	int ITERATION_NUMBER = 1000;

	JdbcConnectionSource connectionSource = null;
	Dao<Offerer, Integer> offererDao = null;

	private OffererRepository() {
		try {
			if(connectionSource == null){
				// create our data source			
				connectionSource = new JdbcConnectionSource(DATABASE_URL);
				// setup our database and DAOs
				setupDatabase(connectionSource);
				System.out.println("\n\nIt seems to have worked\n\n");		
			}
		} finally {
			// destroy the data source which should close underlying connections
			if (connectionSource != null) {
				connectionSource.close();
			}
		}
	}

	/**
	 * Setup our database and DAOs
	 */
	private void setupDatabase(ConnectionSource connectionSource) throws Exception {

		offererDao = DaoManager.createDao(connectionSource, Offerer.class);
		// if you need to create the table
		TableUtils.createTableIfNotExists(connectionSource, Offerer.class);
	}

	public void delete(int id) throws SQLException, Exception {
		def offerer = get(id);
		offererDao.delete(offerer);
	}

	public void update(int id, String name) throws SQLException, Exception {	
		def offerer = get(id);
		offerer.setName(name);

		offererDao.update(offerer);
	}

	public Offerer get(int id) throws SQLException, Exception {
		return offererDao.queryForId(id);
	}

	/*
	public Offerer create(String name) throws Exception {
		Offerer offerer = new Offerer(name);

		offererDao.create(offerer);
		return offerer;
	}
	*/

	public Offerer create(Offerer offerer) throws Exception {

		// Uses a secure Random not a simple Random
		SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
		// Salt generation 64 bits long
		byte[] bSalt = new byte[8];
		random.nextBytes(bSalt);
		// Digest computation
		byte[] bDigest = getHash(ITERATION_NUMBER,offerer.password,bSalt);
		String sDigest = byteToBase64(bDigest);
		String sSalt = byteToBase64(bSalt);
		

		offerer.password = sDigest
		offerer.salt = sSalt

		offererDao.create(offerer)
		offerer
	}

	public boolean authenticate(String username, String password) {
		
		//def results = offererDao.queryForFieldValuesArgs(username: username, password: password)
		//return results.size() == 1

		def results = offererDao.queryForFieldValuesArgs(username: username)
		
		if (results.size() < 1)
			return false

		def obj = results.get(0); //chequear que no haya mas de uno (dos usuarios con mismo username)

        byte[] bDigest = base64ToByte(obj.password);
        byte[] bSalt = base64ToByte(obj.salt);
 
        // Compute the new DIGEST
        byte[] proposedDigest = getHash(ITERATION_NUMBER, password, bSalt);
        return proposedDigest == bDigest

		
	}

	public List<Offerer> list() {
		try {
		    return offererDao.queryForAll();
		} catch (SQLException e) {
		    e.printStackTrace();
		}
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
   public byte[] getHash(int iterationNb, String password, byte[] salt){
       MessageDigest digest = MessageDigest.getInstance("SHA-256");
       digest.reset();
       digest.update(salt);
       byte[] input = digest.digest(password.getBytes("UTF-8"));
       for (int i = 0; i < iterationNb; i++) {
           digest.reset();
           input = digest.digest(input);
       }
       return input;
   }


   /**
    * From a base 64 representation, returns the corresponding byte[] 
    * @param data String The base64 representation
    * @return byte[]
    * @throws IOException
    */
   public static byte[] base64ToByte(String data) throws IOException {
       BASE64Decoder decoder = new BASE64Decoder();
       return decoder.decodeBuffer(data);
   }
 
   /**
    * From a byte[] returns a base 64 representation
    * @param data byte[]
    * @return String
    * @throws IOException
    */
   public static String byteToBase64(byte[] data){
       BASE64Encoder endecoder = new BASE64Encoder();
       return endecoder.encode(data);
   }


}
