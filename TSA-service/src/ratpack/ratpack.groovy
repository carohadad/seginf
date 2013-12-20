import static ratpack.groovy.Groovy.*
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
    get("get_timestamp") { TSAModule tsa ->

      def reqGen = new TimeStampRequestGenerator()
      def request = reqGen.generate(TSPAlgorithms.SHA256, new byte[32])

      tsa.validate(request)

      def resp = tsa.generate(request)
      def encodedResponse = tsa.encode(resp)

      response.contentType("application/timestamp-reply")
      response.send "${encodedResponse}"
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

