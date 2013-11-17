@GrabResolver("https://oss.jfrog.org/artifactory/repo")
@GrabResolver("http://central.maven.org/maven2/org/bouncycastle/")

@Grab("org.ratpack-framework:ratpack-groovy:0.9.0-SNAPSHOT")
@Grab("org.bouncycastle:bctsp-jdk16")

import static org.ratpackframework.groovy.RatpackScript.ratpack
import static org.ratpackframework.groovy.Template.groovyTemplate

import org.bouncycastle.tsp.*


// You can change anything in the ratpack {} closure without needing to restart

ratpack {
    handlers {
        get("bouncy") {
            TimeStampRequestGenerator reqGen = new TimeStampRequestGenerator();

            // Dummy request
            TimeStampRequest request = reqGen.generate(
                TSPAlgorithms.SHA1, new byte[20], BigInteger.valueOf(100));

            byte[] reqData = request.getEncoded();


           response.send "It worked ${reqData}"

        }
    }
}
