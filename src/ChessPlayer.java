public class ChessPlayer {
    protected int hp;
    protected int mana;
    protected int playerDeck; // num cards remain in deck
    protected int playerRune;
    protected int playerDraw;

    public ChessPlayer(int hp, int mana, int playerDeck, int playerRune, int playerDraw) {
        this.hp = hp;
        this.mana = mana;
        this.playerDeck = playerDeck;
        this.playerRune = playerRune;
        this.playerDraw = playerDraw;
    }

    public ChessPlayer(ChessPlayer player) {
        this.hp = player.hp;
        this.mana = player.mana;
        this.playerDeck = player.playerDeck;
        this.playerRune = player.playerRune;
        this.playerDraw = player.playerDraw;
    }
}
