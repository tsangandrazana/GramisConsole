import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTable;

import java.sql.SQLException;
import java.util.List;

@DatabaseTable(tableName = "niveau")
public class Niveau {
    public static final String NIVEAU_FIELD_NAME = "niveau_id";

    public static Dao<Niveau, String> niveauDao;

    @DatabaseField(columnName = NIVEAU_FIELD_NAME, canBeNull = false, id = true)
    private String niveau_id;

    public Niveau() {
    }

    public Niveau(ConnectionSource connectionSource) {
        try {
            niveauDao = DaoManager.createDao(connectionSource, Niveau.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Niveau getNiveauById(String niveau_id) {
        try {
            niveauDao = DaoManager.createDao(Main.connectionSource, Niveau.class);
            QueryBuilder<Niveau, String> niveauStringQueryBuilder =
                    niveauDao.queryBuilder();
            return niveauStringQueryBuilder.where().eq(NIVEAU_FIELD_NAME, niveau_id).queryForFirst();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<Niveau> getAllNiveau() {
        try {
            niveauDao = DaoManager.createDao(Main.connectionSource, Niveau.class);
            return niveauDao.queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getNiveau_id() {
        return niveau_id;
    }

    public void setNiveau_id(String niveau_id) {
        this.niveau_id = niveau_id;
    }

    @Override
    public String toString() {
        return niveau_id;
    }
}
