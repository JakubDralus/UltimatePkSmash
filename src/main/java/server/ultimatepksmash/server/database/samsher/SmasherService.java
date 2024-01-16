package server.ultimatepksmash.server.database.samsher;

import server.ultimatepksmash.server.database.DataBaseService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class SmasherService {
    private final Connection connection = DataBaseService.connection;
    
    public List<Smasher> getSmashers() throws SQLException {
        PreparedStatement getSmashers = connection.prepareStatement("SELECT * FROM p_smasher");
        ResultSet resultSet = getSmashers.executeQuery();
        
        List<Smasher> smashers = new ArrayList<>();
        // Iterate over the rows
        while (resultSet.next()) {
            Smasher smasher = new Smasher();
            mapSmasher(smasher, resultSet);
            smashers.add(smasher);
        }
        
        resultSet.close();
        getSmashers.close();
        return smashers;
    }
    
    // returns list of smashers owned by a user (id)
    public List<Smasher> getUserSmashers(Long userId) throws SQLException {
        String sql = "select * from p_smasher ps join p_smasher_user psu on psu.smasher_id = ps.id where psu.user_id = ?;";
        PreparedStatement getSmashers = connection.prepareStatement(sql);
        getSmashers.setLong(1, userId);
        ResultSet resultSet = getSmashers.executeQuery();
        
        List<Smasher> smashers = new ArrayList<>();
        // Iterate over the rows
        while (resultSet.next()) {
            Smasher smasher = new Smasher();
            mapSmasher(smasher, resultSet);
            smashers.add(smasher);
        }
        
        resultSet.close();
        getSmashers.close();
        return smashers;
    }
    
    // sets empty smasher fields with data returned by database
    private static void mapSmasher(Smasher smasher, ResultSet resultSet) throws SQLException {
        smasher.setId(resultSet.getLong("id"));
        smasher.setName(resultSet.getString("name"));
        smasher.setDescription(resultSet.getString("description"));
        smasher.setHealthPoints(resultSet.getString("health_points"));
        smasher.setPhotoPath("none"); //todo: make it later
    }
    
}