import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.stmt.DeleteBuilder;
import org.beryx.textio.TextIO;
import org.beryx.textio.TextIoFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Scanner;

public class Main {

    private final static String DATABASE_URL = "jdbc:mariadb://localhost:3306/gramis18?user=root&password=Ecclesiaste1213";
    public static JdbcConnectionSource connectionSource;

    public static void calculerMoyenneEc(String semestre, String niveau, String session) {
        try {
            MoyenneEc.moyenneEcDao = DaoManager.createDao(connectionSource, MoyenneEc.class);
            DeleteBuilder<MoyenneEc, String> moyenneEcDeleteBuilder = MoyenneEc.moyenneEcDao.deleteBuilder();
            for (Etudiant etd : Etudiant.getEtudiantByNiveau(niveau)) {
                for (MoyenneEc moyenneEc : Notes.moyenneParEc(semestre, etd.getMatricule_id(), session)) {
                    moyenneEcDeleteBuilder.where().eq(MoyenneEc.EC_FIELD_NAME, moyenneEc.getEc_id())
                            .and().eq(MoyenneEc.MATRICULE_FIELD_NAME, moyenneEc.getMatricule_id());
                    moyenneEcDeleteBuilder.delete();
                    MoyenneEc.moyenneEcDao.create(moyenneEc);
                }
            }
            System.out.println("Calcul OK!");
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public static void calculerMoyenneUe(String niveau, String semestre) {
        try {
            MoyenneUe.moyenneUeDao = DaoManager.createDao(connectionSource, MoyenneUe.class);
            DeleteBuilder<MoyenneUe, String> moyenneUeStringDeleteBuilder =
                    MoyenneUe.moyenneUeDao.deleteBuilder();
            for (MoyenneUe moyUe : MoyenneUe.moyenneUeParSemestre(niveau, semestre)) {
                moyenneUeStringDeleteBuilder.where()
                        .eq(MoyenneUe.MATRICULE_FIELD_NAME, moyUe.getMatricule_id().getMatricule_id())
                        .and()
                        .eq(MoyenneUe.UE_FIELD_NAME, moyUe.getUe_id().getUe_id());
                moyenneUeStringDeleteBuilder.delete();
                MoyenneUe.moyenneUeDao.create(moyUe);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        // Try to connect to the mariadb server
        try {
            connectionSource = new JdbcConnectionSource(DATABASE_URL);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // destroy the data source which should close underlying connections
            if (connectionSource != null) {
                connectionSource.close();
            }
        }
        TextIO textIO = TextIoFactory.getTextIO();
        new StartGramis().accept(textIO, null);
    }
}
