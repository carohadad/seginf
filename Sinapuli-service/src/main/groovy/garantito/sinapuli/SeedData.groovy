package garantito.sinapuli

import ratpack.h2.H2Module

import com.google.inject.*
import garantito.sinapuli.*
import garantito.sinapuli.model.*
import garantito.sinapuli.tsa.*

import org.joda.time.*
import java.security.*


class SeedData {
  static class SeedModule extends AbstractModule {
    def keyStore

    SeedModule() {
      keyStore = KeyStore.getInstance('JKS')
      def stream = getClass().classLoader.getResourceAsStream('samples/faketsa.jks')
      keyStore.load(stream, 'garantito'.toCharArray())
    }

    @Override
    protected void configure() {
      bind(KeyStore.class).toInstance(keyStore)
      bind(TSAClient.class).to(FakeTSAClient.class)
    }
  }

  static final String DATABASE_URL = "jdbc:h2:data/SinapuliDB;DB_CLOSE_DELAY=-1"

  def offerer
  def projects = [:]

  def digester

  def seedProjects(repo) {
    println "Seeding projects..."
    def templates = [
      finished: [
        name: "El finalizado", description: "Proyecto de prueba cerrado y finalizado", 
        start: -10, end: -5],
      closed: [
        name: "El cerrado", description: "Proyecto de prueba cerrado pero aún espera las ofertas finales", 
        start: -6, end: -1],
      open: [
        name: "El abierto", description: "Proyecto de prueba abierto y aceptando ofertas",
        start: -1, end: 3],
      pending: [
        name: "El pendiente", description: "Proyecto de prueba que aún no comenzó",
        start: 1, end: 10]
    ]

    def tenderBytes = getClass().classLoader.getResourceAsStream('samples/enunciado.pdf').bytes

    templates.each { key, templ ->
      def start = new DateTime().plusDays(templ.start).toDate()
      def end = new DateTime().plusDays(templ.end).toDate()
      def project = new Project(name: templ.name, description: templ.description)
      project.startTenderDate = start
      project.endTenderDate = end
      project.tender = tenderBytes
      project.tenderContentType = 'application/pdf'
      project.tenderFilename = 'enunciado.pdf'

      projects[key] = repo.create(project)
    }
  }

  def loadOffererKeyStore() {
    def sampleStream = getClass().classLoader.getResourceAsStream('samples/sample.jks')
    def keystore = KeyStore.getInstance('JKS')
    keystore.load(sampleStream, 'sample'.toCharArray())
    keystore
  }

  def seedOfferer(repo) {
    println "Seeding offerer..."

    def keystore = loadOffererKeyStore()
    def certificate = keystore.getCertificate('sample')

    def offerer = new Offerer(username: 'jose', name: 'José Oferente')
    offerer.password = 'jose'
    offerer.repeatPassword = 'jose'
    offerer.publicKey = "-----BEGIN CERTIFICATE-----\n" + \
      certificate.encoded.encodeBase64(true) + \
      "-----END CERTIFICATE-----\n"

    this.offerer = repo.create(offerer)
  }

  def offererSignData(byte[] data) {
    def keystore = loadOffererKeyStore()
    def privateKey = keystore.getKey('sample', 'sample'.toCharArray()) 
    def signature = Signature.getInstance("SHA256withRSA")
    signature.initSign(privateKey)
    signature.update(data)
    signature.sign().encodeBase64(true)
  }

  def seedOffers(repo, tsaClient) {
    println "Seeding offers..."

    def offer

    def file1 = getClass().classLoader.getResourceAsStream('samples/oferta1.txt').bytes
    def file2 = getClass().classLoader.getResourceAsStream('samples/oferta2.txt').bytes

    def hash1 = digester.digest(file1)
    def hash2 = digester.digest(file2)

    // dos ofertas en el proyecto abierto
    offer = new TenderOffer()
    offer.offerer = offerer
    offer.project = projects.open
    offer.hash = hash1
    offer.hashSignature = offererSignData(hash1)
    offer.offerDate = new DateTime(projects.open.startTenderDate).plusHours(1).toDate()
    tsaClient.date = offer.offerDate
    repo.placeOffer(offer)

    offer = new TenderOffer()
    offer.offerer = offerer
    offer.project = projects.open
    offer.hash = hash2
    offer.hashSignature = offererSignData(hash2)
    offer.offerDate = new DateTime(projects.open.startTenderDate).plusHours(2).toDate()
    tsaClient.date = offer.offerDate
    repo.placeOffer(offer)

    // dos ofertas en el cerrado, una completa, la otra no
    offer = new TenderOffer()
    offer.offerer = offerer
    offer.project = projects.closed
    offer.hash = hash1
    offer.hashSignature = offererSignData(hash1)
    offer.offerDate = new DateTime(projects.closed.startTenderDate).plusHours(1).toDate()
    tsaClient.date = offer.offerDate
    offer = repo.placeOffer(offer)

    offer.document = file1
    offer.documentType = 'text/plain'
    offer.documentFilename = 'oferta1.txt'
    offer.completeDate = new DateTime(projects.closed.endTenderDate).plusHours(1).toDate()
    tsaClient.date = offer.completeDate
    repo.complete(offer)

    offer = new TenderOffer()
    offer.offerer = offerer
    offer.project = projects.closed
    offer.hash = hash2
    offer.hashSignature = offererSignData(hash2)
    offer.offerDate = new DateTime(projects.closed.startTenderDate).plusHours(2).toDate()
    tsaClient.date = offer.offerDate
    repo.placeOffer(offer)

    // dos ofertas en el finalizado, una completa, la otra no
    offer = new TenderOffer()
    offer.offerer = offerer
    offer.project = projects.finished
    offer.hash = hash1
    offer.hashSignature = offererSignData(hash1)
    offer.offerDate = new DateTime(projects.finished.startTenderDate).plusHours(1).toDate()
    tsaClient.date = offer.offerDate
    offer = repo.placeOffer(offer)

    offer.document = file1
    offer.documentType = 'text/plain'
    offer.documentFilename = 'oferta1.txt'
    offer.completeDate = new DateTime(projects.finished.endTenderDate).plusHours(1).toDate()
    tsaClient.date = offer.completeDate
    repo.complete(offer)

    offer = new TenderOffer()
    offer.offerer = offerer
    offer.project = projects.finished
    offer.hash = hash2
    offer.hashSignature = offererSignData(hash2)
    offer.offerDate = new DateTime(projects.finished.startTenderDate).plusHours(2).toDate()
    tsaClient.date = offer.offerDate
    repo.placeOffer(offer)
  }

  def run() {
    def injector = Guice.createInjector(
      new H2Module('', '', DATABASE_URL), 
      new SeedModule(),
      new SinapuliModule()
    )
    
    digester = injector.getInstance(Digester.class)

    seedOfferer(injector.getInstance(OffererRepository.class))
    seedProjects(injector.getInstance(ProjectRepository.class))
    seedOffers(injector.getInstance(TenderOfferRepository.class),
      injector.getInstance(TSAClient.class))
  }

  public static void main(String[] args) {
    new SeedData().run()
  }
}

