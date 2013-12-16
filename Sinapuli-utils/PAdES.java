import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;

import java.security.PrivateKey;
import java.security.Signature;
import java.security.KeyStore;
 
public class PAdES {
	public static void main(String[] args) throws Exception {

		String docPath = args[0]; 
		String keystorePath = args[1]; 
		String password = args[2]; 
		String outputPath = args[3]; 


		//pades(byte[] keystore, byte[] pdf, String filename, String password, boolean withTS, boolean withOCSP){  

		//PdfReader reader = new PdfReader(new FileInputStream("EnunciadosTP.pdf"));
		PdfReader reader = new PdfReader(pdf);
		FileOutputStream fout = new FileOutputStream("signed_" + filename);


		//------- firmado ------------------------------------//
		//String KEYSTORE = "../../signer.jks";

		//char[] PASSWORD = "garantito".toCharArray();
		//password = garantito, luego generar interfaz con usuario :P

		//----------------------------------------------------//
		BouncyCastleProvider provider = new BouncyCastleProvider();
		//BouncyCastle, es el security provider
		Security.addProvider(provider);
		KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());

		//ks.load(new FileInputStream(KEYSTORE), PASSWORD);

		ks.load(new ByteArrayInputStream(keystore), password.toCharArray());

		//cargo keystore, y le paso la pass
		String alias = (String)ks.aliases().nextElement();
		PrivateKey pk = (PrivateKey) ks.getKey(alias, password.toCharArray());
		Certificate[] chain = ks.getCertificateChain(alias);

		//-----------------------------------------------
		String digestAlgorithm = DigestAlgorithms.SHA256;
		//creating stamper
		PdfStamper stamper = PdfStamper.createSignature(reader, fout, (char)'\0');
		// Creating the appearance
		PdfSignatureAppearance appearance = stamper.getSignatureAppearance();

		//se le pueden agregar atributos:
		//appearance.setVisibleSignature("mySig");
		//appearance.setReason("Estoy probando para el tp");
		//appearance.setLocation("desde mi pc, man");
		//appearance.setVisibleSignature(new Rectangle(36, 748, 144, 780), 1, "sig");


		//----------------------------------------------
		// Creating the signature
		ExternalDigest digest = new BouncyCastleDigest();
		ExternalSignature signature = new PrivateKeySignature(pk, "SHA256", "BC");
		//BC = BouncyCastle, es el security provider

		// If we add a time stamp:
		TSAClient tsc = null;

		if (withTS) {
			String tsa_url    = "http://localhost:5050/timestamp"
			String tsa_login  = null
			String tsa_passw  = null
			tsc = new TSAClientBouncyCastle(tsa_url, tsa_login, tsa_passw);
		}

		// If we use OCSP:
		OcspClient ocsp = null;
		//Online Certificate Status Protocol (OCSP)
		//Internet protocol used for obtaining the revocation status of an X.509 digital certificate.
		//an alternative to certificate revocation lists (CRL)
		if (withOCSP) {
		ocsp = new OcspClientBouncyCastle();
		}

		MakeSignature.signDetached(appearance, digest, signature, chain, null, ocsp, tsc, 0, CryptoStandard.CMS);
		//CMS = Cryptographic Message Syntax
		
	}
}
