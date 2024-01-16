package server.ultimatepksmash.server;

import java.io.IOException;
import java.sql.SQLException;

import static server.ultimatepksmash.server.database.DataBaseService.closeConnection;
import static server.ultimatepksmash.server.database.DataBaseService.connectToDatabase;

public class Main {
    public static void main(String[] args) throws SQLException {
        try {
            connectToDatabase();
            Server server = new Server(25800);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        finally {
         closeConnection();
        }
    }
}
