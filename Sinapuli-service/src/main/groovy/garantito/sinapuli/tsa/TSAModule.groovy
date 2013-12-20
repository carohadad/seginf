package garantito.sinapuli.tsa

import com.google.inject.AbstractModule
import com.google.inject.Provides

class TSAModule extends AbstractModule {
  private String url
  private String username
  private String password

  public TSAModule(String url, String username, String password) {
    this.url = url
    this.username = username
    this.password = password
  }

  @Override
  protected void configure() {
  }

  @Provides
  TSAClient tsaClient() {
    new TSAClientImpl(url, username, password)
  }
}

