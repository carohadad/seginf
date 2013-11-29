import static ratpack.groovy.Groovy.*
import org.bouncycastle.tsp.*
import java.security.KeyStore

import garantito.tsa.TSAModule

ratpack {
  handlers {

    get("get_timestamp") {

      def reqGen = new TimeStampRequestGenerator()
      def request = reqGen.generate(TSPAlgorithms.SHA1, new byte[20])


      TSAModule tsa = new TSAModule(loadKeyStore())
      tsa.validate(request)

      def resp = tsa.generate(request)

      def encodedResponse = tsa.encode(resp)

      response.contentType("application/timestamp-reply")
      response.send "${encodedResponse}"
    }

    post("timestamp") {

      rawRequest = request.getInputStream()

      def tsaRequest = new TimeStampRequest(rawRequest)

      TSAModule tsa = new TSAModule(loadKeyStore())
      tsa.validate(tsaRequest)

      def resp = tsa.generate(tsaRequest)

      def encodedResponse = tsa.encode(resp)

      response.contentType("application/timestamp-reply")
      response.headers.add("Content-Encoding", "base64")

      response.send "${encodedResponse}"
    }
  }
}

def loadKeyStore() {
  def keyStore = KeyStore.getInstance("JKS")
  keyStore.load(new File('../../tsa.jks').newInputStream(), 'garantito'.toCharArray())
  keyStore
}


