package garantito.sinapuli.model

import com.j256.ormlite.support.ConnectionSource
import com.j256.ormlite.dao.Dao
import com.j256.ormlite.dao.DaoManager
import com.j256.ormlite.table.TableUtils

import java.sql.SQLException
import javax.sql.DataSource

import javax.inject.Inject

public class TenderOfferRepository {
	private Dao<TenderOffer, Integer> tenderOfferDao

  @Inject
	TenderOfferRepository(ConnectionSource connectionSource) {
    setupDatabase(connectionSource)
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

	public TenderOffer create(String hash, Offerer offerer, Project project) throws Exception {
		TenderOffer tenderOffer = new TenderOffer(hash, offerer, project);

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

	public List<TenderOffer> listWithProject(int idProject) {
		try {
		    return tenderOfferDao.queryForEq("project_id", idProject);
		} catch (SQLException e) {
		    e.printStackTrace();
		}
	}

}
