import static ratpack.groovy.Groovy.*
import org.bouncycastle.tsp.*
import org.bouncycastle.cms.SignerInfoGenerator
import org.bouncycastle.operator.DigestCalculator;

import garantito.tsa.TSAModule

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

      TimeStampRequestGenerator reqGen = new TimeStampRequestGenerator()

      // TODO: generar este objeto a partir de los parametros!
      TimeStampRequest request = reqGen.generate(
        TSPAlgorithms.SHA1, new byte[20], BigInteger.valueOf(100))


      TSAModule tsa = new TSAModule()
      tsa.validate(request)


      // Generamos la respuesta
      SignerInfoGenerator signerInfoGen = null
      DigestCalculator digestCalculator = null
      org.bouncycastle.asn1.ASN1ObjectIdentifier tsaPolicy = request.getReqPolicy()

      TimeStampTokenGenerator respGen = new TimeStampTokenGenerator(signerInfoGen, digestCalculator, tsaPolicy)

      // TODO: Este numero tiene que ser unico, por ahora lo genero random, pero creo que deberiamos guardarlo.
      java.math.BigInteger serialNumber = new BigInteger(160, new Random())

      TimeStampToken timeStampToken = respGen.generate(request, serialNumber, new Date())

      response.send "It worked ${timeStampToken}"

    }
  }
}
