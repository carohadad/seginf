package garantito.sinapuli.model

import com.j256.ormlite.support.ConnectionSource
import com.j256.ormlite.dao.Dao
import com.j256.ormlite.dao.DaoManager
import com.j256.ormlite.table.TableUtils

import javax.inject.Inject
import groovy.util.logging.Log
import java.util.logging.Level
import java.sql.SQLException

@Log
public class ProjectRepository {
  private Dao<Project, Integer> projectDao

  @Inject
  ProjectRepository(ConnectionSource connectionSource) {
    setupDatabase(connectionSource)
  }

  /**
   * Setup our database and DAOs
   */
  private void setupDatabase(ConnectionSource connectionSource) {
    projectDao = DaoManager.createDao(connectionSource, Project.class)
    TableUtils.createTableIfNotExists(connectionSource, Project.class)
  }

  public Project get(int id) {
    projectDao.queryForId(id)
  }

  public void delete(int id) {
    projectDao.deleteById(id)
  }

  public void delete(Project project) {
    projectDao.delete(project)
  }

  public Project create(Project project) {
    project.validate()
    projectDao.create(project)
    project
  }

  public Project update(Project project) {
    if (project.id) {
      project.validate()
      projectDao.update(project)
      project
    } else {
      throw new IllegalStateException('project is not saved yet')
    }
  }

  public List<Project> list() {
    try {
      projectDao.queryForAll()
    } catch (SQLException e) {
      log.log Level.WARNING, "failed to get project list", e
      []
    }
  }
}

