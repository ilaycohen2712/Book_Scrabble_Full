package Model;

import Model.gameClasses.Player;
import Model.gameClasses.Tile;

import java.util.List;
import java.util.Observable;

abstract public class PlayerModel extends Observable  implements Model {
    Player myPlayer;
    int currentPlayerId;
    boolean isGameFinished;

    public List<Character> getMyTiles() {
        return myPlayer.getTiles();
    }
    public void setCurrentPlayerId(int currentPlayerId) {
        this.currentPlayerId = currentPlayerId;
    }
    public int getCurrentPlayerId(){
        return currentPlayerId;
    }
}
