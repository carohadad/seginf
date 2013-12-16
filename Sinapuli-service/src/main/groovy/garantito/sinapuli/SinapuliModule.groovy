package garantito.sinapuli

import com.google.inject.AbstractModule
import com.google.inject.Scopes

class SinapuliModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(OffererRepository.class).in(Scopes.SINGLETON)
    bind(TenderOfferRepository.class).in(Scopes.SINGLETON)
    bind(ProyectRepository.class).in(Scopes.SINGLETON)
    bind(AuthHandlers.class).in(Scopes.SINGLETON)
  }
}

