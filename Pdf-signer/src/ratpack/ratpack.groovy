import static ratpack.groovy.Groovy.*
import static ratpack.form.Forms.form

import java.io.*;
import java.security.*;
import java.security.cert.Certificate;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.security.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import com.itextpdf.text.pdf.security.MakeSignature.CryptoStandard;

ratpack {

  handlers {
    get {
      render groovyTemplate("index.html")
    }

    /*	
    get ("download"){
	println "download"

	FileInputStream downloadFile = new FileInputStream("download.pdf");
	byte[] download=new byte[downloadFile.available()];
	downloadFile.read(download);

	response.send(download);
        return
    }

    post("html2pdf"){

      def f = context.parse(form())
      html2pdf(f.url)
      render groovyTemplate("pdf.html")
    }
    */


    post("html2pdf"){

	def f = context.parse(form())
	html2pdf(f.url)

	FileInputStream downloadFile = new FileInputStream("download.pdf");
	byte[] download=new byte[downloadFile.available()];
	downloadFile.read(download);

	response.send(download);
	return	
    }

    assets "public"
  }

}


public void html2pdf(String url){

  def command = ["phantomjs", "/usr/local/share/phantomjs/examples/rasterize.js", url, "html2pdf.pdf"]

  def proc = command.execute()                 // Call *execute* on the string
  proc.waitFor()                               // Wait for the command to finish

  // Obtain status and output
  println "return code: ${ proc.exitValue()}"
  println "stderr: ${proc.err.text}"
  println "stdout: ${proc.in.text}" // *out* from the external program is *in* for groovy

  pades(true, true)
}


public void pades(boolean withTS, boolean withOCSP){

  PdfReader reader = new PdfReader(new FileInputStream("html2pdf.pdf"));
  FileOutputStream fout = new FileOutputStream("download.pdf");

  //------- firmado ------------------------------------//
  String KEYSTORE = "../../signer.jks";
  char[] PASSWORD = "garantito".toCharArray();
  
  //----------------------------------------------------//
  BouncyCastleProvider provider = new BouncyCastleProvider();
  //BouncyCastle, es el security provider
  Security.addProvider(provider);
  KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
  
  
  ks.load(new FileInputStream(KEYSTORE), PASSWORD);
  
  
  //cargo keystore, y le paso la pass
  String alias = (String)ks.aliases().nextElement();
  PrivateKey pk = (PrivateKey) ks.getKey(alias, PASSWORD);  
  Certificate[] chain = ks.getCertificateChain(alias);

  //-----------------------------------------------
  String digestAlgorithm = DigestAlgorithms.SHA256;
  //creating stamper
  PdfStamper stamper = PdfStamper.createSignature(reader, fout, (char)'\0');
  // Creating the appearance
  PdfSignatureAppearance appearance = stamper.getSignatureAppearance();

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
