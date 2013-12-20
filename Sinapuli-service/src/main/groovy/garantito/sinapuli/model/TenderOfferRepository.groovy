package garantito.sinapuli.model

import com.j256.ormlite.support.ConnectionSource
import com.j256.ormlite.dao.Dao
import com.j256.ormlite.dao.DaoManager
import com.j256.ormlite.table.TableUtils

import java.sql.SQLException
import groovy.util.logging.Log
import java.util.logging.Level

import javax.inject.Inject

import garantito.sinapuli.tsa.TSAClient
import garantito.sinapuli.ValidationException

@Log
public class TenderOfferRepository {
	private Dao<TenderOffer, Integer> tenderOfferDao
  private TSAClient tsaClient

  @Inject
	TenderOfferRepository(ConnectionSource connectionSource, TSAClient tsaClient) {
    setupDatabase(connectionSource)
    this.tsaClient = tsaClient
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

  public TenderOffer placeOffer(TenderOffer offer) {
    offer.validate()

    if (offer.offerDate == null) {
      offer.offerDate = new Date()
    }

    // FIXME: deberíamos firmar el token luego de recibido
    byte[] hashBytes = offer.hash.decodeHex()
    byte[] token = tsaClient.getToken(hashBytes)
    offer.receiptToken = token.encodeBase64(true)

    create(offer)
  }

	public TenderOffer create(TenderOffer offer) {
    offer.validate()
		tenderOfferDao.create(offer)
		offer
	}

  public TenderOffer complete(TenderOffer offer) {
    offer.validate()
    if (!offer.hasDocument) {
      throw new ValidationException("No se puede completar una oferta sin un documento")
    }
    if (offer.documentReceiptToken != null) {
      throw new ValidationException("La oferta ya está completa")
    }

    if (offer.completeDate == null) {
      offer.completeDate = new Date()
    }

    // FIXME: enviar al TSA otro hash distinto del anterior
    // FIXME: firmar el token recibido
    byte[] hashBytes = offer.hash.decodeHex()
    byte[] token = tsaClient.getToken(hashBytes)
    offer.documentReceiptToken = token.encodeBase64(true)

    update(offer)
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

  public List<TenderOffer> listForProjectAndOfferer(int projectId, int offererId) {
    try {
      def query = tenderOfferDao.queryBuilder()
      query.where().eq('offerer_id', offererId).and().eq('project_id', projectId)
      query.query()
    } catch (SQLException e) {
      log.log Level.WARNING, "failed to get offers for project ${projectId}, offerer ${offererId}", e
      []
    }
  }
}

