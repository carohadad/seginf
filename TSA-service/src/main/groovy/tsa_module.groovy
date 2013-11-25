import org.bouncycastle.tsp.*

class TSAModule {

  def acceptedAlgorithms = [TSPAlgorithms.SHA1] as Set

  def validate(TimeStampRequest request) {
    request.validate(acceptedAlgorithms, null, null)
  }
}
