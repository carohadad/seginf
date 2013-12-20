import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;

import java.security.MessageDigest;
import java.security.SecureRandom;

import javax.xml.bind.DatatypeConverter;

import java.util.Arrays;

 
public class HashDocument {

	public static void main(String[] args) throws Exception {

		String docPath = args[0]; 
		String outputPath = args[1]; 

		FileInputStream docFile = new FileInputStream(docPath);
		byte[] doc=new byte[docFile.available()];

		// Digest computation
		byte[] bDigest = getHash(doc);

		File file = new File(outputPath);

		//FileOutputStream fop = new FileOutputStream(file);
		PrintWriter out = new PrintWriter(file);

		// if file doesn't exists, then create it
		if (!file.exists()) {
			file.createNewFile();
		}

		out.println(DatatypeConverter.printBase64Binary(bDigest));
		out.close();

		//fop.write(bDigest);		
		//fop.flush();
		//fop.close();

		System.out.println("Done");
	}


	public static byte[] getHash(byte[] doc) throws Exception {

		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		digest.reset();

		byte[] input = digest.digest(doc);

		return input;
	}

}
