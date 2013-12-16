

public class Html2Pdf {
	public static void main(String[] args) throws Exception {

		String url = args[0]; 
		String outputPath = args[1]; 

		final Runtime rt = Runtime.getRuntime();
		rt.exec("phantomjs /usr/local/share/phantomjs/examples/rasterize.js " + url + " " + outputPath);

	}
	
}
