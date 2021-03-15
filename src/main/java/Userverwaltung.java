import com.mysql.cj.jdbc.MysqlDataSource;
import model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;


public class Userverwaltung {


    private Connection connection;


    private void initConnection() {

        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setDatabaseName("cb");
        dataSource.setUser("root");
        dataSource.setPassword("usbw");
        dataSource.setPort(3307);
        dataSource.setServerName("localhost");
        try {
            Connection connection = dataSource.getConnection();
            System.out.println("Verbindung zu DB hergestellt!");
            // Instanzvariable initialisieren
            this.connection = connection;
        } catch (SQLException ex) {
            // Im Fehlerfall: Fehlermeldung ausgeben
            System.out.println("Fehler bei DB-Connection: " + ex.getMessage());
            ex.printStackTrace();
            // Programm beenden
            System.exit(0);
        }
    }

    private void insertUser(User user) {

        try {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO user (id, name) VALUES (?,?)");
            // werte setzten 1 und 2 referenzieren auf das jeweilige ?
            ps.setInt(1, user.getId());
            ps.setString(2, user.getName());
            // Query ausführen
            ps.executeUpdate();
            // Ressourcen des prepared statement freigeben/vergessen;
            ps.close();
        } catch (SQLException ex) {
            System.out.println("Fehler beim Einfügen! " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private Optional<User> getUserByID(int userId) throws SQLException {

        PreparedStatement ps = connection.prepareStatement(
                "SELECT id, name FROM user WHERE id = ?");
        //Query ausführen
        try (ps) {
            ps.setInt(1, userId);
            ResultSet resultSet = ps.executeQuery();
            if (resultSet.next()) {
                // Einzelne Werte vom Datensatz holen
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                // Daraus ein USer Obejkt erstellen
                User user = new User(id, name);
                return Optional.of(user);
            }
        }
        ps.close();
        return Optional.empty();
    }

    private List<User> getAllUserInDB() throws SQLException {

        PreparedStatement ps = connection.prepareStatement("SELECT id, name FROM user");
        // Query ausführen
        ResultSet resultSet = ps.executeQuery();
        // Liste für Rückgabe
        List<User> result = new ArrayList();
        // Zeile für Zeile result abarbeiten solange es ein next gibt
        while (resultSet.next()) {
            // Einzelne Werte vom Datensatz holen
            int id = resultSet.getInt("id");
            String name = resultSet.getString("name");
            // Daraus ein USer Obejkt erstellen
            User user = new User(id, name);
            //diesen user in resultliste einfügen,
            result.add(user);
        }
        ps.close();
        return result;
    }

    private void verwaltung() {

        Random random = new Random();
        User user1 = new User(random.nextInt(1000), "Walter");
        insertUser(user1);
        System.out.println("Ein User wurde erfolgreich hinzugefügt");

        try {
            List<User> user = getAllUserInDB();
            for (User u : user) {
                print(u);
            }
        } catch (SQLException ex) {
            System.out.println("Fehler beim Auslesen aller User " + ex.getMessage());
        }

        int sucherId = 2;
        System.out.printf("User mit ID %d suchen:\n", sucherId);
        try {
            Optional<User> optionalUser = getUserByID(sucherId);
            if (optionalUser.isPresent()) {
                User user = optionalUser.get();
                print(user);
            } else {
                System.out.println("User leider nicht gefunden! ");
            }
        } catch (SQLException ex) {
            System.out.println("Fehler beim Finden des Users " + ex.getMessage());
        }
    }

    private void closeConnection() {

        try {
            connection.close();
            System.out.println("Datenbankverbindung geschlossen!");
        } catch (SQLException ex) {
            System.out.println("Fehler dei DB-Connection schließen " + ex.getMessage());
        }
    }

    private void print(User user) {
        System.out.printf("%d %s \n", user.getId(), user.getName());
    }

    public static void main(String[] args) {
        Userverwaltung userVerwaltung = new Userverwaltung();

        // DB-Connection erstellen
        userVerwaltung.initConnection();
        // User einfügen und auslesen
        userVerwaltung.verwaltung();
        // DB-Connection schließen
        userVerwaltung.closeConnection();
    }

}


