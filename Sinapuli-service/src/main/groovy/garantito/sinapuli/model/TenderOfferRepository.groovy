package garantito.sinapuli.model

import com.j256.ormlite.support.ConnectionSource
import com.j256.ormlite.dao.Dao
import com.j256.ormlite.dao.DaoManager
import com.j256.ormlite.table.TableUtils

import java.sql.SQLException
import groovy.util.logging.Log
import java.util.logging.Level
import java.security.Signature

import javax.inject.Inject

import garantito.sinapuli.tsa.TSAClient
import garantito.sinapuli.ValidationException
import garantito.sinapuli.Digester
import garantito.sinapuli.KeyProvider

@Log
public class TenderOfferRepository {
	private Dao<TenderOffer, Integer> tenderOfferDao

  @Inject
  TSAClient tsaClient

  @Inject
  KeyProvider keyProvider

  @Inject
  Digester digester

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

  private def signData(byte[] data) {
    def signature = Signature.getInstance("SHA256withRSA")
    signature.initSign(keyProvider.privateKey)
    signature.update(data)
    signature = signature.sign()
  }

  public TenderOffer placeOffer(TenderOffer offer) {
    offer.validate()
    offer.validateHashSignature()

    if (offer.offerDate == null) {
      offer.offerDate = new Date()
    }

    byte[] hashBytes = offer.hash.decodeHex()
    byte[] token = tsaClient.getToken(hashBytes)
    byte[] signature = signData(token)

    offer.receiptToken = "-----BEGIN TSP RESPONSE-----\n" + \
      token.encodeBase64(true) + \
      "-----END TSP RESPONSE-----\n\n" + \
      "-----BEGIN SIGNATURE-----\n" + \
      signature.encodeBase64(true) + \
      "-----END SIGNATURE-----\n\n" + \
      "-----BEGIN CERTIFICATE-----\n" + \
      keyProvider.certificate.encoded.encodeBase64(true) + \
      "-----END CERTIFICATE-----\n"

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
    offer.validateDocument()

    if (offer.completeDate == null) {
      offer.completeDate = new Date()
    }

    String receipt = "DOCUMENTO RECIBIDO, HASH ${offer.hash}\n"
    byte[] receiptHashBytes = digester.digest(receipt.bytes)
    byte[] token = tsaClient.getToken(receiptHashBytes)
    byte[] signature = signData(token)

    offer.documentReceiptToken = 
      "-----BEGIN RECEIPT-----\n" + \
      receipt + \
      "-----END RECEIPT-----\n\n" + \
      "-----BEGIN TSP RESPONSE-----\n" + \
      token.encodeBase64(true) + \
      "-----END TSP RESPONSE-----\n\n" + \
      "-----BEGIN SIGNATURE-----\n" + \
      signature.encodeBase64(true) + \
      "-----END SIGNATURE-----\n\n" + \
      "-----BEGIN CERTIFICATE-----\n" + \
      keyProvider.certificate.encoded.encodeBase64(true) + \
      "-----END CERTIFICATE-----\n"

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
		  def query = tenderOfferDao.queryBuilder()
      query.where().eq("project_id", projectId)
      query.orderBy('offerDate', true)
      query.query()
		} catch (SQLException e) {
      log.log Level.WARNING, "failed to get offers for project ${projectId}", e
      []
		}
	}

  public long countForProjectId(int projectId) {
    println "counting offers for ${projectId}"
    tenderOfferDao.queryBuilder().where().eq('project_id', projectId).countOf()
  }

	public List<TenderOffer> listForOffererId(int offererId) {
		try {
		  def query = tenderOfferDao.queryBuilder()
      query.where().eq("offerer_id", offererId)
      query.orderBy('offerDate', true)
      query.query()
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

