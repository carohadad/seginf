//@GrabResolver("http://jcenter.bintray.com")
//@Grab("pdfbox:pdfbox:0.7.3")
//@Grab("xhtmlrenderer:xhtmlrenderer")
//@Grab("itext:itext:2.0.8")
//@Grab("jtidy:jtidy")

import static ratpack.groovy.Groovy.*
import static ratpack.form.Forms.form

import java.io.*;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfFormField;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfAnnotation;
import com.itextpdf.text.pdf.security.ExternalSignature;
import com.itextpdf.text.pdf.security.PrivateKeySignature;
import com.itextpdf.text.pdf.security.BouncyCastleDigest;
import com.itextpdf.text.pdf.security.MakeSignature;
import com.itextpdf.text.pdf.security.MakeSignature.CryptoStandard;
import com.itextpdf.text.pdf.security.ExternalDigest;
import com.itextpdf.text.pdf.security.DigestAlgorithms;

import com.itextpdf.text.pdf.security.OcspClient;
import com.itextpdf.text.pdf.security.OcspClientBouncyCastle;
import com.itextpdf.text.pdf.security.TSAClient;
import com.itextpdf.text.pdf.security.TSAClientBouncyCastle;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.Signature;
import java.security.spec.RSAPublicKeySpec;
import java.security.KeyFactory;
import java.security.PublicKey;

import org.pdfbox.exceptions.*
import org.pdfbox.pdmodel.PDDocument
import org.pdfbox.pdmodel.PDPage
import org.pdfbox.pdmodel.edit.PDPageContentStream
import org.pdfbox.pdmodel.font.PDFont
import org.pdfbox.pdmodel.font.PDType1Font

/*
import java.io.*
import org.xhtmlrenderer.pdf.ITextRenderer
import org.w3c.tidy.Tidy
import org.w3c.dom.Document;
*/

// You can change anything in the ratpack {} closure without needing to restart

ratpack {

  handlers {
    get {
      render groovyTemplate("index.html")
    }

    /*
    get("pades") {
      pades(true, true) //TODO: levantar estos boolean de un check
      render groovyTemplate("pdf.html")
    }
    */
    
    get("getPublicKey") {
      def publicKey = getPublicKey()
      render groovyTemplate("index.html", publicKey: publicKey)     
    }
    
    get("generatePDF") {

      def document = null
      def page = null
      def contentStream = null
      def font = null

      try{
        document = new PDDocument()
        page = new PDPage()
        document.addPage(page)

        font = PDType1Font.HELVETICA_BOLD;

        contentStream = new PDPageContentStream(document, page);
        contentStream.beginText();
        contentStream.setFont( font, 12 );
        contentStream.moveTextPositionByAmount( 100, 700 );
        contentStream.drawString( "Hola mundo!" );
        contentStream.endText();
        contentStream.close();

        document.save("miPDF")
      }
      finally
      {
        if( document != null )
        {
          document.close();
          render groovyTemplate("pdf.html")
        }
      }
    }

    post("html2pdf"){

      def f = context.parse(form())
      html2pdf(f.url)
      render groovyTemplate("pdf.html")
    }

    post("pdfSignature"){

      def f = context.parse(form())
      def pdf = f.file('pdfFile').getText()

      byte[] signature = pdfSignature(pdf)

      render groovyTemplate("index.html", signature: signature)     
    }

    post("pades"){
      
      def f = context.parse(form())

      
      if( f.file('keystoreFile').getBytes() != null &&
            f.password?.trim() &&
            f.file('pdfFile').getBytes() != null) {

          def keystore = f.file('keystoreFile').getBytes()
          def password = f.password

          def pdf = f.file('pdfFile').getBytes()
          def filename = f.file('pdfFile').fileName

          
          pades(keystore, pdf, filename, password, false, false)

          render groovyTemplate("pdf.html")
      } else {
          render groovyTemplate("index.html", error: "Debe ingresar los tres campos")     
      }
      
    }


    post("checkHash"){

      def f = context.parse(form())
      def pdf = f.file('pdfFile').getBytes()
      def hash = f.hash.getBytes()
      def result = checkHash(hash, pdf)	

      render groovyTemplate("index.html", result: result)     

    }

    assets "public"
  }

}

