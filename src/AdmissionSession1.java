import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.table.DatabaseTable;

import java.sql.SQLException;
import java.util.List;

@DatabaseTable(tableName = "admission_session1")
public class AdmissionSession1 {

    public static final String MATRICULE_FIELD_NAME = "matricule_id";
    public static final String MOYENNE_FIELD_NAME = "moyenne_session1";
    public static final String NIVEAU_FIELD_NAME = "niveau_id";
    public static final String ADMISSION_FIELD_NAME = "admis_session1";
    public static final String MENTION_FIELD_NAME = "mention";

    public static Dao<AdmissionSession1, String> admissionSession1Dao;

    @DatabaseField(generatedId = true)
    private int admission_id;

    @DatabaseField(columnName = MATRICULE_FIELD_NAME, canBeNull = false, foreign = true)
    private Etudiant matricule_id;

    @DatabaseField(columnName = NIVEAU_FIELD_NAME, canBeNull = false, foreign = true)
    private Niveau niveau_id;

    @DatabaseField(columnName = MOYENNE_FIELD_NAME, canBeNull = false)
    private Double moyenne_session1;

    @DatabaseField(columnName = ADMISSION_FIELD_NAME, canBeNull = false)
    private String admis_session1;

    @DatabaseField(columnName = MENTION_FIELD_NAME, canBeNull = false)
    private String mention;

    public AdmissionSession1() {
    }

    // Calculer admission en session 1 par niveau
    public static void calculerAdmissionSession1(String niveau) {
        /*
        Il s'agit de recuperer tous les etudiants du niveau, puis, pour chaque etudiant
        recuperer toutes les moyennes des ECs (calculer la moyenne en passant).
        Si il n'y a aucun EC à refaire, l'etudiant est admis en première session.
        L'insertion dans la BDD se passe à la fin, pour tous les étudiants
         */

        // Delete old data
        try {
            admissionSession1Dao = DaoManager.createDao(Main.connectionSource, AdmissionSession1.class);
            DeleteBuilder<AdmissionSession1, String> admissionSession1StringDeleteBuilder =
                    admissionSession1Dao.deleteBuilder();
            for (Etudiant etd : Etudiant.getEtudiantByNiveau(niveau)) {
                admissionSession1StringDeleteBuilder.where().eq(AdmissionSession1.NIVEAU_FIELD_NAME, niveau);
                admissionSession1StringDeleteBuilder.delete();
            }
            System.out.println("Calcul OK!");
        } catch (SQLException e) {
            e.printStackTrace();
        }


        for (Etudiant etd : Etudiant.getEtudiantByNiveau(niveau)) {
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
            AdmissionSession1 nouvelleAdmission = new AdmissionSession1();
            nouvelleAdmission.setMatricule_id(etd);
            Double moyeAnnee = totalEc / ecCounter;
            nouvelleAdmission.setMoyenne_session1(moyeAnnee);
            nouvelleAdmission.setNiveau_id(Niveau.getNiveauById(niveau));
            if (moyeAnnee >= 10.0) {
                nouvelleAdmission.setAdmis_session1("OUI");
            } else {
                nouvelleAdmission.setAdmis_session1("NON");
            }

            // mention
            if (moyeAnnee >= 16.0) {
                nouvelleAdmission.setMention("TRES BIEN");
            } else if (moyeAnnee >= 14.0 && moyeAnnee < 16.0) {
                nouvelleAdmission.setMention("BIEN");
            } else if (moyeAnnee >= 12.0 && moyeAnnee < 14.0) {
                nouvelleAdmission.setMention("ASSEZ BIEN");
            } else if (moyeAnnee >= 10.0 && moyeAnnee < 12.0) {
                nouvelleAdmission.setMention("PASSABLE");
            } else {
                nouvelleAdmission.setMention("AJOURNE");
            }

            // insertion dans la base
            try {
                admissionSession1Dao.create(nouvelleAdmission);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

    // Mettre à jour EC à repasser en modifiant le champ 'refaire' dans notes
    public static void mettreAJourEcARefaire(String niveau) {
        /*
        Si l'étudiant est admis en première session, toutes ses Ec qui sont à refaire
        sont annulées.
        Pour ce faire, récupérer la liste des étudiants admis, puis pour chaque étudiant
        recupérer la liste des Ecs à refaire et mettre à jour cette liste
         */
        // Créer un UpdateBuilder
        UpdateBuilder<MoyenneEc, String> moyenneEcStringUpdateBuilder = null;
        try {
            MoyenneEc.moyenneEcDao = DaoManager.createDao(Main.connectionSource, MoyenneEc.class);
            moyenneEcStringUpdateBuilder =
                    MoyenneEc.moyenneEcDao.updateBuilder();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Recupération de la liste des étudiants admis par niveau et mise à jour
        for (AdmissionSession1 admis : getAdmisParNiveau(niveau)) {
            for (Semestre sem : Semestre.getSemestreByNiveau(niveau)) {
                for (MoyenneEc ecARefaire : MoyenneEc.moyenneEcParEtudiant(admis.matricule_id.getMatricule_id(), sem.getSemestre_id())) {

                    if (ecARefaire.getRefaire().equals("OUI")) {
                        try {
                            moyenneEcStringUpdateBuilder.where()
                                    .eq(MoyenneEc.EC_FIELD_NAME, ecARefaire.getEc_id().getEc_id())
                                    .and()
                                    .eq(MoyenneEc.MATRICULE_FIELD_NAME, admis.getMatricule_id().getMatricule_id())
                                    .and()
                                    .eq(MoyenneEc.SEMESTRE_FIELD_NAME, sem.getSemestre_id());
                            moyenneEcStringUpdateBuilder
                                    .updateColumnValue("refaire", "NON")
                                    .update();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    public static List<AdmissionSession1> getAdmisParNiveau(String niveau) {
        try {
            admissionSession1Dao = DaoManager.createDao(Main.connectionSource, AdmissionSession1.class);
            QueryBuilder<AdmissionSession1, String> admissionSession1StringQueryBuilder =
                    admissionSession1Dao.queryBuilder();
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

    public static List<AdmissionSession1> getAdmissionParNiveau(String niveau) {
        try {
            admissionSession1Dao = DaoManager.createDao(Main.connectionSource, AdmissionSession1.class);
            QueryBuilder<AdmissionSession1, String> admissionSession1StringQueryBuilder =
                    admissionSession1Dao.queryBuilder();
            return admissionSession1StringQueryBuilder.where()
                    .eq(NIVEAU_FIELD_NAME, niveau).query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getAdmission_id() {
        return admission_id;
    }

    public void setAdmission_id(int admission_id) {
        this.admission_id = admission_id;
    }

    public Etudiant getMatricule_id() {
        return matricule_id;
    }

    public void setMatricule_id(Etudiant matricule_id) {
        this.matricule_id = matricule_id;
    }

    public Double getMoyenne_session1() {
        return moyenne_session1;
    }

    public void setMoyenne_session1(Double moyenne_session1) {
        this.moyenne_session1 = moyenne_session1;
    }

    public Niveau getNiveau_id() {
        return niveau_id;
    }

    public void setNiveau_id(Niveau niveau_id) {
        this.niveau_id = niveau_id;
    }

    public String getAdmis_session1() {
        return admis_session1;
    }

    public void setAdmis_session1(String admis_session1) {
        this.admis_session1 = admis_session1;
    }

    public String getMention() {
        return mention;
    }

    public void setMention(String mention) {
        this.mention = mention;
    }
}
