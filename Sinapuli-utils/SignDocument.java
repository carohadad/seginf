import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;

import java.security.PrivateKey;
import java.security.Signature;
import java.security.KeyStore;
 
public class SignDocument {
	public static void main(String[] args) throws Exception {

		String docPath = args[0]; 
		String keystorePath = args[1]; 
		String password = args[2]; 
		String outputPath = args[3]; 

		FileInputStream docFile = new FileInputStream(docPath);
		byte[] doc=new byte[docFile.available()];

		KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
		ks.load(new FileInputStream(keystorePath), password.toCharArray());

		String alias = (String)ks.aliases().nextElement();
		PrivateKey privateKey = (PrivateKey) ks.getKey(alias, password.toCharArray());
	
		// Compute signature
		Signature instance = Signature.getInstance("SHA256withRSA");
		instance.initSign(privateKey);

		instance.update(doc);

		File file = new File(outputPath);

		FileOutputStream fop = new FileOutputStream(file);

		// if file doesn't exists, then create it
		if (!file.exists()) {
			file.createNewFile();
		}

		fop.write(instance.sign());
		fop.flush();
		fop.close();

		System.out.println("Done");
	}
}
