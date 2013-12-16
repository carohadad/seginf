import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;

import java.security.MessageDigest;
import java.security.SecureRandom;

import java.util.Arrays;

 
public class CheckHashDocument {

	public static void main(String[] args) throws Exception {

		int ITERATION_NUMBER = 1000;

		String docPath = args[0]; 
		String hashDocPath = args[1]; 

		FileInputStream docFile = new FileInputStream(docPath);
		byte[] doc=new byte[docFile.available()];

		FileInputStream hashDocFile = new FileInputStream(hashDocPath);
		byte[] hashDoc=new byte[hashDocFile.available()];

		//byte[] bDigest = base64ToByte(obj.password);
		//byte[] bSalt = base64ToByte(obj.salt);

		// Compute the new DIGEST
		byte[] proposedDigest = getHash(ITERATION_NUMBER, password, bSalt);
		//return proposedDigest == bDigest

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
