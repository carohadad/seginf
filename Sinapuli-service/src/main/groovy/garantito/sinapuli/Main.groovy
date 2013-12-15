package garantito.sinapuli

import ratpack.launch.LaunchConfigFactory
import ratpack.groovy.launch.GroovyRatpackMain

class Main extends GroovyRatpackMain {
  public static void main(String[] args) {
    println "Iniciando SINAPULI"
    new Main().start()    
  }

  @Override
  protected void addImpliedDefaults(Properties props) {
    super.addImpliedDefaults(props)
    // por algún motivo Ratpack quiere que las properties estén como strings,
    // incluso cuando son números...
    props.put(LaunchConfigFactory.Property.PORT, '5051')
    props.put(LaunchConfigFactory.Property.MAX_CONTENT_LENGTH, (2*1024*1024).toString())
  }
}

