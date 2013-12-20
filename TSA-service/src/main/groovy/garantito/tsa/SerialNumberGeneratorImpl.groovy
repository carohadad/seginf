package garantito.tsa

class SerialNumberGeneratorImpl implements SerialNumberGenerator {
  private File storage

  SerialNumberGeneratorImpl(File storage) {
    this.storage = storage
  }

  synchronized BigInteger next() {
    def result

    if (storage.exists()) {
      result = new BigInteger(storage.text) + 1
    } else {
      result = BigInteger.ONE
    }

    storage.text = result.toString()

    result
  }
}

