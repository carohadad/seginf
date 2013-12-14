package garantito.sinapuli

import com.j256.ormlite.jdbc.JdbcConnectionSource
import com.j256.ormlite.support.ConnectionSource
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager
import com.j256.ormlite.table.TableUtils

import java.sql.SQLException;
import java.util.List;

@Singleton(lazy = true, strict = false)
public class TenderOfferRepository {

	String DATABASE_URL = "jdbc:h2:~/seginf/Sinapuli-service/SinapuliDB;DB_CLOSE_DELAY=-1";

	JdbcConnectionSource connectionSource = null;
	Dao<TenderOffer, Integer> tenderOfferDao = null;

	private TenderOfferRepository() {
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
		TableUtils.createTableIfNotExists(connectionSource, TenderOffer.class);
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

	public TenderOffer create(String hash, Offerer offerer, Proyect proyect) throws Exception {
		TenderOffer tenderOffer = new TenderOffer(hash, offerer, proyect);

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

	public List<TenderOffer> listWithProyect(int idProyect) {
		try {
		    return tenderOfferDao.queryForEq("proyect_id", idProyect);
		} catch (SQLException e) {
		    e.printStackTrace();
		}
	}

}
