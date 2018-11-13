import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import org.beryx.textio.EnumInputReader;
import org.beryx.textio.TextIO;
import org.beryx.textio.TextTerminal;
import org.beryx.textio.web.RunnerData;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import static java.awt.Color.yellow;


public class StartGramis implements BiConsumer<TextIO, RunnerData> {

    public static Map<Integer, Object> listToMap(List<?> list) {
        Map<Integer, Object> mp = new HashMap<>();
        int counter = 1;
        for (Object listItem : list) {
            mp.put(counter, listItem);
            counter += 1;
        }
        return mp;
    }

    @Override
    public void accept(TextIO textIO, RunnerData runnerData) {

        TextTerminal<?> terminal = textIO.getTextTerminal();

        /* Create a big loop */
        String quitGramis = "";
        String initData = (runnerData == null) ? null : runnerData.getInitData();
        AppUtil.printGsonMessage(terminal, initData);
        terminal.getProperties().setInputColor(yellow);

        /* Display a menu
         * Choose between:
         * 1. Calculer Moyenne EC
         * 2. Calculer Moyenne UE
         * 3. Saisir Notes
         * */
        terminal.println("Bienvenue Dans Gramis !");
        terminal.println("-------------------------");
        Boolean quitApp = true;
        while (quitApp) {
            terminal.println("Menu:");
            terminal.println("1. Calculer Moyenne EC");
            terminal.println("2. Calculer Moyenne UE");
            terminal.println("3. Saisir Notes - Session 1");
            terminal.println("4. Calculer Admission Session 1");
            terminal.println("5. Saisir Notes - Session 2");
            terminal.println("6. Calculer Admission Session 2");
            terminal.println("7. Quitter");
            int choixMenu = textIO.newIntInputReader()
                    .read("Votre choix ? ");
            if (choixMenu == 1) {
                String niveau = textIO.newStringInputReader()
                        .read("Niveau: ");
                String semestre = textIO.newStringInputReader()
                        .read("Semestre: ");
                String session = textIO.newStringInputReader()
                        .read("Session: ");
                Main.calculerMoyenneEc(semestre, niveau, session);
            } else if (choixMenu == 2) {
                String niveau = textIO.newStringInputReader()
                        .read("Niveau: ");
                String semestre = textIO.newStringInputReader()
                        .read("Semestre: ");
                Main.calculerMoyenneUe(niveau, semestre);
            } else if (choixMenu == 3) {
                //<editor-fold desc="Main Gramis Loop">
                while (!quitGramis.equals("quit")) {

                    // Choix du Niveau
                    Map<Integer, Object> niveauMap = listToMap(Niveau.getAllNiveau());
                    terminal.println("Niveau: ");
                    printHashmap(niveauMap, textIO);
                    int numNiveau = textIO.newIntInputReader().read("Choisir Niveau: ");

                    // Choix du Semestre
                    SEMESTRE semestre = textIO.newEnumInputReader(SEMESTRE.class)
                            .read();

                    // Choix des UE
                    terminal.println("\nUE: ");
                    Map<Integer, Object> ueMap = listToMap(Ue.ueParSemestre(semestre.toString()));
                    printHashmap(ueMap, textIO);
                    int numUE = textIO.newIntInputReader().read("Choisir UE: ");

                    // Choix des EC
                    terminal.println("\nEC: ");
                    Map<Integer, Object> ecMap = listToMap(Ec.getEcParUe(ueMap.get(numUE).toString()));
                    printHashmap(ecMap, textIO);
                    int numEC = textIO.newIntInputReader().read("Choisir EC: ");

                    // EC ou CC/TP
                    terminal.println("EX/CC/TP: ");
                    TypeEpreuve type = textIO.newEnumInputReader(TypeEpreuve.class)
                            .read("Type d'Epreuve: ");

                    /* Do some preparation for database insertion */
                    QueryBuilder<Notes, String> notesStringQueryBuilder = null;
                    UpdateBuilder<Notes, String> notesStringUpdateBuilder = null;
                    try {
                        Notes.notesDao = DaoManager.createDao(Main.connectionSource, Notes.class);
                        notesStringQueryBuilder = Notes.notesDao.queryBuilder();
                        notesStringUpdateBuilder = Notes.notesDao.updateBuilder();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                    // Code ou Note
                    //<editor-fold desc="Code ou Notes">
                    switch (type.toString()) {
                        case "EX":
                            CodeNote codeNote = textIO.newEnumInputReader(CodeNote.class).read("Code ou Note: ");
                            // if CODE
                            if (codeNote.toString().equals("CODE")) {
                                String lettreCode = textIO.newStringInputReader().read("Lettre du Code");
                                // Affichage des Etudiants
                                terminal.println("Liste Etudiants:");
                                Map<Integer, Object> etudiantMap =
                                        listToMap(Etudiant.getEtudiantByNiveau(niveauMap.get(numNiveau).toString()));
                                String exitCode = "";
                                printHashmap(etudiantMap, textIO);

                                while (!exitCode.equals("quit")) {
                                    terminal.resetLine();
                                    int numEtudiant = textIO.newIntInputReader().read("Tapez votre choix: ");
                                    String valeurCode = textIO.newStringInputReader().read("Code: ");
                                    // insertion dans base
                                    //Things we need: ec_id, type, matricule_id, code, note = 0.0
                                    Ec ec_id_ex_code = Ec.getEcbyID(ecMap.get(numEC).toString());
                                    String type_ex_code = "EX";
                                    Etudiant matricule_id = (Etudiant) etudiantMap.get(numEtudiant);

                                    /* Create a new note */
                                    Notes nouveauNote = new Notes();
                                    nouveauNote.setType(type_ex_code);
                                    nouveauNote.setEc(ec_id_ex_code);
                                    nouveauNote.setMatricule(matricule_id);
                                    nouveauNote.setNote(0.0);
                                    nouveauNote.setCode(lettreCode + valeurCode);

                                    /* Now we are going to insert a new row into the database */
                                    try {
                                        Notes.notesDao.create(nouveauNote);
                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                    }

                                    exitCode = textIO.newStringInputReader()
                                            .withDefaultValue("Non").read("Continuer ? [Taper 'quit' pour quitter]");
                                }
                            }

                            // if NOTE
                            if (codeNote.toString().equals("NOTE")) {
                                String lettreCode = textIO.newStringInputReader().read("Lettre du Code");
                                // Affichage liste CODE par EC
                                //terminal.println("Liste Code: ");
                                Map<Integer, Object> codeMap =
                                        listToMap(Notes.getNotesByEc(Ec.getEcbyID(ecMap.get(numEC).toString()), "EX", lettreCode));
                                String exitCode = "";
                                printHashmap(codeMap, textIO);
                                while (!exitCode.equals("quit")) {
                                    terminal.resetLine();
                                    int numCode = textIO.newIntInputReader().read("Tapez votre choix: ");
                                    Double valeurNote = textIO.newDoubleInputReader().read("Note: ");

                                    // Mise à jour note pour code
                                    try {
                                        notesStringUpdateBuilder.where().eq(Notes.EC_FIELD_NAME, ecMap.get(numEC).toString())
                                                .and()
                                                .eq(Notes.CODE_FIELD_NAME, codeMap.get(numCode).toString());
                                        notesStringUpdateBuilder.updateColumnValue(Notes.NOTE_FIELD_NAME, valeurNote).update();
                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                    }

                                    exitCode = textIO.newStringInputReader()
                                            .withDefaultValue("Non").read("Continuer ? [Taper 'quit' pour quitter]");
                                }
                            }
                            break;
                        case "CC":
                            // Affichage des Etudiants
                            //terminal.println("Liste Etudiants:");
                            Map<Integer, Object> etudiantMapCC =
                                    listToMap(Etudiant.getEtudiantByNiveau(niveauMap.get(numNiveau).toString()));
                            String exitCodeCC = "";
                            printHashmap(etudiantMapCC, textIO);
                            while (!exitCodeCC.equals("quit")) {
                                terminal.resetLine();
                                //printHashmap(etudiantMapCC, textIO);
                                int numEtudiantCC = textIO.newIntInputReader().read("Tapez votre choix: ");
                                Double valeurNote = textIO.newDoubleInputReader().read("Note: ");

                                // Insertion dans base
                                //Things we need: ec_id, type, matricule_id, code = NULL, note
                                Ec ec_id_cc = Ec.getEcbyID(ecMap.get(numEC).toString());
                                String type_cc = "CC";
                                Etudiant matricule_id_cc = (Etudiant) etudiantMapCC.get(numEtudiantCC);

                                /* Create a new note */
                                Notes nouveauNote = new Notes();
                                nouveauNote.setType(type_cc);
                                nouveauNote.setEc(ec_id_cc);
                                nouveauNote.setMatricule(matricule_id_cc);
                                nouveauNote.setNote(valeurNote);

                                /* Now we are going to insert a new row into the database */
                                try {
                                    Notes.notesDao.create(nouveauNote);
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }

                                exitCodeCC = textIO.newStringInputReader()
                                        .withDefaultValue("Non").read("Continuer ? [Taper 'quit' pour quitter]");
                            }
                            break;
                        case "TP":
                            // Affichage des Etudiants
                            //terminal.println("Liste Etudiants:");
                            Map<Integer, Object> etudiantMapTP =
                                    listToMap(Etudiant.getEtudiantByNiveau(niveauMap.get(numNiveau).toString()));
                            String exitCodeTP = "";
                            printHashmap(etudiantMapTP, textIO);
                            while (!exitCodeTP.equals("quit")) {
                                terminal.resetLine();
                                int numEtudiantTP = textIO.newIntInputReader().read("Tapez votre choix: ");
                                Double valeurNote = textIO.newDoubleInputReader().read("Note: ");

                                // Insertion dans base
                                //Things we need: ec_id, type, matricule_id, code = NULL, note
                                Ec ec_id_tp = Ec.getEcbyID(ecMap.get(numEC).toString());
                                String type_tp = "TP";
                                Etudiant matricule_id_tp = (Etudiant) etudiantMapTP.get(numEtudiantTP);

                                /* Create a new note */
                                Notes nouveauNote = new Notes();
                                nouveauNote.setType(type_tp);
                                nouveauNote.setEc(ec_id_tp);
                                nouveauNote.setMatricule(matricule_id_tp);
                                nouveauNote.setNote(valeurNote);

                                /* Now we are going to insert a new row into the database */
                                try {
                                    Notes.notesDao.create(nouveauNote);
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }

                                exitCodeTP = textIO.newStringInputReader()
                                        .withDefaultValue("Non").read("Continuer ? [Taper 'quit' pour quitter]");
                            }
                            break;

                    }
                    //</editor-fold>
                    //textIO.newStringInputReader().withMinLength(0).read("\nPress enter to terminate...");
                    quitGramis = textIO.newStringInputReader()
                            .withDefaultValue("Non").read("Continuer Gramis? [Taper 'quit' pour quitter]");
                    if (quitGramis.equals("quit")) {
                        textIO.dispose();
                    } else {
                        terminal.moveToLineStart();
                    }
                }
                //</editor-fold>
            } else if (choixMenu == 4) {
                String niveau = textIO.newStringInputReader()
                        .read("Niveau: ");
                // Recuperer la liste de tous les étudiants du niveau
                AdmissionSession1.calculerAdmissionSession1(niveau);
                // Mise à jour Ec à refaire
                AdmissionSession1.mettreAJourEcARefaire(niveau);

            } else if (choixMenu == 5) {
                //<editor-fold desc="Saisie Session 2">
                while (!quitGramis.equals("quit")) {

                    // Choix du Niveau
                    Map<Integer, Object> niveauMap = listToMap(Niveau.getAllNiveau());
                    terminal.println("Niveau: ");
                    printHashmap(niveauMap, textIO);
                    int numNiveau = textIO.newIntInputReader().read("Choisir Niveau: ");

                    // Choix du Semestre
                    SEMESTRE semestre = textIO.newEnumInputReader(SEMESTRE.class)
                            .read();

                    // Choix des UE
                    terminal.println("\nUE: ");
                    Map<Integer, Object> ueMap = listToMap(Ue.ueParSemestre(semestre.toString()));
                    printHashmap(ueMap, textIO);
                    int numUE = textIO.newIntInputReader().read("Choisir UE: ");

                    // Choix des EC
                    terminal.println("\nEC: ");
                    Map<Integer, Object> ecMap = listToMap(Ec.getEcParUe(ueMap.get(numUE).toString()));
                    printHashmap(ecMap, textIO);
                    int numEC = textIO.newIntInputReader().read("Choisir EC: ");

                    // Saisie Code ou note
                    CodeNote codeNote = textIO.newEnumInputReader(CodeNote.class).read("Code ou Note: ");

                    MoyenneEc.moyenneEcDao = null;
                    UpdateBuilder<MoyenneEc, String> moyenneEcStringUpdateBuilder = null;
                    try {
                        MoyenneEc.moyenneEcDao = DaoManager.createDao(Main.connectionSource, MoyenneEc.class);
                        moyenneEcStringUpdateBuilder = MoyenneEc.moyenneEcDao.updateBuilder();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                    // if CODE
                    if (codeNote.toString().equals("CODE")) {
                        String lettreCode = textIO.newStringInputReader().read("Lettre du Code");
                        // Affichage des Etudiants
                        terminal.println("Liste Etudiants:");
                        Map<Integer, Object> etudiantMap =
                                listToMap(MoyenneEc.listeEtudiantPourSession2(semestre.toString(), ecMap.get(numEC).toString()));
                        String exitCode = "";
                        printHashmap(etudiantMap, textIO);
                        try {
                            while (!exitCode.equals("quit")) {
                                terminal.resetLine();
                                int numEtudiant = textIO.newIntInputReader().read("Tapez votre choix: ");
                                String valeurCode = textIO.newStringInputReader().read("Code: ");
                                // mettre à jour le code de la Session2
                                moyenneEcStringUpdateBuilder.where()
                                        .eq(MoyenneEc.MATRICULE_FIELD_NAME,
                                            Etudiant.getEtudiantByName(etudiantMap.get(numEtudiant).toString()).getMatricule_id())
                                        .and().eq(MoyenneEc.EC_FIELD_NAME, ecMap.get(numEC).toString())
                                        .and().eq(MoyenneEc.SEMESTRE_FIELD_NAME, semestre.toString());
                                moyenneEcStringUpdateBuilder
                                        .updateColumnValue(MoyenneEc.SESSION2_FIELD_NAME, lettreCode + valeurCode);
                                moyenneEcStringUpdateBuilder.update();

                                exitCode = textIO.newStringInputReader()
                                        .withDefaultValue("Non").read("Continuer ? [Taper 'quit' pour quitter]");
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }

                    // if NOTE
                    if (codeNote.toString().equals("NOTE")) {
                        String lettreCode = textIO.newStringInputReader().read("Lettre du Code");
                        // Affichage liste CODE par EC
                        //terminal.println("Liste Code: ");
                        Map<Integer, Object> codeMap =
                                listToMap(MoyenneEc.listeMoyenneEcPourSession
                                        (semestre.toString(), ecMap.get(numEC).toString()));
                        String exitCode = "";
                        printHashmap(codeMap, textIO);
                        while (!exitCode.equals("quit")) {
                            terminal.resetLine();
                            int numCode = textIO.newIntInputReader().read("Tapez votre choix: ");
                            Double valeurNote = textIO.newDoubleInputReader().read("Note: ");

                            // Mise à jour note pour code
                            try {
                                moyenneEcStringUpdateBuilder.where().eq(MoyenneEc.EC_FIELD_NAME, ecMap.get(numEC).toString())
                                        .and()
                                        .eq(MoyenneEc.SESSION2_FIELD_NAME, codeMap.get(numCode).toString());
                                moyenneEcStringUpdateBuilder.updateColumnValue(MoyenneEc.MOYENNE_FIELD_NAME, valeurNote).update();
                                moyenneEcStringUpdateBuilder.updateColumnValue(MoyenneEc.SESSION_FIELD_NAME, "Session2").update();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }

                            exitCode = textIO.newStringInputReader()
                                    .withDefaultValue("Non").read("Continuer ? [Taper 'quit' pour quitter]");
                        }
                    }
                }
                //</editor-fold>
            } else if (choixMenu == 6) {
                // Calcul admission Session 2
            } else if (choixMenu == 7) {
                terminal.dispose();
                quitApp = false;
            }
        }

    }

    public void printHashmap(Map<Integer, Object> map, TextIO textIO) {
        TextTerminal<?> terminal = textIO.getTextTerminal();
        int limitCounter;
        if (map.size() < 3) {
            limitCounter = map.size();
        } else {
            limitCounter = (map.size() / 3) + 1;
        }
        for (int printCounter = 1; printCounter <= limitCounter; printCounter++) {
            int printIncrementer = limitCounter;
            int secondCol = printCounter + printIncrementer;
            int thirdCol = printCounter + printIncrementer * 2;
            // Check if map values is null, then do not print null values
            if (map.get(secondCol) == null) {
                terminal.printf("%-60.35s%n",
                                printCounter + ":" + map.get(printCounter));
            } else if (map.get(thirdCol) == null) {
                terminal.printf("%-60.35s\t|  %-60.35s%n",
                                printCounter + ":" + map.get(printCounter),
                                secondCol + ":" + map.get(secondCol));
            } else {
                terminal.printf("%-60.35s\t|  %-60.35s\t| %-60.35s%n",
                                printCounter + ":" + map.get(printCounter),
                                secondCol + ":" + map.get(secondCol),
                                thirdCol + ":" + map.get(thirdCol));
            }
        }
    }
}