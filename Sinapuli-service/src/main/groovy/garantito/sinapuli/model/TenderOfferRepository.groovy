package garantito.sinapuli.model

import com.j256.ormlite.support.ConnectionSource
import com.j256.ormlite.dao.Dao
import com.j256.ormlite.dao.DaoManager
import com.j256.ormlite.table.TableUtils

import java.sql.SQLException
import groovy.util.logging.Log
import java.util.logging.Level

import javax.inject.Inject

@Log
public class TenderOfferRepository {
	private Dao<TenderOffer, Integer> tenderOfferDao

  @Inject
	TenderOfferRepository(ConnectionSource connectionSource) {
    setupDatabase(connectionSource)
	}

	/**
	 * Setup our database and DAOs
	 */
	private void setupDatabase(ConnectionSource connectionSource) {
		tenderOfferDao = DaoManager.createDao(connectionSource, TenderOffer.class)
		TableUtils.createTableIfNotExists(connectionSource, TenderOffer.class)
	}

	public void delete(int id) {
		tenderOfferDao.deleteById(id)
	}

	public TenderOffer get(int id) {
		tenderOfferDao.queryForId(id)
	}

	public TenderOffer create(TenderOffer offer) {
    offer.validate()
		tenderOfferDao.create(offer)
		offer
	}

  public TenderOffer update(TenderOffer offer) {
    if (offer.id == null) {
      throw new IllegalArgumentException("offer is not saved yet")
    } else {
      offer.validate()
      tenderOfferDao.update(offer)
      offer
    }
  }

	public List<TenderOffer> list() {
		try {
      tenderOfferDao.queryForAll()
		} catch (SQLException e) {
      log.log Level.WARNING, "failed to get offers list", e
      []
		}
	}

	public List<TenderOffer> listForProjectId(int projectId) {
		try {
		  tenderOfferDao.queryForEq("project_id", projectId)
		} catch (SQLException e) {
      log.log Level.WARNING, "failed to get offers for project ${projectId}", e
      []
		}
	}

	public List<TenderOffer> listForOffererId(int offererId) {
		try {
		  tenderOfferDao.queryForEq("offerer_id", offererId)
		} catch (SQLException e) {
      log.log Level.WARNING, "failed to get offers for offerer ${offererId}", e
      []
		}
	}
}

