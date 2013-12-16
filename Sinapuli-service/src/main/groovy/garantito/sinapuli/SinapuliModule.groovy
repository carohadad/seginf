package garantito.sinapuli

import com.google.inject.AbstractModule
import com.google.inject.Scopes
import com.j256.ormlite.support.ConnectionSource

class SinapuliModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(ConnectionSource.class).toProvider(ConnectionSourceProvider.class).in(Scopes.SINGLETON)

    bind(OffererRepository.class).in(Scopes.SINGLETON)
    bind(TenderOfferRepository.class).in(Scopes.SINGLETON)
    bind(ProyectRepository.class).in(Scopes.SINGLETON)

    bind(AuthHandlers.class).in(Scopes.SINGLETON)
  }
}

