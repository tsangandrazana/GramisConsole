import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTable;

import java.sql.SQLException;
import java.util.List;

@DatabaseTable(tableName = "ec")
public class Ec {

    public static final String EC_FIELD_NAME = "ec_id";
    public static final String UE_FIELD_NAME = "ue_id";


    public static Dao<Ec, String> ecDao;

    @DatabaseField(columnName = EC_FIELD_NAME, canBeNull = false, id = true)
    private String ec_id;

    @DatabaseField(columnName = UE_FIELD_NAME, canBeNull = false, foreign = true)
    private Ue ue;

    public Ec() {
    }

    public static List<Ec> getEcParSemestre(String semestre) {

        try {
            ecDao = DaoManager.createDao(Main.connectionSource, Ec.class);
            Ue.ueDao = DaoManager.createDao(Main.connectionSource, Ue.class);
            Semestre.semestreDao = DaoManager.createDao(Main.connectionSource, Semestre.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // QueryBuilder construction
        QueryBuilder<Ec, String> ecQueryBuilder =
                ecDao.queryBuilder();


        QueryBuilder<Ue, String> ueQueryBuilder =
                Ue.ueDao.queryBuilder();

        QueryBuilder<Semestre, String> semestreQueryBuilder =
                Semestre.semestreDao.queryBuilder();
        try {
            semestreQueryBuilder.where().eq(Semestre.SEMESTRE_FIELD_NAME, semestre);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            ueQueryBuilder.join(semestreQueryBuilder).query();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            return ecQueryBuilder.join(ueQueryBuilder).query();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static List<Ec> getEcParUe(String ue) {

        try {
            ecDao = DaoManager.createDao(Main.connectionSource, Ec.class);
            // QueryBuilder construction
            QueryBuilder<Ec, String> ecQueryBuilder =
                    ecDao.queryBuilder();
            return ecQueryBuilder.where().eq(UE_FIELD_NAME, ue).query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Ec getEcbyID(String ec_id) {
        try {
            ecDao = DaoManager.createDao(Main.connectionSource, Ec.class);
            QueryBuilder<Ec, String> ecStringQueryBuilder =
                    ecDao.queryBuilder();
            return ecStringQueryBuilder.where().eq(EC_FIELD_NAME, ec_id).queryForFirst();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getEc_id() {
        return ec_id;
    }

    public void setEc_id(String ec_id) {
        this.ec_id = ec_id;
    }

    public Ue getUe() {
        return ue;
    }

    public void setUe(Ue ue) {
        this.ue = ue;
    }

    @Override
    public String toString() {
        return ec_id;
    }
}
