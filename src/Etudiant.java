import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTable;

import java.sql.SQLException;
import java.util.List;

@DatabaseTable(tableName = "etudiants")
public class Etudiant {

    public static final String MATRICULE_FIELD_NAME = "matricule_id";
    public static final String NOMSPRENOMS_FIELD_NAME = "nomPrenoms";
    public static final String NIVEAU_FIELD_NAME = "niveau_id";

    public static Dao<Etudiant, String> etudiantDao;

    @DatabaseField(columnName = MATRICULE_FIELD_NAME, canBeNull = false, id = true)
    private String matricule_id;

    @DatabaseField(columnName = NOMSPRENOMS_FIELD_NAME, canBeNull = false)
    private String nomPrenoms;

    @DatabaseField(columnName = NIVEAU_FIELD_NAME, canBeNull = false, foreign = true)
    private Niveau niveau;

    public Etudiant() {
    }

    public Etudiant(String matricule) {
        this.matricule_id = matricule;
    }

    public Etudiant(ConnectionSource connectionSource) {
        try {
            etudiantDao = DaoManager.createDao(connectionSource, Etudiant.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<Etudiant> getAllEtudiants() {
        List<Etudiant> allEtudiant = null;
        try {
            allEtudiant = etudiantDao.queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return allEtudiant;
    }

    public static List<Etudiant> getEtudiantByNiveau(String niveau) {
        try {
            etudiantDao = DaoManager.createDao(Main.connectionSource, Etudiant.class);
            QueryBuilder<Etudiant, String> etdQueryBuilder =
                    etudiantDao.queryBuilder();
            etdQueryBuilder.where().eq(NIVEAU_FIELD_NAME, niveau);
            etdQueryBuilder.orderBy(NOMSPRENOMS_FIELD_NAME, true);
            return etdQueryBuilder.query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Etudiant getEtudiantByMatricule(String matricule) {
        try {
            etudiantDao = DaoManager.createDao(Main.connectionSource, Etudiant.class);
            QueryBuilder<Etudiant, String> etdQueryBuilder =
                    etudiantDao.queryBuilder();
            Etudiant etd = etdQueryBuilder.where().eq(MATRICULE_FIELD_NAME, matricule).queryForFirst();
            return etd;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Etudiant getEtudiantByName(String name) {
        try {
            etudiantDao = DaoManager.createDao(Main.connectionSource, Etudiant.class);
            QueryBuilder<Etudiant, String> etdQueryBuilder =
                    etudiantDao.queryBuilder();
            return etdQueryBuilder.where().eq(NOMSPRENOMS_FIELD_NAME, name).queryForFirst();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getMatricule_id() {
        return matricule_id;
    }

    public void setMatricule_id(String matricule_id) {
        this.matricule_id = matricule_id;
    }

    public String getNomPrenoms() {
        return nomPrenoms;
    }

    public void setNomPrenoms(String nomPrenoms) {
        this.nomPrenoms = nomPrenoms;
    }

    public Niveau getNiveau() {
        return niveau;
    }

    public void setNiveau(Niveau niveau) {
        this.niveau = niveau;
    }

    /*public static List<Etudiant> getEtudiantParNiveau(String niveau) {
        // QueryBuilder construction
        List<Etudiant> etudiantParNiveau = null;
        try {
            etudiantDao = DaoManager.createDao(Main.connectionSource, Etudiant.class);
            QueryBuilder<Etudiant, String> queryBuilder =
                    etudiantDao.queryBuilder();
            etudiantParNiveau = etudiantDao.query(
                    etudiantDao.queryBuilder().where()
                            .eq(Etudiant.NIVEAU_FIELD_NAME, niveau)
                            .prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return etudiantParNiveau;
    }*/

    @Override
    public String toString() {
        return nomPrenoms;
    }


}
