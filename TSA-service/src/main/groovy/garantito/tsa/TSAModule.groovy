package garantito.tsa

import org.bouncycastle.tsp.*

class TSAModule {

  /*
    The Time Stamp Authority SHOULD check whether or not the given hash algorithm is
    known to be "sufficient" (based on the current state of knowledge in
    cryptanalysis and the current state of the art in computational resources, for example).
  */

  def acceptedAlgorithms = [TSPAlgorithms.SHA1] as Set

  /*
  public void validate(java.util.Set algorithms,
                     java.util.Set policies,
                     java.util.Set extensions)
              throws TSPException

  */
  def validate(TimeStampRequest request) {
    request.validate(acceptedAlgorithms, null, null)
  }
}
