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

      response.send "${encodedResponse}"
    }
  }
}
