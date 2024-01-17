package server.ultimatepksmash.server.gamesmanager;

import server.ultimatepksmash.server.database.smasher.Smasher;
import server.ultimatepksmash.server.database.user.User;
import server.ultimatepksmash.server.messages.BattleStartResponse;
import server.ultimatepksmash.server.messages.StartRoundReq;
import server.ultimatepksmash.server.messages.StartRoundResp;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
public class GameSession {
    protected List<User> players = new ArrayList<>();
    protected List<Smasher> smashers = new ArrayList<>();
   // private
   protected int numberOfPlayersReady = 0;

    public int getNumberOfPlayersReady() {
        return numberOfPlayersReady;
    }

    protected final int numberOfPlayerInGame;
    protected ReentrantReadWriteLock numberOfPlayersReadyLock = new ReentrantReadWriteLock();
    protected Lock numberOfPlayersReadyRaadLock = numberOfPlayersReadyLock.readLock();
    protected Lock numberOfPlayersReadyWriteLock = numberOfPlayersReadyLock.writeLock();
    private Lock indexLock = new ReentrantLock();
    private int aIndex;
    private int bIndex;
    private StartRoundResp startRoundResp;

    public GameSession(int numberOfPlayerInGame)
    {
        aIndex = 0;
        bIndex = numberOfPlayerInGame/2;
        this.numberOfPlayerInGame = numberOfPlayerInGame;
    }

    private BattleStartResponse battleStart1v1Response = new BattleStartResponse();
    public BattleStartResponse getBattleStartResponse() {

        return battleStart1v1Response;
    }
    protected void addPlayerAndWaitForOthersToJoin(User user, Smasher smasher)
    {
        synchronized (players)
        {
            players.add(user);
           smashers.add(smasher);
           battleStart1v1Response.getSmashers().add(smasher);
           battleStart1v1Response.getPlayersNames().add(user.getUsername());
        }
        waitForOtherPlayers();
    }
    protected void waitForOtherPlayers()
    {
        numberOfPlayersReadyWriteLock.lock();
        numberOfPlayersReady++;
        if(numberOfPlayerInGame != numberOfPlayersReady)
        {
            while (numberOfPlayerInGame != numberOfPlayersReady) {
                numberOfPlayersReadyWriteLock.unlock();
                numberOfPlayersReadyWriteLock.lock();
            }
            if(numberOfPlayersReady == numberOfPlayerInGame) numberOfPlayersReady = 0;
        }
        numberOfPlayersReadyWriteLock.unlock();
    }

    public void isUserPlaying(User user)
    {
        boolean result = false;
        indexLock.lock();
        synchronized (players)
        {
            int indexOf = players.indexOf(user);
            result = indexOf == aIndex || indexOf == bIndex;
        }
        indexLock.unlock();
    }

    public void notifySmasherKilled(Smasher smasher)
    {
        indexLock.lock();
        synchronized (players)
        {
            int indexOf = smashers.indexOf(smasher);
            if(indexOf < numberOfPlayerInGame) aIndex++;
            else bIndex++;
        }
        indexLock.unlock();
    }
    public boolean checkIfTheGameIsStillOn()
    {
        boolean result = true;
        indexLock.lock();
        if(!(aIndex < numberOfPlayerInGame/2)) result = false;
        else if(!(aIndex < numberOfPlayerInGame)) result = false;
        indexLock.unlock();
        return result;
    }
    public StartRoundResp executeRequest(User user, StartRoundReq req)
    {
        numberOfPlayersReadyRaadLock.lock();
        if(numberOfPlayersReady == 0)
        {
            startRoundResp = new StartRoundResp();
        }
        synchronized (players) {
            int userIndex = players.indexOf(user);
            Smasher userSmasher;
            synchronized (smashers) {
                userSmasher = smashers.get(userIndex);
            }

            synchronized (startRoundResp) {
            if (userIndex < numberOfPlayerInGame) {
                //Team A
             startRoundResp.setIdAttackTeamA(req.getIdAttack());
             startRoundResp.setIdDefenceTeamA(req.getIdDefence());
            } else {

                startRoundResp.setIdAttackTeamB(req.getIdAttack());
                startRoundResp.setIdDefenceTeamB(req.getIdDefence());

            }
            if(startRoundResp.getIdAttackTeamA() != null &&
                startRoundResp.getIdAttackTeamB() != null &&
                startRoundResp.getIdDefenceTeamA() != null &&
                startRoundResp.getIdDefenceTeamB() != null
                )
                {
                    synchronized (smashers)
                    {
                        Smasher smasherTeamA = smashers.get(aIndex);
                        Smasher smasherTeamB = smashers.get(bIndex);
                        Double healthPointsTeamA = smasherTeamA.getHealthPoints();
                        Double healthPointsTeamB = smasherTeamB.getHealthPoints();
                        //excute round

                        startRoundResp.setDamageTeamsA(healthPointsTeamA - smasherTeamA.getHealthPoints());
                        startRoundResp.setDamageTeamsB(healthPointsTeamB - smasherTeamB.getHealthPoints());
                        if(smasherTeamA.getHealthPoints() <= 0) startRoundResp.setWasAttackFatalForTeamA(true);
                        if(smasherTeamB.getHealthPoints() <= 0) startRoundResp.setWasAttackFatalForTeamA(true);
                        if(!checkIfTheGameIsStillOn()) startRoundResp.setNextRoundPossible(false);
                    }
                }
            }
        }
        numberOfPlayersReadyRaadLock.unlock();
        waitForOtherPlayers();
        return startRoundResp;
    }

    public StartRoundResp spyRequests(User user, StartRoundReq req) {
        waitForOtherPlayers();
        return startRoundResp;
    }
}
