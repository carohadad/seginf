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
public class TenderOfferRepository {

	String DATABASE_URL = "jdbc:h2:mem:tenderOfferer;DB_CLOSE_DELAY=-1";

	JdbcConnectionSource connectionSource = null;
	Dao<TenderOffer, Integer> tenderOfferDao = null;

	TenderOfferRepository() {
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

		tenderOfferDao = DaoManager.createDao(connectionSource, TenderOffer.class);
		// if you need to create the table
		TableUtils.createTable(connectionSource, TenderOffer.class);
	}

	public void delete(int id) throws SQLException, Exception {
		def tenderOffer = get(id);
		tenderOfferDao.delete(tenderOffer);
	}

	public void update(int id, String hash) throws SQLException, Exception {	
		def tenderOffer = get(id);
		tenderOffer.setHash(hash);

		tenderOfferDao.update(tenderOffer);
	}

	public TenderOffer get(int id) throws SQLException, Exception {
		return tenderOfferDao.queryForId(id);
	}

	public TenderOffer create(String hash) throws Exception {
		TenderOffer tenderOffer = new TenderOffer(hash);

		tenderOfferDao.create(tenderOffer);
		return tenderOffer;
	}

	public List<TenderOffer> list() {
		try {
		    return tenderOfferDao.queryForAll();
		} catch (SQLException e) {
		    e.printStackTrace();
		}
	}

}
