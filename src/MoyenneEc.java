import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.table.DatabaseTable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@DatabaseTable(tableName = "moyennes_ec")
public class MoyenneEc {
    public static final String EC_FIELD_NAME = "ec_id";
    public static final String SEMESTRE_FIELD_NAME = "semestre_id";
    public static final String MATRICULE_FIELD_NAME = "matricule_id";
    public static final String MOYENNE_FIELD_NAME = "moyenne";
    public static final String SESSION_FIELD_NAME = "session";
    public static final String SESSION2_FIELD_NAME = "code_session2";

    public static Dao<MoyenneEc, String> moyenneEcDao;

    @DatabaseField(generatedId = true)
    private int moyenneEc_id;

    @DatabaseField(columnName = EC_FIELD_NAME, canBeNull = false, foreign = true)
    private Ec ec_id;

    @DatabaseField(columnName = SEMESTRE_FIELD_NAME, canBeNull = false, foreign = true)
    private Semestre semestre_id;

    @DatabaseField(columnName = MATRICULE_FIELD_NAME, canBeNull = false, foreign = true)
    private Etudiant matricule_id;

    @DatabaseField(columnName = MOYENNE_FIELD_NAME, canBeNull = false)
    private Double moyenne;

    @DatabaseField(columnName = SESSION_FIELD_NAME, canBeNull = false)
    private String session;

    @DatabaseField(columnName = SESSION2_FIELD_NAME, canBeNull = true)
    private String code_session2;

    @DatabaseField(columnName = "refaire", canBeNull = true)
    private String refaire;

    public MoyenneEc() {
    }

    public static List<MoyenneEc> moyenneEcParEtudiant(String matricule, String semestre) {

        try {
            moyenneEcDao = DaoManager.createDao(Main.connectionSource, MoyenneEc.class);
            QueryBuilder<MoyenneEc, String> moyenneEcStringQueryBuilder =
                    moyenneEcDao.queryBuilder();
            return moyenneEcStringQueryBuilder.where().eq(MATRICULE_FIELD_NAME, matricule)
                    .and().eq(SEMESTRE_FIELD_NAME, semestre).query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /* Saisie code Session 2
    Nous avons besoin du Semestre, de l'UE et de l'EC, de la liste des étudiants qui on refait l'epreuve
     */
    public static List<Etudiant> listeEtudiantPourSession2(String semestre, String ec_id) {
        List<Etudiant> etudiantPourSession2 = new ArrayList<>();
        try {
            //1. Obtenir la liste des étudiants devant refaire l'epreuve
            moyenneEcDao = DaoManager.createDao(Main.connectionSource, MoyenneEc.class);
            Etudiant.etudiantDao = DaoManager.createDao(Main.connectionSource, Etudiant.class);

            QueryBuilder<MoyenneEc, String> moyenneEcStringQueryBuilder = moyenneEcDao.queryBuilder();
            QueryBuilder<Etudiant, String> etudiantStringQueryBuilder = Etudiant.etudiantDao.queryBuilder();

            List<MoyenneEc> moyEcListForLoop = null;
            moyenneEcStringQueryBuilder.where()
                    .eq(EC_FIELD_NAME, ec_id).and().eq(SEMESTRE_FIELD_NAME, semestre)
                    .and().eq("refaire", "OUI");
            etudiantStringQueryBuilder.join(moyenneEcStringQueryBuilder);
            etudiantStringQueryBuilder.orderBy(Etudiant.NOMSPRENOMS_FIELD_NAME, true);
            etudiantPourSession2 = etudiantStringQueryBuilder.query();

            /*for (MoyenneEc moyEc : ) {
                Etudiant newEtd = Etudiant.getEtudiantByMatricule(moyEc.getMatricule_id().getMatricule_id());
                etudiantPourSession2.add(newEtd);
            }*/
            return etudiantPourSession2;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return etudiantPourSession2;
    }

    public static List<MoyenneEc> listeMoyenneEcPourSession(String semestre, String ec_id) {
        List<MoyenneEc> moyenneEcPourSession2 = new ArrayList<>();
        try {
            //1. Obtenir la liste des étudiants devant refaire l'epreuve
            moyenneEcDao = DaoManager.createDao(Main.connectionSource, MoyenneEc.class);
            QueryBuilder<MoyenneEc, String> moyenneEcStringQueryBuilder = moyenneEcDao.queryBuilder();
            List<MoyenneEc> moyEcForLoop = null;
            moyenneEcStringQueryBuilder.where()
                    .eq(EC_FIELD_NAME, ec_id).and().eq(SEMESTRE_FIELD_NAME, semestre)
                    .and().eq("refaire", "OUI");
            moyenneEcStringQueryBuilder.orderBy(MoyenneEc.SESSION2_FIELD_NAME, true);
            moyEcForLoop = moyenneEcStringQueryBuilder.query();
            for (MoyenneEc moyEc : moyEcForLoop) {
                moyenneEcPourSession2.add(moyEc);
            }
            return moyenneEcPourSession2;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return moyenneEcPourSession2;
    }

    public int getMoyenneEc_id() {
        return moyenneEc_id;
    }

    public void setMoyenneEc_id(int moyenneEc_id) {
        this.moyenneEc_id = moyenneEc_id;
    }

    public Ec getEc_id() {
        return ec_id;
    }

    public void setEc_id(Ec ec_id) {
        this.ec_id = ec_id;
    }

    public Semestre getSemestre_id() {
        return semestre_id;
    }

    public void setSemestre_id(Semestre semestre_id) {
        this.semestre_id = semestre_id;
    }

    public Etudiant getMatricule_id() {
        return matricule_id;
    }

    public void setMatricule_id(Etudiant matricule_id) {
        this.matricule_id = matricule_id;
    }

    public Double getMoyenne() {
        return moyenne;
    }

    public void setMoyenne(Double moyenne) {
        this.moyenne = moyenne;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public String getCode_session2() {
        return code_session2;
    }

    public void setCode_session2(String code_session2) {
        this.code_session2 = code_session2;
    }

    public String getRefaire() {
        return refaire;
    }

    public void setRefaire(String refaire) {
        this.refaire = refaire;
    }

    @Override
    public String toString() {
        return code_session2;
    }
}
