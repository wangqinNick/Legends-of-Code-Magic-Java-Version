import java.util.ArrayList;

public class ManaCurve {
    protected int [] curve;
    protected int creatureCount;

    public ManaCurve() {
        this.curve = new int[ConstantField.MAX_MANA+1];
        this.creatureCount = 0;
    }

    public void computeCurve(ArrayList<Card> cards) {
        for (Card card: cards) {
            curve[card.cost] += 1;
            if (card.cardType == CardType.CREATURE) creatureCount += 1;
        }
    }

    public int evaluateScore(){
        int sevenPlusCount = 0;
        for (int i = 0; i < ConstantField.MAX_MANA+1; i++) {
            sevenPlusCount += curve[i];
        }

        return Math.abs(curve[0] - ConstantField.ZERO_MANA) +
                Math.abs(curve[1] - ConstantField.ONE_MANA) +
                Math.abs(curve[2] - ConstantField.TWO_MANA) +
                Math.abs(curve[3] - ConstantField.THREE_MANA) +
                Math.abs(curve[4] - ConstantField.FOUR_MANA) +
                Math.abs(curve[5] - ConstantField.FIVE_MANA) +
                Math.abs(curve[6] - ConstantField.SIX_MANA) +
                Math.abs(sevenPlusCount - ConstantField.SEVEN_PLUS_MANA);
    }
}
