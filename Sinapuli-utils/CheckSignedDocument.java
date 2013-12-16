import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.BufferedInputStream;

import java.security.PublicKey;
import java.security.Signature;
import java.security.KeyStore;
import java.security.KeyFactory;
//import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

 
public class CheckSignedDocument {
	public static void main(String[] args) throws Exception {

		String docPath = args[0]; 
		String signedPath = args[1]; 
		String publicKeyPath = args[2]; 
		
		/*
		FileInputStream keyfis = new FileInputStream(publicKeyPath);
		byte[] encKey = new byte[keyfis.available()];  
		keyfis.read(encKey);

		keyfis.close();

		X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(encKey);
		
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		PublicKey pubKey = keyFactory.generatePublic(pubKeySpec);
		*/
		
		
		PublicKey pubKey = readPublicKeyFromFile(publicKeyPath);

		Signature sig = Signature.getInstance("SHA256withRSA");
		sig.initVerify(pubKey);
		

		/*
		FileInputStream docFile = new FileInputStream(docPath);
		byte[] doc=new byte[docFile.available()];

		sig.update(doc);
		*/

		FileInputStream datafis = new FileInputStream(docPath);
            	BufferedInputStream bufin = new BufferedInputStream(datafis);
 
		byte[] buffer = new byte[1024];
		int len;
		while (bufin.available() != 0) {
			len = bufin.read(buffer);
			sig.update(buffer, 0, len);
		};

		bufin.close();


		FileInputStream signedFile = new FileInputStream(signedPath);
		byte[] sigToVerify =new byte[signedFile.available()];
		signedFile.read(sigToVerify);
		signedFile.close();

		boolean verifies = sig.verify(sigToVerify);	

		System.out.println("¿Esta firmado correctamente? ");
		System.out.println(verifies);
	}

	public static PublicKey readPublicKeyFromFile(String publicKeyPath) throws Exception{  
		
		FileInputStream keyfis = new FileInputStream(publicKeyPath);
		byte[] encKey = new byte[keyfis.available()];  
		keyfis.read(encKey);
		keyfis.close();
	
		X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(encKey);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		PublicKey publicKey = keyFactory.generatePublic(pubKeySpec);
		return publicKey;  
		
		/*
		FileInputStream certFileIs = new FileInputStream(publicKeyPath);
      		CertificateFactory cf = CertificateFactory.getInstance("X509");
		X509Certificate cert = (X509Certificate) cf.generateCertificate(certFileIs);
		return cert.getPublicKey();
		*/
		/*
		File keyFile = new File(publicKeyPath);
		byte[] encodedKey = new byte[(int)keyFile.length()];

		new FileInputStream(keyFile).read(encodedKey);

		X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(encodedKey);

		KeyFactory kf = KeyFactory.getInstance("RSA");
		PublicKey pk = kf.generatePublic(publicKeySpec);
		return pk; 
		*/		     
	} 
}
