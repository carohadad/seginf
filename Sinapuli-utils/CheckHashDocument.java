import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileReader;


import java.security.MessageDigest;
import java.security.SecureRandom;

import javax.xml.bind.DatatypeConverter;

import java.util.Arrays;

 
public class CheckHashDocument {

	public static void main(String[] args) throws Exception {

		String docPath = args[0]; 
		String hashDocPath = args[1]; 

		FileInputStream docFile = new FileInputStream(docPath);
		byte[] doc=new byte[docFile.available()];

                //FileInputStream hashDocFile = new FileInputStream(hashDocPath);
		//byte[] hashDoc=new byte[hashDocFile.available()];
		//hashDocFile.read(hashDoc);

		byte[] hashDoc = DatatypeConverter.parseBase64Binary(readFile(hashDocPath));

		// Compute the new DIGEST
		byte[] proposedDigest = getHash(doc);

		System.out.println("¿El Hash se corresponde con el documento?");
		System.out.println(Arrays.equals(proposedDigest, hashDoc));
	}


	public static byte[] getHash(byte[] doc) throws Exception {

		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		digest.reset();

		byte[] input = digest.digest(doc);

		return input;
	}

	public static String readFile(String filename)
	{
		String content = null;
		File file = new File(filename);
		try {
			FileReader reader = new FileReader(file);
			char[] chars = new char[(int) file.length()];
			reader.read(chars);
			content = new String(chars);
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return content;
	}

}
