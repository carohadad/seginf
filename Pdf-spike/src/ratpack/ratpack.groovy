//@GrabResolver("http://jcenter.bintray.com")
//@Grab("pdfbox:pdfbox:0.7.3")
//@Grab("xhtmlrenderer:xhtmlrenderer")
//@Grab("itext:itext:2.0.8")
//@Grab("jtidy:jtidy")

import static ratpack.groovy.Groovy.*
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
//import com.itextpdf.text.pdf.security.TSAClientBouncyCastle;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;

/*
import org.pdfbox.exceptions.*
import org.pdfbox.pdmodel.PDDocument
import org.pdfbox.pdmodel.PDPage
import org.pdfbox.pdmodel.edit.PDPageContentStream
import org.pdfbox.pdmodel.font.PDFont
import org.pdfbox.pdmodel.font.PDType1Font

import java.io.*
import org.xhtmlrenderer.pdf.ITextRenderer

import org.w3c.tidy.Tidy
import org.w3c.dom.Document;
*/
// You can change anything in the ratpack {} closure without needing to restart

ratpack {
    handlers {
        get {
            render groovyTemplate("html2pdf.html")
        }

	get("pades") {
		//render groovyTemplate("pades.html")
		pades()
		render groovyTemplate("pdf.html")
        }
	/*
	
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
			contentStream.drawString( "any shit" );
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
	*/
	post("html2pdf"){

		html2pdf(request.form.url)
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

	def command = ["phantomjs", 
			"/usr/local/share/phantomjs/examples/rasterize.js", 
			url, "salida.pdf"]

	def proc = command.execute()                 // Call *execute* on the string
	proc.waitFor()                               // Wait for the command to finish

	// Obtain status and output
	println "return code: ${ proc.exitValue()}"
	println "stderr: ${proc.err.text}"
	println "stdout: ${proc.in.text}" // *out* from the external program is *in* for groovy


}


public void pades(){

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
	String KEYSTORE = "ks";
	char[] PASSWORD = "garantito".toCharArray();//password = garantito
	BouncyCastleProvider provider = new BouncyCastleProvider();
	Security.addProvider(provider);
	KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
	ks.load(new FileInputStream(KEYSTORE), PASSWORD);
	String alias = (String)ks.aliases().nextElement();
	PrivateKey pk = (PrivateKey) ks.getKey(alias, PASSWORD);
	Certificate[] chain = ks.getCertificateChain(alias);
	String digestAlgorithm = DigestAlgorithms.SHA256;
	
	/*
	sign(String src, String name, String dest, Certificate[] chain,
	  PrivateKey pk, String digestAlgorithm, String provider,
	  CryptoStandard subfilter, String reason, String location)

	sign(SRC, String.format(DEST, 4), chain, pk,
	DigestAlgorithms.RIPEMD160, provider.getName(), CryptoStandard.CADES,
	"Test 4", "Ghent");
	*/

	PdfStamper stamper = PdfStamper.createSignature(reader, fout, (char)'\0');
	// Creating the appearance
	PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
	//ver si hace falta
	//appearance.setReason("Test");
	//appearance.setLocation(location);
	//appearance.setVisibleSignature(name);
	//----------------------------------------------
	// Creating the signature
	ExternalDigest digest = new BouncyCastleDigest();
	//ExternalSignature signature = new PrivateKeySignature(pk, digestAlgorithm, provider);
	ExternalSignature signature = new PrivateKeySignature(pk, "SHA-256", "BC");
	MakeSignature.signDetached(appearance, digest, signature, chain, null, null, null, 0, CryptoStandard.CMS);



	println "hello pades!"
}

