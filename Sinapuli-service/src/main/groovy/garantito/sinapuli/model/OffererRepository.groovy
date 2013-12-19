package garantito.sinapuli.model

import com.j256.ormlite.jdbc.JdbcConnectionSource
import com.j256.ormlite.support.ConnectionSource
import com.j256.ormlite.dao.Dao
import com.j256.ormlite.dao.DaoManager
import com.j256.ormlite.table.TableUtils

import java.sql.SQLException

import javax.inject.Inject
import groovy.util.logging.Log
import java.util.logging.Level

import garantito.sinapuli.ValidationException

@Log
public class OffererRepository {
  Dao<Offerer, Integer> offererDao

  @Inject
  OffererRepository(ConnectionSource connectionSource) {
    setupDatabase(connectionSource)
  }

  /**
   * Setup our database and DAOs
   */
  private void setupDatabase(ConnectionSource connectionSource) {
    offererDao = DaoManager.createDao(connectionSource, Offerer.class)
    TableUtils.createTableIfNotExists(connectionSource, Offerer.class)
  }

  public void delete(int id) {
    offererDao.deleteById(id)
  }

  public Offerer get(int id) {
    offererDao.queryForId(id)
  }

  public Offerer create(Offerer offerer) throws Exception {
    try {
      offerer.validate()
      offererDao.create(offerer)
      offerer
    } catch (SQLException e) {
      if (offererDao.queryForFieldValuesArgs(username: offerer.username).size() > 0) {
        throw new ValidationException('El nombre de usuario ya existe')
      } else {
        throw e
      }
    }
  }

  public boolean authenticate(String username, String password) {
    def offerer = findByUsername(username)

    if (offerer == null) {
      false
    } else {
      offerer.checkPassword(password)
    }
  }

  public List<Project> list() {
    try {
      offererDao.queryForAll()
    } catch (SQLException e) {
      log.log Level.WARNING, "failed to get offerer list", e
      []
    }
  }

  public Offerer findByUsername(String username) {
    def results = offererDao.queryForFieldValuesArgs(username: username)
    if (results.size() < 1) {
      null
    } else {
      results.get(0)
    }
  }
}

