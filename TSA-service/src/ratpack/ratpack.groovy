import static ratpack.groovy.Groovy.*
import org.bouncycastle.tsp.*
import java.security.KeyStore

import garantito.tsa.TSAModule

def loadKeyStore() {
  def keyStore = KeyStore.getInstance("JKS")
  keyStore.load(new File('tsa.jks').newInputStream(), 'garantito'.toCharArray())
  keyStore
}

ratpack {
  modules {
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
      def inputStream = request.getInputStream()
      def requestBytes = inputStream.bytes

      def tsaRequest = new TimeStampRequest(requestBytes)

      tsa.validate(tsaRequest)

      def resp = tsa.generate(tsaRequest)
      def encodedResponse = tsa.encode(resp)

      response.contentType("application/timestamp-reply")
      response.headers.add("Content-Encoding", "base64")

      response.send "${encodedResponse}"
    }
  }
}

