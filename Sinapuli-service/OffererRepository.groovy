import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.db.H2DatabaseType;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;

import java.sql.SQLException;
import java.util.List;

public class OffererRepository {
    private Dao<Offerer, Integer> offererDao;
    String databaseUrl = "jdbc:h2:mem:offerer";

    public OffererRepository() {
        ConnectionSource connectionSource = null;
        try {
		connectionSource = new JdbcConnectionSource(databaseUrl, new H2DatabaseType());
		offererDao = DaoManager.createDao(connectionSource, Offerer.class);

        } catch (SQLException e) {
            e.printStackTrace();  
        }
    }

    public List<Offerer> list() {
        try {
            return offererDao.queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
            return list();
        }
    }
}
