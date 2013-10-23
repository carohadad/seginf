@GrabResolver("https://oss.jfrog.org/artifactory/repo")
@Grab("org.ratpack-framework:ratpack-groovy:0.9.0-SNAPSHOT")
import static org.ratpackframework.groovy.RatpackScript.ratpack
import static org.ratpackframework.groovy.Template.groovyTemplate


// You can change anything in the ratpack {} closure without needing to restart

ratpack {
    handlers {
        get {
	       println("GET")
            response.send "This is the app root (also try: /date and /some.txt)"
        }

        get("date") {
            println("GET Date")
            render groovyTemplate("date.html")
        }

        assets "public"
    }
}
