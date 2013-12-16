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
	
	/*
	// Este update no se deberia usar, si se necesita hay que modificar para no cambiar algunos campos
	public void update(int id, String name, String description, Date creationDate, Date startTenderDate, Date endTenderDate, byte[] tender) throws SQLException, Exception {	
		def project = get(id);
		
		project.name = name;
		project.description = description;
		project.creationDate = creationDate;
		project.startTenderDate = startTenderDate;
		project.endTenderDate = endTenderDate;
		project.tender = tender;


		tenderOfferDao.update(project);
	}
	*/
	
	public Project get(int id) throws SQLException, Exception {
		return projectDao.queryForId(id);
	}

	public Project create(String name, String description, Date startTenderDate, Date endTenderDate, byte[] tender) throws Exception {
		Project project = new Project(name, description, startTenderDate, endTenderDate, tender);

		projectDao.create(project);
		return project;
	}

	public List<Project> list() {
		try {
		    return projectDao.queryForAll();
		} catch (SQLException e) {
		    e.printStackTrace();
		}
	}

}
