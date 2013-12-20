import static ratpack.groovy.Groovy.*
import static ratpack.form.Forms.form
import org.bouncycastle.tsp.*
import java.security.KeyStore

import garantito.tsa.*

def loadKeyStore() {
  def keyStore = KeyStore.getInstance("JKS")
  keyStore.load(new File('tsa.jks').newInputStream(), 'garantito'.toCharArray())
  keyStore
}

ratpack {
  modules {
    bind SerialNumberGenerator, new SerialNumberGeneratorImpl(new File('tsa.srl'))
    bind KeyStore, loadKeyStore()
    bind KeyProvider
    bind TSAModule
  }

  handlers {
    get {
      render groovyTemplate("index.html")
    }

    post('token') { TSAModule tsa ->
      def form = parse(form())

      if (form.hash == null || form.hash.trim() == "" || form.hash.trim().size() != 64) {
        response.status 400
        response.send 'text/plain', 'El hash es inválido'
        return
      }

      def reqGen = new TimeStampRequestGenerator()
      reqGen.certReq = true
      def nonce = BigInteger.valueOf(System.currentTimeMillis())
      def tsq = reqGen.generate(TSPAlgorithms.SHA256, form.hash.trim().decodeHex(), nonce)

      tsa.validate(tsq)

      def tsr = tsa.generate(tsq)

      response.send "text/plain", tsr.encoded.encodeBase64(true).toString()
    }

    post('show_tsr') { TSAModule tsa ->
      def form = parse(form())

      if (form.tsr == null || form.tsr.trim() == "") {
        response.status 400
        response.send 'text/plain', 'El TSR es inválido'
        return
      }

      def tsrB64 = form.tsr.replaceAll(/\s+/, '').decodeBase64()
      def tsr = new TimeStampResponse(tsrB64)

      render groovyTemplate("show_tsr.html", "text/html", tsr: tsr)
    }

    post("timestamp") { TSAModule tsa ->
      try {
        def inputStream = request.getInputStream()
        def requestBytes = inputStream.bytes

        def tsaRequest = new TimeStampRequest(requestBytes)

        tsa.validate(tsaRequest)

        println "Generando TSR para ${tsaRequest.messageImprintDigest.encodeHex()}"

        def resp = tsa.generate(tsaRequest)
        def encodedResponse = tsa.encode(resp)

        response.contentType("application/timestamp-reply")
        response.headers.add("Content-Encoding", "base64")

        response.send "${encodedResponse}"
      } catch (Exception e) {
        def writer = new StringWriter()
        response.status 500, e.message
        e.printStackTrace(new PrintWriter(writer))
        e.printStackTrace()
        response.send "text/plain", writer.toString()
      }
    }
  }
}

