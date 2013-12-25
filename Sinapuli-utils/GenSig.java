
/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
 
 
import java.io.*;
import java.security.*;
import java.security.cert.Certificate;
import javax.xml.bind.DatatypeConverter;
 
class GenSig {
 
	public static void main(String[] args) {

		/* Generate a RSA signature */

		if (args.length != 5) {
		    System.out.println("Usage: GenSig nameOfFileToSign keystore password sign publicKey");

		    }
		else try{

		    KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
		    ks.load(new FileInputStream(args[1]), args[2].toCharArray());
		    
		    String alias = (String)ks.aliases().nextElement();
		    PrivateKey privateKey = (PrivateKey) ks.getKey(alias, args[2].toCharArray());

		    Certificate cert = ks.getCertificate(alias);

		    // Get public key	
		    PublicKey publicKey = cert.getPublicKey();

		    /* Create a Signature object and initialize it with the private key */

		    Signature rsa = Signature.getInstance("SHA256withRSA");	
	

		    rsa.initSign(privateKey);

		    /* Update and sign the data */

        	    String hexString = readFile(args[0]).trim();
		    byte[] decodedBytes = DatatypeConverter.parseHexBinary(hexString);	
		    InputStream bufin = new ByteArrayInputStream(decodedBytes);


		    byte[] buffer = new byte[1024];
		    int len;
		    while (bufin.available() != 0) {
			len = bufin.read(buffer);
			rsa.update(buffer, 0, len);
		    };

		    bufin.close();

		    /* Now that all the data to be signed has been read in, 
			    generate a signature for it */

		    byte[] realSig = rsa.sign();

		 
		    /* Save the signature in a file */

		    File file = new File(args[3]);
		    PrintWriter out = new PrintWriter(file);

			// if file doesn't exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}

		    out.println(DatatypeConverter.printBase64Binary(realSig));
		    out.close();


		    /* Save the public key in a file */
		    byte[] key = publicKey.getEncoded();
		    FileOutputStream keyfos = new FileOutputStream(args[4]);
		    keyfos.write(key);

		    keyfos.close();

		} catch (Exception e) {
		    System.err.println("Caught exception " + e.toString());
		}

	}

	public static String readFile(String filename)
	{
		String content = null;
		File file = new File(filename);
		try {
			FileReader reader = new FileReader(file);
			char[] chars = new char[(int) file.length()];
			reader.read(chars);
			content = new String(chars);
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return content;
	}
 
}
