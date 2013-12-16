package garantito.sinapuli

import com.j256.ormlite.jdbc.JdbcConnectionSource
import com.j256.ormlite.support.ConnectionSource
import com.j256.ormlite.dao.Dao
import com.j256.ormlite.dao.DaoManager
import com.j256.ormlite.table.TableUtils

import java.sql.SQLException
import java.util.Date

import javax.inject.Inject

public class ProyectRepository {

	private Dao<Proyect, Integer> proyectDao

  @Inject
	ProyectRepository(ConnectionSource connectionSource) {
    setupDatabase(connectionSource)
	}

	/**
	 * Setup our database and DAOs	 
	 */
	private void setupDatabase(ConnectionSource connectionSource) throws Exception {
		proyectDao = DaoManager.createDao(connectionSource, Proyect.class);
		// if you need to create the table
		TableUtils.createTableIfNotExists(connectionSource, Proyect.class);
	}

	public void delete(int id) throws SQLException, Exception {
		def proyect = get(id);
		proyectDao.delete(proyect);
	}

	
	public void update(int id, String name) throws SQLException, Exception {	
		def proyect = get(id);		
		
		proyect.name = name;	
		proyectDao.update(proyect);
	}	
	
	/*
	// Este update no se deberia usar, si se necesita hay que modificar para no cambiar algunos campos
	public void update(int id, String name, String description, Date creationDate, Date startTenderDate, Date endTenderDate, byte[] tender) throws SQLException, Exception {	
		def proyect = get(id);
		
		proyect.name = name;
		proyect.description = description;
		proyect.creationDate = creationDate;
		proyect.startTenderDate = startTenderDate;
		proyect.endTenderDate = endTenderDate;
		proyect.tender = tender;


		tenderOfferDao.update(proyect);
	}
	*/
	
	public Proyect get(int id) throws SQLException, Exception {
		return proyectDao.queryForId(id);
	}

	public Proyect create(String name, String description, Date startTenderDate, Date endTenderDate, byte[] tender) throws Exception {
		Proyect proyect = new Proyect(name, description, startTenderDate, endTenderDate, tender);

		proyectDao.create(proyect);
		return proyect;
	}

	public List<Proyect> list() {
		try {
		    return proyectDao.queryForAll();
		} catch (SQLException e) {
		    e.printStackTrace();
		}
	}

}
