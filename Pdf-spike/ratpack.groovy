@GrabResolver("https://oss.jfrog.org/artifactory/repo")
//@GrabResolver("http://jcenter.bintray.com")
@Grab("io.ratpack:ratpack-groovy:0.9.0-SNAPSHOT")
//@Grab("pdfbox:pdfbox:0.7.3")
//@Grab("xhtmlrenderer:xhtmlrenderer")
//@Grab("itext:itext:2.0.8")
//@Grab("jtidy:jtidy")

import static ratpack.groovy.Groovy.*

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

