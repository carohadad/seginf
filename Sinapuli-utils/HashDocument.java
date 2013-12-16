import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;

import java.security.MessageDigest;
import java.security.SecureRandom;

import java.util.Arrays;

 
public class HashDocument {

	public static void main(String[] args) throws Exception {

		int ITERATION_NUMBER = 1000;

		String docPath = args[0]; 
		String outputPath = args[1]; 

		FileInputStream docFile = new FileInputStream(docPath);
		byte[] doc=new byte[docFile.available()];

		// Uses a secure Random not a simple Random
		SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
		// Salt generation 64 bits long
		byte[] bSalt = new byte[8];
		random.nextBytes(bSalt);
		// Digest computation
		byte[] bDigest = getHash(ITERATION_NUMBER, doc, bSalt);

		File file = new File(outputPath);

		FileOutputStream fop = new FileOutputStream(file);

		// if file doesn't exists, then create it
		if (!file.exists()) {
			file.createNewFile();
		}

		fop.write(bDigest);
		fop.flush();
		fop.close();

		System.out.println("Done");
	}


	public static byte[] getHash(int iterationNb, byte[] doc, byte[] salt) throws Exception {
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		digest.reset();
		digest.update(salt);
		byte[] input = digest.digest(doc);
		for (int i = 0; i < iterationNb; i++) {
			digest.reset();
			input = digest.digest(input);
		}
		return input;
	}

}
