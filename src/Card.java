public class Card {
    protected int cardNumber;
    protected int instanceId;
    protected CardLocation location;
    protected CardType cardType;
    protected int cost;
    protected int attack;
    protected int defense;
    protected int hpChange;
    protected int hpChangeEnemy;
    protected int cardDraw;
    protected String abilities;

    protected int index;

    protected boolean canAttack;  // creature can always attack if it is on board when reading

    protected boolean breakthrough;
    protected boolean charge;
    protected boolean guard;
    protected boolean lethal;
    protected boolean drain;
    protected boolean ward;

    public Card(int cardNumber, int instanceId, CardLocation location, CardType cardType, int cost, int attack, int defense, int hpChange, int hpChangeEnemy, int cardDraw, String abilities, int index, boolean canAttack) {
        this.cardNumber = cardNumber;
        this.instanceId = instanceId;
        this.location = location;
        this.cardType = cardType;
        this.cost = cost;
        this.attack = attack;
        this.defense = defense;
        this.hpChange = hpChange;
        this.hpChangeEnemy = hpChangeEnemy;
        this.cardDraw = cardDraw;
        this.abilities = abilities;
        this.index = index;
        this.canAttack = canAttack;

        this.breakthrough = false;
        this.charge = false;
        this.guard = false;
        this.lethal = false;
        this.drain = false;
        this.ward = false;
    }

    public Card(Card card) {
        this.cardNumber = card.cardNumber;
        this.instanceId = card.instanceId;
        this.location = card.location;
        this.cardType = card.cardType;
        this.cost = card.cost;
        this.attack = card.attack;
        this.defense = card.defense;
        this.hpChange = card.hpChange;
        this.hpChangeEnemy = card.hpChangeEnemy;
        this.cardDraw = card.cardDraw;
        this.abilities = card.abilities;
        this.index = card.index;
        this.canAttack = card.canAttack;

        this.breakthrough = card.breakthrough;
        this.charge = card.charge;
        this.guard = card.guard;
        this.lethal = card.lethal;
        this.drain = card.drain;
        this.ward = card.ward;
    }
}
