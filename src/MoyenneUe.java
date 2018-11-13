import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.table.DatabaseTable;

import java.awt.event.MouseAdapter;
import java.util.ArrayList;
import java.util.List;

@DatabaseTable(tableName = "moyennes_ue")
public class MoyenneUe {
    public static final String UE_FIELD_NAME = "ue_id";
    public static final String MATRICULE_FIELD_NAME = "matricule_id";
    public static final String MOYENNE_FIELD_NAME = "moyenne_ue";
    public static final String NIVEAU_FIELD_NAME = "niveau_id";
    public static final String SEMESTRE_FIELD_NAME = "semestre_id";

    public static Dao<MoyenneUe, String> moyenneUeDao;

    @DatabaseField(generatedId = true)
    private int moyenne_ue_id;

    @DatabaseField(columnName = UE_FIELD_NAME, canBeNull = false, foreign = true)
    private Ue ue_id;

    @DatabaseField(columnName = MATRICULE_FIELD_NAME, canBeNull = false, foreign = true)
    private Etudiant matricule_id;

    @DatabaseField(columnName = MOYENNE_FIELD_NAME, canBeNull = false)
    private Double moyenne_ue;

    @DatabaseField(columnName = SEMESTRE_FIELD_NAME, canBeNull = false, foreign = true)
    private Semestre semestre_id;

    @DatabaseField(columnName = NIVEAU_FIELD_NAME, canBeNull = false, foreign = true)
    private Niveau niveau;

    public MoyenneUe() {
    }

    public static List<MoyenneUe> moyenneUeParSemestre(String niveau, String semestre) {

        List<MoyenneUe> listeMoyenneUe = new ArrayList<>();

        for (Etudiant etd : Etudiant.getEtudiantByNiveau(niveau)) {
            for (Ue ue : Ue.ueParSemestre(semestre)) {
                MoyenneUe nouveauMoyenneUe = new MoyenneUe();
                Double moyenneUe = 0.0;
                int ecParUeCount = 0;
                for (Ec ec : Ec.getEcParUe(ue.getUe_id())) {
                    Double totalMoyenneEc = 0.0;
                    ecParUeCount += 1;
                    for (MoyenneEc moyEc : MoyenneEc.moyenneEcParEtudiant(etd.getMatricule_id(), semestre)) {
                        if (moyEc.getEc_id().getEc_id().equals(ec.getEc_id())) {
                            totalMoyenneEc += moyEc.getMoyenne();
                        }
                    }
                    moyenneUe = totalMoyenneEc / ecParUeCount;

                    // Creation MoyenneUe
                    nouveauMoyenneUe.setMatricule_id(etd);
                    nouveauMoyenneUe.setMoyenne_ue(moyenneUe);
                    nouveauMoyenneUe.setNiveau(Niveau.getNiveauById(niveau));
                    nouveauMoyenneUe.setSemestre_id(Semestre.getSemestreById(semestre));
                    nouveauMoyenneUe.setUe_id(Ue.getUeById(ue.getUe_id()));

                    listeMoyenneUe.add(nouveauMoyenneUe);
                }

            }
        }
        return listeMoyenneUe;
    }

    public int getMoyenne_ue_id() {
        return moyenne_ue_id;
    }

    public void setMoyenne_ue_id(int moyenne_ue_id) {
        this.moyenne_ue_id = moyenne_ue_id;
    }

    public Ue getUe_id() {
        return ue_id;
    }

    public void setUe_id(Ue ue_id) {
        this.ue_id = ue_id;
    }

    public Etudiant getMatricule_id() {
        return matricule_id;
    }

    public void setMatricule_id(Etudiant matricule_id) {
        this.matricule_id = matricule_id;
    }

    public Double getMoyenne_ue() {
        return moyenne_ue;
    }

    public void setMoyenne_ue(Double moyenne_ue) {
        this.moyenne_ue = moyenne_ue;
    }

    public Semestre getSemestre_id() {
        return semestre_id;
    }

    public void setSemestre_id(Semestre semestre_id) {
        this.semestre_id = semestre_id;
    }

    public Niveau getNiveau() {
        return niveau;
    }

    public void setNiveau(Niveau niveau) {
        this.niveau = niveau;
    }

}
