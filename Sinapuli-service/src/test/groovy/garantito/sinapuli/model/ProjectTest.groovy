package garantito.sinapuli.model

import garantito.sinapuli.*
import spock.lang.Specification

import org.joda.time.*

class ProjectTest extends Specification {
  def "validates end date after start date"() {
    setup:
    def now = new DateTime()
    def project = buildProject()

    when:
    project.startTenderDate = now.toDate()
    project.endTenderDate = now.plusDays(1).toDate()
    project.validate()

    then:
    notThrown(ValidationException)

    when:
    project.endTenderDate = now.minusDays(1).toDate()
    project.validate()

    then:
    thrown(ValidationException)

    when:
    project.endTenderDate = now.toDate()
    project.validate()

    then:
    thrown(ValidationException)
  }

  def "finish date after end (close) date"() {
    setup:
    def now = new DateTime()
    def project = buildProject()

    when:
    project.startTenderDate = now.toDate()
    project.endTenderDate = now.plusDays(1).toDate()

    then:
    project.finishTenderDate instanceof Date
    project.finishTenderDate > project.endTenderDate
  }

  def "calculates status according to start and end dates"() {
    setup:
    def project = buildProject()
    def now = new DateTime()

    when:
    project.startTenderDate = now.plusDays(1).toDate()
    project.endTenderDate = now.plusDays(2).toDate()

    then:
    project.status == Project.Status.PENDING

    when:
    project.startTenderDate = now.minusHours(1).toDate()
    project.endTenderDate = now.plusHours(1).toDate()

    then:
    project.status == Project.Status.OPEN

    when:
    // una hora antes de transcurrido el período de cierre
    project.endTenderDate = now.minus(Project.CLOSING_PERIOD).plusHours(1).toDate()
    project.startTenderDate = new DateTime(project.endTenderDate).minusDays(1).toDate()

    then:
    project.status == Project.Status.CLOSED

    when:
    // una hora después de transcurrido el período de cierre
    project.endTenderDate = now.minus(Project.CLOSING_PERIOD).minusHours(1).toDate()
    project.startTenderDate = new DateTime(project.endTenderDate).minusDays(1).toDate()

    then:
    project.status == Project.Status.FINISHED
  }

  private Project buildProject() {
    def project = new Project(name: 'project', description: 'some project')
    project.tender = 'el pliego'.bytes
    project.tenderContentType = 'text/plain'
    project.tenderFilename = 'pliego.txt'
    project
  }
}

