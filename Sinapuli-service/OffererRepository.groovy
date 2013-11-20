import com.j256.ormlite.jdbc.JdbcConnectionSource
import com.j256.ormlite.support.ConnectionSource
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager
import com.j256.ormlite.table.TableUtils

import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@Singleton(lazy = true)
public class OffererRepository {

	String DATABASE_URL = "jdbc:h2:mem:offerer;DB_CLOSE_DELAY=-1";

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
		TableUtils.createTable(connectionSource, Offerer.class);
	}


	private void delete(int id) throws SQLException, Exception {
		def offerer = get(id);
		offererDao.delete(offerer);
	}

	//INPOT
	private Offerer update(int id, String name) throws SQLException, Exception {
	
		def offerer = get(id);
		offerer.setName(name);

		// update the database after changing the object
		offererDao.update(offerer);
		verifyDb(id, offerer);
	}

	private Offerer get(int id) throws SQLException, Exception {
		return offererDao.queryForId(id);
	}

	private Offerer create(String name) throws Exception {
		// create an instance of Offerer
		Offerer offerer = new Offerer(name);

		// persist the offerer object to the database
		offererDao.create(offerer);
		int id = offerer.getId();
		verifyDb(id, offerer);

		return offerer;
	}

	/**
	 * Verify that the offerer stored in the database was the same as the expected object.
	 */
	private void verifyDb(int id, Offerer expected) throws SQLException, Exception {
		// make sure we can read it back
		Offerer offerer2 = offererDao.queryForId(id);
		if (offerer2 == null) {
		        throw new Exception("Should have found id '" + id + "' in the database");
		}
		verify(expected, offerer2);
	}
	/**
	 * Verify that the offerer is the same as expected.
	 */
	private static void verify(Offerer expected, Offerer offerer2) {
		assertEquals("expected name does not equal offerer name", expected, offerer2);
	}

	private List<Offerer> list() {
		try {
		    return offererDao.queryForAll();
		} catch (SQLException e) {
		    e.printStackTrace();
		    //return list();
		}
	}
}
