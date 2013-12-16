package garantito.sinapuli

import com.google.inject.AbstractModule
import com.google.inject.Scopes
import com.j256.ormlite.support.ConnectionSource

import garantito.sinapuli.model.*
import garantito.sinapuli.handlers.*

class SinapuliModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(ConnectionSource.class).toProvider(ConnectionSourceProvider.class).in(Scopes.SINGLETON)

    bind(OffererRepository.class).in(Scopes.SINGLETON)
    bind(TenderOfferRepository.class).in(Scopes.SINGLETON)
    bind(ProjectRepository.class).in(Scopes.SINGLETON)

    bind(AuthHandlers.class).in(Scopes.SINGLETON)
    bind(RegisterHandler.class).in(Scopes.SINGLETON)
    bind(AdminHandlers.class).in(Scopes.SINGLETON)
    bind(OffererHandlers.class).in(Scopes.SINGLETON)
  }
}

