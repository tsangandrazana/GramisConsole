import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@DatabaseTable(tableName = "notes")
public class Notes {
    public static final String EC_FIELD_NAME = "ec_id";
    public static final String TYPE_FIELD_NAME = "type";
    public static final String MATRICULE_FIELD_NAME = "matricule_id";
    public static final String NOTE_FIELD_NAME = "note";
    public static final String CODE_FIELD_NAME = "code";

    public static Dao<Notes, String> notesDao;

    @DatabaseField(generatedId = true)
    private int notes_id;

    @DatabaseField(columnName = EC_FIELD_NAME, canBeNull = false, foreign = true)
    private Ec ec;

    @DatabaseField(columnName = TYPE_FIELD_NAME, canBeNull = false)
    private String type;

    @DatabaseField(columnName = MATRICULE_FIELD_NAME, canBeNull = false, foreign = true)
    private Etudiant matricule;

    @DatabaseField(columnName = NOTE_FIELD_NAME, canBeNull = false)
    private Double note;

    @DatabaseField(columnName = CODE_FIELD_NAME, canBeNull = true)
    private String code;

    public Notes() {
    }

    public static List<Notes> getNotesByEcAndByEtudiant(Ec ec, String matricule) {
        try {
            notesDao = DaoManager.createDao(Main.connectionSource, Notes.class);
            QueryBuilder<Notes, String> notesQueryBuilder =
                    notesDao.queryBuilder();
            return notesQueryBuilder.where()
                    .eq(EC_FIELD_NAME, ec)
                    .and().eq(MATRICULE_FIELD_NAME, matricule)
                    .query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;

    }

    public static List<Notes> getNotesByEc(Ec ec, String type, String code) {
        try {
            notesDao = DaoManager.createDao(Main.connectionSource, Notes.class);
            QueryBuilder<Notes, String> notesQueryBuilder =
                    notesDao.queryBuilder();
            notesQueryBuilder.where()
                    .eq(EC_FIELD_NAME, ec)
                    .and()
                    .eq(TYPE_FIELD_NAME, type)
                    .and()
                    .like(CODE_FIELD_NAME, "%" + code + "%");
            notesQueryBuilder.orderBy(CODE_FIELD_NAME, true);
            return notesQueryBuilder.query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;

    }

    public static List<Notes> getNotesByEtudiant(String matricule) {
        try {
            notesDao = DaoManager.createDao(Main.connectionSource, Notes.class);
            QueryBuilder<Notes, String> notesQueryBuilder =
                    notesDao.queryBuilder();
            return notesQueryBuilder.where().eq(MATRICULE_FIELD_NAME, matricule).query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<Notes> notesParEtudiantParSemestre(String semestre, String matricule) {
        List<Ec> ecParSemestre = Ec.getEcParSemestre(semestre);
        List<Notes> listeNotesParEtudiantParSemestre = new ArrayList<>();
        for (Ec ec : ecParSemestre) {
            //System.out.println(ec.getEc_id() + ": ");
            for (Notes note : Notes.getNotesByEcAndByEtudiant(ec, matricule)) {
                //System.out.println("Type: " + note.getType() + " | Note: " + note.getNote());
                if (note == null) {
                    Notes noteGenerique = new Notes();
                    noteGenerique.setNote(0.0);
                    noteGenerique.setMatricule(Etudiant.getEtudiantByMatricule(matricule));
                    noteGenerique.setEc(ec);
                    noteGenerique.setType("EX");
                    listeNotesParEtudiantParSemestre.add(noteGenerique);
                } else {
                    listeNotesParEtudiantParSemestre.add(note);
                }

            }
        }
        return listeNotesParEtudiantParSemestre;
    }

    public static List<MoyenneEc> moyenneParEc(String semestre, String matricule, String session) {
        List<Ec> ecParSemestre = Ec.getEcParSemestre(semestre);
        List<MoyenneEc> listeMoyenneParEc = new ArrayList<>();
        for (Ec singleEc : ecParSemestre) {
            try {
                int exNumber = 0;
                int ccNumber = 0;
                int tpNumber = 0;
                Map<String, Double> noteParEc = new HashMap<>();
                Double moyenneEc = 0.0;
                // Recupérer toutes les notes pour cet EC
                for (Notes note : notesParEtudiantParSemestre(semestre, matricule)) {
                    if (note.getEc().getEc_id().equals(singleEc.getEc_id())) {
                        switch (note.getType()) {
                            case "EX":
                                exNumber += 1;
                                noteParEc.put("EX", note.getNote());
                                break;
                            case "TP":
                                tpNumber += 1;
                                noteParEc.put("TP", note.getNote());
                                break;
                            case "CC":
                                ccNumber += 1;
                                noteParEc.put("CC", note.getNote());
                                break;

                        }
                    }
                }
                // Obtenir nombre de EX, CC, TP
                Boolean noExam = (exNumber == 0 && ccNumber == 0 && tpNumber == 0);
                Boolean tpOnly = (exNumber == 0 && ccNumber == 0 && tpNumber == 1);
                Boolean ccOnly = (exNumber == 0 && ccNumber == 1 && tpNumber == 0);
                Boolean cc_tp = (exNumber == 0 && ccNumber == 1 && tpNumber == 1);
                Boolean exOnly = (exNumber == 1 && ccNumber == 0 && tpNumber == 0);
                Boolean ex_tp = (exNumber == 1 && ccNumber == 0 && tpNumber == 1);
                Boolean ex_cc = (exNumber == 1 && ccNumber == 1 && tpNumber == 0);
                Boolean ex_cc_tp = (exNumber == 1 && ccNumber == 1 && tpNumber == 1);

                // Calculer Moyenne pour cet EC
                if (noExam) {

                } else if (tpOnly) {
                    moyenneEc = noteParEc.get("TP") * 0.3;
                } else if (ccOnly) {
                    moyenneEc = noteParEc.get("CC") * 0.3;
                } else if (cc_tp) {
                    moyenneEc = noteParEc.get("CC") * 0.3 + noteParEc.get("TP") * 0.3;
                } else if (exOnly) {
                    moyenneEc = noteParEc.get("EX");
                } else if (ex_tp) {
                    moyenneEc = noteParEc.get("EX") * 0.6 + noteParEc.get("TP") * 0.3;
                } else if (ex_cc) {
                    moyenneEc = noteParEc.get("EX") * 0.6 + noteParEc.get("CC") * 0.3;
                } else if (ex_cc_tp) {
                    moyenneEc = noteParEc.get("EX") * 0.4 + noteParEc.get("CC") * 0.3 + noteParEc.get("TP") * 0.3;
                }

                //Display moyenne
                MoyenneEc moyenneEcGenerique = new MoyenneEc();
                moyenneEcGenerique.setEc_id(singleEc);
                moyenneEcGenerique.setMatricule_id(Etudiant.getEtudiantByMatricule(matricule));
                moyenneEcGenerique.setSession(session);
                moyenneEcGenerique.setMoyenne(moyenneEc);
                moyenneEcGenerique.setSemestre_id(Semestre.getSemestreById(semestre));
                if (moyenneEc >= 10.0) {
                    moyenneEcGenerique.setRefaire("NON");
                } else {
                    moyenneEcGenerique.setRefaire("OUI");
                }

                listeMoyenneParEc.add(moyenneEcGenerique);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return listeMoyenneParEc;
    }

    public int getNote_id() {
        return notes_id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Double getNote() {
        return note;
    }

    public void setNote(Double note) {
        this.note = note;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public List<Notes> getAllNotes() {
        List<Notes> allNotes = null;
        try {
            notesDao = DaoManager.createDao(Main.connectionSource, Notes.class);
            allNotes = notesDao.queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return allNotes;
    }

    public Ec getEc() {
        return ec;
    }

    public void setEc(Ec ec) {
        this.ec = ec;
    }

    public Etudiant getMatricule() {
        return matricule;
    }

    public void setMatricule(Etudiant matricule) {
        this.matricule = matricule;
    }

    @Override
    public String toString() {
        return code;
    }
}
