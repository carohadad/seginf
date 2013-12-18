import java.io.*;
import java.security.*;
import java.security.cert.Certificate;
 
class SinapuliTokenRead {
 
    public static void main(String[] args) {
 
        /* Generate a RSA signature */
 
        if (args.length != 3) {
            System.out.println("Usage: Sinapuli sinapuliToken hashOutput signOutput");

        }
        else try{

		String sinapuliTokenPath = args[0]; 
		String hashOutputPath = args[1];
	        String signOutputPath = args[2];
 
	    //----------------------------------------------------------------------- 	
	    //Read Token	

	        FileInputStream tokenFile = new FileInputStream(sinapuliTokenPath);
		byte[] token=new byte[tokenFile.available()];
		tokenFile.read(token);

	    //Copy hash

		byte[] hash=new byte[32];
		System.arraycopy(token, 0, hash, 0, 32);

	    //Copy sign

		byte[] sign = java.util.Arrays.copyOfRange(token, 32, token.length);

	    //-----------------------------------------------------------------------
	    //Export publicKey	

		/* Save the hash in a file */
		
		FileOutputStream hashfos = new FileOutputStream(hashOutputPath);
		hashfos.write(hash);
		hashfos.close();
		
		/* Save the sign in a file */

		FileOutputStream signfos = new FileOutputStream(signOutputPath);
		signfos.write(sign);
		signfos.close();

 
        } catch (Exception e) {
            System.err.println("Caught exception " + e.toString());
        }
 
    };
 

}
