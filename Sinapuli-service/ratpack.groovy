@GrabResolver("https://oss.jfrog.org/artifactory/repo")
@GrabResolver("http://mvnrepository.com/artifact/")

@Grab("com.j256.ormlite:ormlite-jdbc:4.47")
@Grab("com.j256.ormlite:ormlite-core:4.47")
@Grab("com.h2database:h2:1.3.174")
@Grab("io.ratpack:ratpack-groovy:0.9.0-SNAPSHOT")

import static ratpack.groovy.Groovy.*

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.j256.ormlite.jdbc.JdbcConnectionSource
import com.j256.ormlite.support.ConnectionSource
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager
import com.j256.ormlite.table.TableUtils

import java.sql.SQLException;
import java.util.List;


ratpack {
    	
	
	String DATABASE_URL = "jdbc:h2:mem:offerer;DB_CLOSE_DELAY=-1";

	JdbcConnectionSource connectionSource = null;
	Dao<Offerer, Integer> offererDao = null;

	try {
		// create our data source
		connectionSource = new JdbcConnectionSource(DATABASE_URL);
		// setup our database and DAOs
		setupDatabase(connectionSource);
		System.out.println("\n\nIt seems to have worked\n\n");

	} finally {
		// destroy the data source which should close underlying connections
		if (connectionSource != null) {
			connectionSource.close();
		}
	}
	
    	
	//def repo = new OffererRepository();

	handlers {

		// default route
		get {
			render groovyTemplate("index.html", offererList:listOfferer())
		}

		// Form for creating new entries
		get ("new") {
		    render groovyTemplate("new.html")
		}

		// http://localhost:5050/edit/23
		get("edit/:id"){
		  
			println "getting Offerer with ID " + pathTokens.id
			def offerer = getOfferer((pathTokens.id).toInteger())
				  
			render groovyTemplate("edit.html", id: offerer.id, name: offerer.name) 
		}		

		// http://localhost:5050/delete/2
		get("delete/:id"){

			render groovyTemplate("delete.html", id: pathTokens.id)
		}

		// --------------------------------------------------------------------
		// POSTs
		// --------------------------------------------------------------------

		// Data posted from a form
		post ("submit") {
			def form = request.form
	
			def offerer = createOfferer(form.name)

			def message = "Just created Offerer " + offerer.name + " with id " + offerer.id
			println message
	
			render groovyTemplate("index.html", offererList:listOfferer())
			
		}									

		// http://localhost:5050/update/offerer/1
		post ("update/offerer/:id") {

			def form = request.form

			// Update is a save with an id	
			updateOfferer(form.id.toInteger(), form.name)

			render groovyTemplate("index.html", offererList:listOfferer())

		}											

		// http://localhost:5050/delete/offerer/1
		post ("delete/offerer/:id") {

			println "Now deleting offerer with ID: ${pathTokens.id}"
			deleteOfferer(pathTokens.id.toInteger())

			render groovyTemplate("index.html", offererList:listOfferer())			
		}									

		assets "public"
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


private void deleteOfferer(int id) throws SQLException, Exception {
	def offerer = getOfferer(id);
	offererDao.delete(offerer);
}

//INPOT
private Offerer updateOfferer(int id, String name) throws SQLException, Exception {
	
	def offerer = getOfferer(id);
        offerer.setName(name);

        // update the database after changing the object
        offererDao.update(offerer);
        verifyDb(id, offerer);
}

private Offerer getOfferer(int id) throws SQLException, Exception {
        return offererDao.queryForId(id);
}

private Offerer createOfferer(String name) throws Exception {
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
        verifyOfferer(expected, offerer2);
}
/**
 * Verify that the offerer is the same as expected.
 */
private static void verifyOfferer(Offerer expected, Offerer offerer2) {
        assertEquals("expected name does not equal offerer name", expected, offerer2);
}

private List<Offerer> listOfferer() {
	try {
	    return offererDao.queryForAll();
	} catch (SQLException e) {
	    e.printStackTrace();
	    //return list();
	}
}
