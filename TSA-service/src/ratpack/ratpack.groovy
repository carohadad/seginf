import static ratpack.groovy.Groovy.*
import org.bouncycastle.tsp.*

import garantito.tsa.TSAModule

ratpack {
  handlers {

    get("get_timestamp") {

      def reqGen = new TimeStampRequestGenerator()
      def request = reqGen.generate(TSPAlgorithms.SHA1, new byte[20])


      TSAModule tsa = new TSAModule()
      tsa.validate(request)

      def resp = tsa.generate(request)

      def encodedResponse = tsa.encode(resp)

      response.contentType("application/timestamp-reply")
      response.send "${encodedResponse}"
    }

    post("timestamp") {

      rawRequest = request.getInputStream()

      def tsaRequest = new TimeStampRequest(rawRequest)

      TSAModule tsa = new TSAModule()
      tsa.validate(tsaRequest)

      def resp = tsa.generate(tsaRequest)

      def encodedResponse = tsa.encode(resp)

      response.contentType("application/timestamp-reply")
      response.headers.add("Content-Transfer-Encoding", "base64")

      response.send "${encodedResponse}"
    }
  }
}
