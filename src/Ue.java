import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTable;

import java.sql.SQLException;
import java.util.List;

@DatabaseTable(tableName = "ue")
public class Ue {

    public static final String SEMESTRE_FIELD_NAME = "semestre_id";
    public static final String UE_FIELD_NAME = "ue_id";

    public static Dao<Ue, String> ueDao;

    @DatabaseField(columnName = SEMESTRE_FIELD_NAME, canBeNull = false, foreign = true)
    private Semestre semestre;

    @DatabaseField(columnName = UE_FIELD_NAME, canBeNull = false, id = true)
    private String ue_id;

    public Ue() {
    }

    public static List<Ue> ueParSemestre(String semestre) {
        try {
            ueDao = DaoManager.createDao(Main.connectionSource, Ue.class);
            QueryBuilder<Ue, String> ueQueryBuilder =
                    ueDao.queryBuilder();
            return ueQueryBuilder.where().eq(SEMESTRE_FIELD_NAME, semestre).query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Ue getUeById(String ue_id) {
        try {
            ueDao = DaoManager.createDao(Main.connectionSource, Ue.class);
            QueryBuilder<Ue, String> ueStringQueryBuilder =
                    ueDao.queryBuilder();
            return ueStringQueryBuilder.where().eq(UE_FIELD_NAME, ue_id).queryForFirst();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Semestre getSemestre() {
        return semestre;
    }

    public void setSemestre(Semestre semestre) {
        this.semestre = semestre;
    }

    public String getUe_id() {
        return ue_id;
    }

    public void setUe_id(String ue_id) {
        this.ue_id = ue_id;
    }

    @Override
    public String toString() {
        return ue_id;
    }
}