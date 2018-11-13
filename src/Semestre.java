import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTable;

import java.sql.SQLException;
import java.util.List;

@DatabaseTable(tableName = "semestre")
public class Semestre {

    public static final String SEMESTRE_FIELD_NAME = "semestre_id";
    public static final String NIVEAU_FIELD_NAME = "niveau_id";

    public static Dao<Semestre, String> semestreDao;

    @DatabaseField(columnName = SEMESTRE_FIELD_NAME, canBeNull = false, id = true)
    private String semestre_id;

    @DatabaseField(columnName = NIVEAU_FIELD_NAME, canBeNull = false, foreign = true)
    private Niveau niveau;

    public Semestre() {
    }

    public static Semestre getSemestreById(String semestre_id) {
        try {
            semestreDao = DaoManager.createDao(Main.connectionSource, Semestre.class);
            QueryBuilder<Semestre, String> semestreStringQueryBuilder =
                    semestreDao.queryBuilder();
            return semestreStringQueryBuilder.where().eq(SEMESTRE_FIELD_NAME, semestre_id).queryForFirst();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<Semestre> getSemestreByNiveau(String niveau) {
        try {
            semestreDao = DaoManager.createDao(Main.connectionSource, Semestre.class);
            QueryBuilder<Semestre, String> semestreStringQueryBuilder =
                    semestreDao.queryBuilder();
            return semestreStringQueryBuilder.where().eq(NIVEAU_FIELD_NAME, niveau).query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getSemestre_id() {
        return semestre_id;
    }

    public void setSemestre_id(String semestre_id) {
        this.semestre_id = semestre_id;
    }

    public Niveau getNiveau() {
        return niveau;
    }

    public void setNiveau(Niveau niveau) {
        this.niveau = niveau;
    }
}
