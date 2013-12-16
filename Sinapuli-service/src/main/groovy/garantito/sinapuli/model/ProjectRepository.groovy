package garantito.sinapuli.model

import com.j256.ormlite.jdbc.JdbcConnectionSource
import com.j256.ormlite.support.ConnectionSource
import com.j256.ormlite.dao.Dao
import com.j256.ormlite.dao.DaoManager
import com.j256.ormlite.table.TableUtils

import java.sql.SQLException
import java.util.Date

import javax.inject.Inject

public class ProjectRepository {

	private Dao<Project, Integer> projectDao

  @Inject
	ProjectRepository(ConnectionSource connectionSource) {
    setupDatabase(connectionSource)
	}

	/**
	 * Setup our database and DAOs	 
	 */
	private void setupDatabase(ConnectionSource connectionSource) throws Exception {
		projectDao = DaoManager.createDao(connectionSource, Project.class);
		// if you need to create the table
		TableUtils.createTableIfNotExists(connectionSource, Project.class);
	}

	public void delete(int id) throws SQLException, Exception {
		def project = get(id);
		projectDao.delete(project);
	}

	
	public void update(int id, String name) throws SQLException, Exception {	
		def project = get(id);		
		
		project.name = name;	
		projectDao.update(project);
	}	
	
	public Project get(int id) {
		projectDao.queryForId(id)
	}

	public Project create(Project project) {
		projectDao.create(project)
		project
	}

  public List<Project> list() {
    try {
      projectDao.queryForAll()
    } catch (SQLException e) {
      e.printStackTrace()
      []
    }
  }
}
