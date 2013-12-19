package garantito.sinapuli

import ratpack.h2.H2Module

import com.google.inject.*
import garantito.sinapuli.model.*

import org.joda.time.*


class SeedData {
  static final String DATABASE_URL = "jdbc:h2:data/SinapuliDB;DB_CLOSE_DELAY=-1"

  def seedProjects(repo) {
    println "Seeding projects..."
    def templates = [
      [name: "El finalizado", description: "Proyecto de prueba cerrado y finalizado", 
        start: -10, end: -5],
      [name: "El cerrado", description: "Proyecto de prueba cerrado pero aún espera las ofertas finales", 
       start: -6, end: -1],
      [name: "El abierto", description: "Proyecto de prueba abierto y aceptando ofertas",
       start: -1, end: 3],
      [name: "El pendiente", description: "Proyecto de prueba que aún no comenzó",
       start: 1, end: 10]
    ]

    def tenderBytes = getClass().classLoader.getResourceAsStream('enunciado.pdf').bytes

    templates.each { templ ->
      def start = new DateTime().plusDays(templ.start).toDate()
      def end = new DateTime().plusDays(templ.end).toDate()
      def project = new Project(name: templ.name, description: templ.description)
      project.startTenderDate = start
      project.endTenderDate = end
      project.tender = tenderBytes
      project.tenderContentType = 'application/pdf'
      project.tenderFilename = 'enunciado.pdf'

      repo.create(project)
    }
  }

  def run() {
    def injector = Guice.createInjector(
      new H2Module('', '', DATABASE_URL), 
      new SinapuliModule()
    )
    
    seedProjects(injector.getInstance(ProjectRepository.class))
  }

  public static void main(String[] args) {
    new SeedData().run()
  }
}

