import java.io.*;
import java.nio.file.*;
import java.security.*;

import javax.xml.bind.DatatypeConverter;

import java.util.Arrays;

 
public class HashDocument {

	public static void main(String[] args) throws Exception {

		String docPath = args[0]; 
		String outputPath = args[1]; 

		Path path = Paths.get(docPath);
		byte[] doc = Files.readAllBytes(path); 

		// Digest computation
		byte[] bDigest = getHash(doc);

		File file = new File(outputPath);
		PrintWriter out = new PrintWriter(file);

		// if file doesn't exists, then create it
		if (!file.exists()) {
			file.createNewFile();
		}

		out.println(DatatypeConverter.printHexBinary(bDigest));
		out.close();


		System.out.println("Done");
	}


	public static byte[] getHash(byte[] doc) throws Exception {

		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		digest.reset();

		byte[] input = digest.digest(doc);

		return input;
	}

}
