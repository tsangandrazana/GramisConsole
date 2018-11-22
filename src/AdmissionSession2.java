import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.table.DatabaseTable;

import java.sql.SQLException;
import java.util.List;

@DatabaseTable(tableName = "admission_session2")
public class AdmissionSession2 {

    public static final String MATRICULE_FIELD_NAME = "matricule_id";
    public static final String MOYENNE_FIELD_NAME = "moyenne_session2";
    public static final String NIVEAU_FIELD_NAME = "niveau_id";
    public static final String ADMISSION_FIELD_NAME = "admis_session2";
    public static final String MENTION_FIELD_NAME = "mention";

    public static Dao<AdmissionSession2, String> admissionSession2Dao;

    @DatabaseField(generatedId = true)
    private int adm_id;

    @DatabaseField(columnName = MATRICULE_FIELD_NAME, canBeNull = false, foreign = true)
    private Etudiant matricule_id;

    @DatabaseField(columnName = NIVEAU_FIELD_NAME, canBeNull = false, foreign = true)
    private Niveau niveau_id;

    @DatabaseField(columnName = MOYENNE_FIELD_NAME, canBeNull = false)
    private Double moyenne_session2;

    @DatabaseField(columnName = ADMISSION_FIELD_NAME, canBeNull = false)
    private String admis_session2;

    @DatabaseField(columnName = MENTION_FIELD_NAME, canBeNull = false)
    private String mention;

    public AdmissionSession2() {
    }

    // Calculer admission en session 2 par niveau
    public static void calculerAdmissionSession2(String niveau, Double deliberation) {
        /*
        Il s'agit de recuperer tous les etudiants du niveau, puis, pour chaque etudiant
        recuperer toutes les moyennes des ECs (calculer la moyenne en passant).
        Si il n'y a aucun EC à refaire, l'etudiant est admis en première session.
        L'insertion dans la BDD se passe à la fin, pour tous les étudiants
         */

        // Delete old data
        try {
            admissionSession2Dao = DaoManager.createDao(Main.connectionSource, AdmissionSession2.class);
            DeleteBuilder<AdmissionSession2, String> admissionSession2StringDeleteBuilder =
                    admissionSession2Dao.deleteBuilder();
            admissionSession2StringDeleteBuilder.where().eq(AdmissionSession2.NIVEAU_FIELD_NAME, niveau);
            admissionSession2StringDeleteBuilder.delete();

            System.out.println("Calcul OK!");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // prendre la liste des étudiants à partir de admission_session1
        for (Etudiant etd : AdmissionSession1.getEtudiantNonAdmisParNiveau(niveau)) {
            //Boolean admis = true;
            Double totalEc = 0.0;
            int ecCounter = 0;
            // Cette partie a été modifié. Desormais l'admission depend uniquement de la moyenne annuelle
            // de la première session
            for (Semestre sem : Semestre.getSemestreByNiveau(niveau)) {
                // recup les moyennes des ec par etudiant
                for (MoyenneEc moyEc : MoyenneEc.moyenneEcParEtudiant(etd.getMatricule_id(), sem.getSemestre_id())) {
                    totalEc += moyEc.getMoyenne();
                    ecCounter++;
                    /*if (moyEc.getRefaire().equals("OUI")) {
                        admis = false;
                    }*/
                }
            }


            // Inserer dans la BDD
            AdmissionSession2 nouvelleAdmission = new AdmissionSession2();
            nouvelleAdmission.setMatricule_id(etd);
            Double moyeAnnee = totalEc / ecCounter;
            nouvelleAdmission.setMoyenne_session2(moyeAnnee);
            nouvelleAdmission.setNiveau_id(Niveau.getNiveauById(niveau));
            if (moyeAnnee >= deliberation) {
                nouvelleAdmission.setAdmis_session2("OUI");
            } else {
                nouvelleAdmission.setAdmis_session2("NON");
            }

            // mention
            if (moyeAnnee >= 16.0) {
                nouvelleAdmission.setMention("TRES BIEN");
            } else if (moyeAnnee >= 14.0 && moyeAnnee < 16.0) {
                nouvelleAdmission.setMention("BIEN");
            } else if (moyeAnnee >= 12.0 && moyeAnnee < 14.0) {
                nouvelleAdmission.setMention("ASSEZ BIEN");
            } else if (moyeAnnee >= deliberation && moyeAnnee < 12.0) {
                nouvelleAdmission.setMention("PASSABLE");
            } else {
                nouvelleAdmission.setMention("AJOURNE");
            }

            // insertion dans la base
            try {
                admissionSession2Dao.create(nouvelleAdmission);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }


    public static List<AdmissionSession2> getAdmisParNiveau(String niveau) {
        try {
            admissionSession2Dao = DaoManager.createDao(Main.connectionSource, AdmissionSession2.class);
            QueryBuilder<AdmissionSession2, String> admissionSession1StringQueryBuilder =
                    admissionSession2Dao.queryBuilder();
            return admissionSession1StringQueryBuilder.where()
                    .eq(NIVEAU_FIELD_NAME, niveau)
                    .and()
                    .eq(ADMISSION_FIELD_NAME, "OUI")
                    .query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<AdmissionSession2> getAdmissionParNiveau(String niveau) {
        try {
            admissionSession2Dao = DaoManager.createDao(Main.connectionSource, AdmissionSession2.class);
            QueryBuilder<AdmissionSession2, String> admissionSession1StringQueryBuilder =
                    admissionSession2Dao.queryBuilder();
            return admissionSession1StringQueryBuilder.where()
                    .eq(NIVEAU_FIELD_NAME, niveau).query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    public Etudiant getMatricule_id() {
        return matricule_id;
    }

    public void setMatricule_id(Etudiant matricule_id) {
        this.matricule_id = matricule_id;
    }

    public Niveau getNiveau_id() {
        return niveau_id;
    }

    public void setNiveau_id(Niveau niveau_id) {
        this.niveau_id = niveau_id;
    }

    public String getMention() {
        return mention;
    }

    public void setMention(String mention) {
        this.mention = mention;
    }

    public int getAdm_id() {
        return adm_id;
    }

    public void setAdm_id(int adm_id) {
        this.adm_id = adm_id;
    }

    public Double getMoyenne_session2() {
        return moyenne_session2;
    }

    public void setMoyenne_session2(Double moyenne_session2) {
        this.moyenne_session2 = moyenne_session2;
    }

    public String getAdmis_session2() {
        return admis_session2;
    }

    public void setAdmis_session2(String admis_session2) {
        this.admis_session2 = admis_session2;
    }
}