/*
   public void html2pdf(String url){

	   String cleanFile = "cleaned.html"
	   OutputStream cleanOS = new FileOutputStream(cleanFile)

	   InputStream is = new URL(url).openStream();

	   String outputFile = "html.pdf"
	   OutputStream os = new FileOutputStream(outputFile)

	   Tidy tidy = new Tidy();	

	   try{		

		   tidy.setXHTML(true);
		   tidy.setMakeClean(true);

		   Document converted = tidy.parseDOM(is,cleanOS)		

		   ITextRenderer renderer = new ITextRenderer()		
		   renderer.setDocument("cleaned.html")

		   renderer.layout()
		   renderer.createPDF(os)
	} finally {

		   if( os != null ){		
			   os.close();			
		   }
	}

   }
 */

public void html2pdf(String url){

  def command = ["phantomjs", "/usr/local/share/phantomjs/examples/rasterize.js", url, "html2pdf.pdf"]

  def proc = command.execute()                 // Call *execute* on the string
  proc.waitFor()                               // Wait for the command to finish

  // Obtain status and output
  println "return code: ${ proc.exitValue()}"
  println "stderr: ${proc.err.text}"
  println "stdout: ${proc.in.text}" // *out* from the external program is *in* for groovy
}

public boolean checkHash(byte[] sigToVerify, /*publiKey,*/ byte[] pdf){

	Signature sig = Signature.getInstance("SHA256withRSA");

	//cambiar por lo que venga en el form
	PublicKey pubKey = readPublicKeyFromFile("../../public.key");
	sig.initVerify(pubKey); 
	
	sig.update(pdf);

	boolean verifies = sig.verify(sigToVerify);

	return verifies
}

public String getPublicKey(){

	// Levantar una key
	String KEYSTORE = "../../signer.jks";
	char[] PASSWORD = "garantito".toCharArray();

	KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
	ks.load(new FileInputStream(KEYSTORE), PASSWORD);

	String alias = (String)ks.aliases().nextElement();
	PrivateKey privateKey = (PrivateKey) ks.getKey(alias, PASSWORD);

	// Get certificate of public key
	Certificate cert = ks.getCertificate(alias);
	PublicKey publicKey = cert.getPublicKey();

	//la grabo en un archivo	
	//---
	KeyFactory keyFactory = KeyFactory.getInstance("RSA");
	RSAPublicKeySpec rsaPubKeySpec = keyFactory.getKeySpec(publicKey, RSAPublicKeySpec.class);  

	saveKeys("../../public.key", rsaPubKeySpec.getModulus(), rsaPubKeySpec.getPublicExponent());  
	//---
	//Alternativa: 
	/*
		byte[] key = publicKey.getEncoded();
		FileOutputStream keyfos = new FileOutputStream("public");
		keyfos.write(key);
		keyfos.close();

	*/

	return publicKey.toString();
}



private void saveKeys(String fileName,BigInteger mod,BigInteger exp) throws IOException{  
	FileOutputStream fos = null;  
	ObjectOutputStream oos = null;  
	  
	try {  
	    System.out.println("Generating "+fileName + "...");  
	    fos = new FileOutputStream(fileName);  
	    oos = new ObjectOutputStream(new BufferedOutputStream(fos));  
	      
	    oos.writeObject(mod);  
	    oos.writeObject(exp);             
	      
	    System.out.println(fileName + " generated successfully");  
	} catch (Exception e) {  
	    e.printStackTrace();  
	}  
	finally{  
	    if(oos != null){  
		oos.close();  
		  
		if(fos != null){  
		    fos.close();  
		}  
	    }  
	}         
}  


