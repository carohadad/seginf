import static ratpack.groovy.Groovy.*
import org.bouncycastle.tsp.*


ratpack {
  handlers {

    /*
    Request:

    TimeStampReq ::= SEQUENCE {
      version               INTEGER { v1(1) },
      messageImprint        MessageImprint,
      --a hash algorithm OID and the hash value of the data to be time-stamped
      reqPolicy            TSAPolicyId       OPTIONAL,
      nonce                INTEGER         OPTIONAL,
      certReq              BOOLEAN         DEFAULT FALSE,
      extensions           [0] IMPLICIT Extensions OPTIONAL
    }
    */

    get("get_timestamp") {

      TimeStampRequestGenerator reqGen = new TimeStampRequestGenerator();

      // Dummy request
      TimeStampRequest request = reqGen.generate(
        TSPAlgorithms.SHA1, new byte[20], BigInteger.valueOf(100));

      byte[] reqData = request.getEncoded();


      response.send "It worked ${reqData}"

    }
  }
}
