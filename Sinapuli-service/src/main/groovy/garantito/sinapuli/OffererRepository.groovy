package garantito.sinapuli

import com.j256.ormlite.jdbc.JdbcConnectionSource
import com.j256.ormlite.support.ConnectionSource
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager
import com.j256.ormlite.table.TableUtils

import java.sql.SQLException;
import java.util.List;

@Singleton(lazy = true, strict =false)
public class OffererRepository {

	String DATABASE_URL = "jdbc:h2:~/seginf/Sinapuli-service/SinapuliDB;DB_CLOSE_DELAY=-1";

	JdbcConnectionSource connectionSource = null;
	Dao<Offerer, Integer> offererDao = null;

	OffererRepository() {
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

		offererDao = DaoManager.createDao(connectionSource, Offerer.class);
		// if you need to create the table
		TableUtils.createTableIfNotExists(connectionSource, Offerer.class);
	}

	public void delete(int id) throws SQLException, Exception {
		def offerer = get(id);
		offererDao.delete(offerer);
	}

	public void update(int id, String name) throws SQLException, Exception {	
		def offerer = get(id);
		offerer.setName(name);

		offererDao.update(offerer);
	}

	public Offerer get(int id) throws SQLException, Exception {
		return offererDao.queryForId(id);
	}

	public Offerer create(String name) throws Exception {
		Offerer offerer = new Offerer(name);

		offererDao.create(offerer);
		return offerer;
	}

	public Offerer create(Offerer offerer) {
		offererDao.create(offerer)
		offerer
	}

	public boolean authenticate(String username, String password) {
		def results = offererDao.queryForFieldValuesArgs(username: username, password: password)
		return results.size() == 1
	}

	public List<Offerer> list() {
		try {
		    return offererDao.queryForAll();
		} catch (SQLException e) {
		    e.printStackTrace();
		}
	}


}
