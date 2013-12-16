package garantito.sinapuli

import com.j256.ormlite.support.ConnectionSource
import com.j256.ormlite.db.H2DatabaseType
import com.j256.ormlite.jdbc.DataSourceConnectionSource
import javax.inject.Inject
import javax.inject.Provider
import javax.sql.DataSource


class ConnectionSourceProvider implements Provider<ConnectionSource> {
  private final DataSource dataSource

  @Inject
  ConnectionSourceProvider(DataSource ds) {
    this.dataSource = ds
  }

  @Override
  public ConnectionSource get() {
    def connectionSource = new DataSourceConnectionSource(dataSource, new H2DatabaseType())
    connectionSource.initialize()
    connectionSource
  }
}