public PublicKey readPublicKeyFromFile(String fileName) throws IOException{  
	FileInputStream fis = null;  
	ObjectInputStream ois = null;  

	try {  
	    fis = new FileInputStream(new File(fileName));  
	    ois = new ObjectInputStream(fis);  
	      
	    BigInteger modulus = (BigInteger) ois.readObject();  
	    BigInteger exponent = (BigInteger) ois.readObject();  
	      
	    //Get Public Key  
	    RSAPublicKeySpec rsaPublicKeySpec = new RSAPublicKeySpec(modulus, exponent);  
	    KeyFactory fact = KeyFactory.getInstance("RSA");  
	    PublicKey publicKey = fact.generatePublic(rsaPublicKeySpec);  
		          
	    return publicKey;  
	      
	} catch (Exception e) {  
	    e.printStackTrace();  
	}  
	finally{  
	    if(ois != null){  
		ois.close();  
		if(fis != null){  
		    fis.close();  
		}  
	    }  
	}  

	return null;  
} 


public byte[] pdfSignature(String pdf){

	// Levantar una key
	String KEYSTORE = "../../signer.jks";
	char[] PASSWORD = "garantito".toCharArray();

	KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
	ks.load(new FileInputStream(KEYSTORE), PASSWORD);

	String alias = (String)ks.aliases().nextElement();
	PrivateKey privateKey = (PrivateKey) ks.getKey(alias, PASSWORD);

	
	// Compute signature
	Signature instance = Signature.getInstance("SHA256withRSA");
	instance.initSign(privateKey);

	instance.update((pdf).getBytes()); //ver lo de getText getByte

	//byte[] signature = instance.sign();
	return instance.sign();

}


//public void pades(boolean withTS, boolean withOCSP){
public void pades(byte[] keystore, byte[] pdf, String filename, String password, boolean withTS, boolean withOCSP){  

  //PdfReader reader = new PdfReader(new FileInputStream("EnunciadosTP.pdf"));
  PdfReader reader = new PdfReader(pdf);
  FileOutputStream fout = new FileOutputStream("signed_" + filename);

  //------- Creo un area para el firmado -----------------//
  /*
     PdfStamper stp = new PdfStamper(reader, fout);
  //PdfStamper stp = PdfStamper.createSignature(reader, fout, (char)'\0', null, true);
  // create a signature form field
  PdfFormField field = PdfFormField.createSignature(stp.getWriter());
  field.setFieldName("SIGNAME");
  // set the widget properties
  field.setWidget(new Rectangle(72, 732, 144, 780),PdfAnnotation.HIGHLIGHT_OUTLINE);
  field.setFlags(PdfAnnotation.FLAGS_PRINT);
  // add the annotation
  stp.addAnnotation(field, 1);
  // close the stamper
  stp.close();
   */

  //------- firmado ------------------------------------//
  //String KEYSTORE = "../../signer.jks";

  //char[] PASSWORD = "garantito".toCharArray();
  //password = garantito, luego generar interfaz con usuario :P

  //----------------------------------------------------//
  BouncyCastleProvider provider = new BouncyCastleProvider();
  //BouncyCastle, es el security provider
  Security.addProvider(provider);
  KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
  //keystore generada con:
  //keytool -genkeypair -alias sha256 -keyalg RSA -keysize 2048 -sigalg SHA256withRSA -keystore ks
  
  //ks.load(new FileInputStream(KEYSTORE), PASSWORD);
  
  //-- asco!!!!
  FileOutputStream fos = new FileOutputStream("keystore.tmp");
  fos.write(keystore);
  fos.close();
  ks.load(new FileInputStream("keystore.tmp"), password.toCharArray());
  //**--
  

  //cargo keystore, y le paso la pass
  String alias = (String)ks.aliases().nextElement();
  //PrivateKey pk = (PrivateKey) ks.getKey(alias, PASSWORD);
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
    //String tsa_url    = "http://localhost:5050/timestamp"
    //String tsa_login  = null
    //String tsa_passw  = null
    String tsa_url    = "http://ca.signfiles.com/tsa/get.aspx"
    String tsa_login  = "garantito"
    String tsa_passw  = "garantito"
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

  //MakeSignature.signDetached(appearance, digest, signature, chain, null, null, null, 0, CryptoStandard.CMS); 
  MakeSignature.signDetached(appearance, digest, signature, chain, null, ocsp, tsc, 0, CryptoStandard.CMS);
  //CMS = Cryptographic Message Syntax

}
