package garantito.sinapuli

import com.j256.ormlite.jdbc.JdbcConnectionSource
import com.j256.ormlite.support.ConnectionSource
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager
import com.j256.ormlite.table.TableUtils

import java.sql.SQLException;
import java.util.List;
import java.util.Date;

@Singleton(lazy = true, strict =false)
public class ProyectRepository {

	String DATABASE_URL = "jdbc:h2:~/seginf/Sinapuli-service/SinapuliDB;DB_CLOSE_DELAY=-1";

	JdbcConnectionSource connectionSource = null;
	Dao<Proyect, Integer> proyectDao = null;

	ProyectRepository() {
		try {
			if(connectionSource == null){
				// create our data source			
				connectionSource = new JdbcConnectionSource(DATABASE_URL);
				// setup our database and DAOs
				setupDatabase(connectionSource);
				System.out.println("\n\nIt seems to have worked\n\n");		
			}
		} finally {
			// destroy the data source which should close underlying connections
			if (connectionSource != null) {
				connectionSource.close();
			}
		}
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
