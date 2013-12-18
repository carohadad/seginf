import java.io.*;
import java.security.*;
import java.security.cert.Certificate;
 
class SinapuliToken {
 
    public static void main(String[] args) {
 
        /* Generate a RSA signature */
 
        if (args.length != 5) {
            System.out.println("Usage: Sinapuli doc output keystore password publicKey");

        }
        else try{

		String docPath = args[0]; 
		String outputPath = args[1]; 
		String keystorePath = args[2]; 
		String password = args[3]; 
		String publicKeyPath = args[4]; 
	    //-----------------------------------------------------------------------
            //Hash Doc

		FileInputStream docFile = new FileInputStream(docPath);
		byte[] doc=new byte[docFile.available()];

		// Digest computation
		byte[] bDigest = getHash(doc);

	    //-----------------------------------------------------------------------             		
	    //Sign Doc

		KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
		ks.load(new FileInputStream(keystorePath), password.toCharArray());

		String alias = (String)ks.aliases().nextElement();
		PrivateKey privateKey = (PrivateKey) ks.getKey(alias, password.toCharArray());

		Certificate cert = ks.getCertificate(alias);

		// Get public key	
		PublicKey publicKey = cert.getPublicKey();

		/* Create a Signature object and initialize it with the private key */

		Signature rsa = Signature.getInstance("SHA256withRSA");	


		rsa.initSign(privateKey);

		/* Update and sign the data */

		FileInputStream fis = new FileInputStream(docPath);
		BufferedInputStream bufin = new BufferedInputStream(fis);
		byte[] buffer = new byte[1024];
		int len;
		while (bufin.available() != 0) {
			len = bufin.read(buffer);
			rsa.update(buffer, 0, len);
		};

		bufin.close();

		/* Now that all the data to be signed has been read in, 
		    generate a signature for it */

		byte[] realSig = rsa.sign();
 

	    //----------------------------------------------------------------------- 	
	    //Build Token	

	  
         
		/* Save the token in a file */
		FileOutputStream sinapulifos = new FileOutputStream(outputPath);
		sinapulifos.write(bDigest);
		sinapulifos.write(realSig);

		sinapulifos.close();


	    //-----------------------------------------------------------------------
	    //Export publicKey	

		/* Save the public key in a file */
		byte[] key = publicKey.getEncoded();
		FileOutputStream keyfos = new FileOutputStream(publicKeyPath);
		keyfos.write(key);

		keyfos.close();

 
        } catch (Exception e) {
            System.err.println("Caught exception " + e.toString());
        }
 
    };
 

	public static byte[] getHash(byte[] doc) throws Exception {

		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		digest.reset();

		byte[] input = digest.digest(doc);

		return input;
	}

}
