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

    get("pades") {
      pades(true, true) //TODO: levantar estos boolean de un check
      render groovyTemplate("pdf.html")
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

    post("pades"){

      def f = context.parse(form())
      //html2pdf(f.url)
	println "entro al post de pades"
      render groovyTemplate("pdf.html")
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
   }
   finally
   {
   if( os != null )
   {		
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


//public void pades(String src, String dest, boolean withTS, boolean withOCSP)
public void pades(boolean withTS, boolean withOCSP){

  PdfReader reader = new PdfReader(new FileInputStream("EnunciadosTP.pdf"));
  FileOutputStream fout = new FileOutputStream("EnunciadosTP_signed_2.pdf");

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
  String KEYSTORE = "../../signer.jks";

  char[] PASSWORD = "garantito".toCharArray();
  //password = garantito, luego generar interfaz con usuario :P

  BouncyCastleProvider provider = new BouncyCastleProvider();
  //BouncyCastle, es el security provider
  Security.addProvider(provider);
  KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
  //keystore generada con:
  //keytool -genkeypair -alias sha256 -keyalg RSA -keysize 2048 -sigalg SHA256withRSA -keystore ks
  ks.load(new FileInputStream(KEYSTORE), PASSWORD);
  //cargo keystore, y le paso la pass
  String alias = (String)ks.aliases().nextElement();
  PrivateKey pk = (PrivateKey) ks.getKey(alias, PASSWORD);
  Certificate[] chain = ks.getCertificateChain(alias);
  String digestAlgorithm = DigestAlgorithms.SHA256;

  //creating stamper
  PdfStamper stamper = PdfStamper.createSignature(reader, fout, (char)'\0');
  // Creating the appearance
  PdfSignatureAppearance appearance = stamper.getSignatureAppearance();

  //se le pueden agregar atributos:
  //appearance.setVisibleSignature("mySig");
  appearance.setReason("Estoy probando para el tp");
  appearance.setLocation("desde mi pc, man");
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

