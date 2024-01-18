package server.ultimatepksmash.server.messages;

import lombok.AllArgsConstructor;
import lombok.Data;
import server.ultimatepksmash.server.database.smasher.Smasher;

import java.io.Serializable;
@Data
@AllArgsConstructor
public class BattleStart2v2Response implements Serializable {
    private Smasher mySmasher;
    private Smasher opnoentsSmasher;
}
