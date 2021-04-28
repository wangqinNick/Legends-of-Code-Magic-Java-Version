import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import static java.lang.Float.NEGATIVE_INFINITY;
import static java.lang.Float.POSITIVE_INFINITY;
import java.util.ArrayList;
import java.util.ArrayList;


class Agent {
    protected State state;
    protected Turn bestTurn;
    protected Timeout timeout;

    public Agent() {
        state = new State();
        bestTurn = new Turn();
        timeout = new Timeout();
    }

    public void read() {
        state = new State();

        Scanner in = new Scanner(System.in);
        for (int i = 0; i < 2; i++) {
            int playerHealth = in.nextInt();
            int playerMana = in.nextInt();
            int playerDeck = in.nextInt();
            int playerRune = in.nextInt();
            int playerDraw = in.nextInt();

            state.players[i] = new ChessPlayer(playerHealth, playerMana, playerDeck, playerRune, playerDraw);
        }
        state.opponentHand = in.nextInt();

        int opponentActions = in.nextInt();
        if (in.hasNextLine()) {
            in.nextLine();
        }

        for (int i = 0; i < opponentActions; i++) {
            String cardNumberAndAction = in.nextLine();
            state.cardNumberAndAction.add(cardNumberAndAction);
        }

        int cardCount = in.nextInt();
        for (int i = 0; i < cardCount; i++) {
            int cardNumber = in.nextInt();
            int instanceId = in.nextInt();

            CardLocation cardLocation;

            /* 0: in the player's hand
               1: on the player's side of the board
               -1: on the opponent's side of the board */
            switch (in.nextInt()) {
                case -1: cardLocation = CardLocation.OPPONENT; break;
                case 0: cardLocation = CardLocation.IN_HAND; break;
                case 1: cardLocation = CardLocation.MINE; break;
                default: cardLocation = CardLocation.OUT_OF_PLAY;
            }

            CardType cardType;
            switch (in.nextInt()){
                case 0: cardType = CardType.CREATURE; break;
                case 1: cardType = CardType.GREEN_ITEM; break;
                case 2: cardType = CardType.RED_ITEM; break;
                default: cardType = CardType.BLUE_ITEM; break;
            }
            int cost = in.nextInt();
            int attack = in.nextInt();
            int defense = in.nextInt();
            String abilities = in.next();

            int myHealthChange = in.nextInt();
            int opponentHealthChange = in.nextInt();
            int cardDraw = in.nextInt();

            boolean canAttack = cardLocation == CardLocation.MINE || cardLocation == CardLocation.OPPONENT;

            Card card = new Card(cardNumber, instanceId, cardLocation, cardType, cost,
                                 attack, defense, myHealthChange, opponentHealthChange,
                                 cardDraw,
                                 abilities, i, canAttack);

            if (card.cardType == CardType.CREATURE) {
                if (card.location == CardLocation.MINE) {
                    state.myCreatureIndexList.add(card.index);
                } else if (card.location == CardLocation.OPPONENT) {
                    state.opponentCreatureList.add(card.index);
                }
            }
            for (int j = 0; j < card.abilities.length(); j++){
                char c = abilities.charAt(j);
                switch (c) {
                    case 'B': card.breakthrough = true; break;
                    case 'L': card.lethal = true; break;
                    case 'G': card.guard = true; break;
                    case 'C': card.charge = true; break;
                    case 'W': card.ward = true; break;
                    case 'D': card.drain = true; break;
                    default: break;//assert false: "Invalid card ability";
                }
            }
            state.cards.add(card);
        }

        timeout.start();
    }

    public void print() {
        bestTurn.print(state);
    }

    public static double evaluateState(State state) {
        ChessPlayer myPlayer = state.players[0];
        ChessPlayer opponent = state.players[1];
        if (myPlayer.hp <= 0) return NEGATIVE_INFINITY;
        if (opponent.hp <= 0) return POSITIVE_INFINITY;

        double hpScore = 0;
        hpScore = hpScore + myPlayer.hp - opponent.hp;

        double creatureScore = 0;
        for (Card creature: state.cards) {
            switch (creature.location) {
                case MINE: creatureScore = creatureScore + creature.attack + creature.defense; break;
                case OPPONENT: creatureScore = creatureScore - creature.attack - creature.defense; break;
                default: break;
            }
        }
        return hpScore + creatureScore;
    }

    public Action getRandomAction(State state) {
        ArrayList<Action> actions = state.generateActionList();
        if (actions.isEmpty()) return null;

        int actionIndex = new Random().nextInt(actions.size());
        return actions.get(actionIndex);
    }

    public void think(boolean isDebugMode) {
        bestTurn.clear();

        if (state.isInDraft()) {

        } else {

            double bestScore = NEGATIVE_INFINITY;

            while (!timeout.isElapsed(ConstantField.MAX_SPAN_SECONDS)) {
                State newState = new State(state);
                Turn turn = new Turn();
                while (true) {
                    Action action = getRandomAction(newState);
                    if (action == null) break;
                    turn.actions.add(action);
                    newState.update(action);
                    if (isDebugMode) {
                        if (action.index_target == ConstantField.OPPONENT_FACE) {
                            Log.log(String.format("Action: %s %d FACE", action.actionType, action.index));
                        } else {
                            Log.log(String.format("Action: %s %d %d", action.actionType, action.index, action.index_target));
                        }
                    }
                }
                double score = evaluateState(newState);
                if (isDebugMode) {
                    Log.log(String.format("%f", score));
                    Log.log("-----------------------");
                }
                if (score > bestScore) {
                    bestScore = score;
                    bestTurn = turn;
                }
            }
        }
    }
    public void debug() {
        Log.log("My creatures: ");
        for (int index: state.myCreatureIndexList) {
            Card card = state.cards.get(index);
            if (card.location == CardLocation.MINE) {
                Log.log(String.format("ID: %d", card.instanceId));
            }
        }

        Log.log("His creatures: ");
        for (int index: state.myCreatureIndexList) {
            Card card = state.cards.get(index);
            if (card.location == CardLocation.OPPONENT) {
                Log.log(String.format("ID: %d", card.instanceId));
            }
        }
    }
}

class Turn {
    protected ArrayList<Action> actions;

    public Turn() {
        this.actions = new ArrayList<>();
    }

    public void clear() {
        actions.clear();
    }

    public void print(State state) {
        int num_actions = actions.size();
        if (num_actions == 0) {
            System.out.println("PASS");
        } else {
            for (int i = 0; i < actions.size(); i++) {
                Action action = actions.get(i);
                if (action == null) {
                    Log.log(String.format("size: %d", actions.size()));
                } else {
                    action.print(state, i == num_actions - 1);  // true if isEnd
                }
            }
        }
    }
}
enum ActionType {
    PASS("PASS"),
    SUMMON("SUMMON"),
    ATTACK("ATTACK"),
    PICK("PICK"),
    USE("USE");

    protected String value;

    ActionType(String value) {
        this.value = value;
    }
}
class Card {
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
class Log {
    public static void log(String msg) {
        System.err.println(msg);
    }
}
class Player {

    public static void main(String[] args) {
        Agent agent = new Agent();

        // game loop
        while (true) {
            agent.read();
            agent.think(false);
            //agent.debug();
            agent.print();
        }
    }
}
class Action {
    protected ActionType actionType;
    protected int index;
    protected int index_target;

    public void pass(){
        actionType = ActionType.PASS;
    }

    public void summon(int index){
        actionType = ActionType.SUMMON;
        this.index = index;
    }

    public void attack(int index, int index_target){
        actionType = ActionType.ATTACK;
        this.index = index;
        this.index_target = index_target;
    }

    public void pick(int index){
        actionType = ActionType.PICK;
        this.index = index;
    }

    public void use(int index, int index_target){
        actionType = ActionType.USE;
        this.index = index;
        this.index_target = index_target;
    }

    public void print(State state, boolean isEnd){
        Card card;
        Card target;

        switch (actionType) {

            case PICK:
                System.out.printf("PICK %d;", index);
                break;

            case SUMMON:
                card = state.cards.get(index);
                System.out.printf("SUMMON %d;", card.instanceId);
                break;

            case USE:
                card = state.cards.get(index);
                if (index_target == -1) {
                    System.out.printf("USE %d %d;", card.instanceId, ConstantField.OPPONENT_FACE);
                } else {
                    target = state.cards.get(index_target);
                    System.out.printf("USE %d %d;", card.instanceId, target.instanceId);
                }
                break;

            case ATTACK:
                card = state.cards.get(index);
                if (index_target == -1) {
                    System.out.printf("ATTACK %d %d;", card.instanceId, ConstantField.OPPONENT_FACE);
                } else {
                    target = state.cards.get(index_target);
                    System.out.printf("ATTACK %d %d;", card.instanceId, target.instanceId);
                }
                break;

            default:
                System.out.println("PASS");
                Log.log("Unknown action type");
                break;
        } // end of switch

        if (isEnd) {
            System.out.println();
        }
    }
}
enum CardType {
    CREATURE(0),
    GREEN_ITEM(1),
    RED_ITEM(2),
    BLUE_ITEM(3);

    protected int value;

    CardType(int value) {
        this.value = value;
    }
}
class ChessPlayer {
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
final class ConstantField {

    protected static final int MAX_MANA = 12;
    protected static final int CARDS_PER_DRAFT = 3;
    protected static final int MAX_CREATURES_IN_PLAY = 6;
    protected static final int OPPONENT_FACE = -1;
    protected static final double MAX_SPAN_SECONDS = 80;
}
enum CardLocation {
    OPPONENT(-1),
    IN_HAND(0),
    MINE(1),
    OUT_OF_PLAY(2);

    protected int value;

    CardLocation(int value) {
        this.value = value;
    }
}


class Timeout {
    protected long startTime;

    public void start() {
        startTime = System.currentTimeMillis();
        Log.log(String.format("%d", startTime));
    }

    public boolean isElapsed(double maxSpanSeconds) {
        long currentTime = System.currentTimeMillis();
        return currentTime - startTime >= maxSpanSeconds;
    }
}

class State {
    protected ChessPlayer[] players;
    protected ArrayList<Card> cards;
    protected int opponentHand;
    protected int opponentActions;
    protected ArrayList<String> cardNumberAndAction;

    protected ArrayList<Integer> myCreatureIndexList;
    protected ArrayList<Integer> opponentCreatureList;

    protected ArrayList<Action> legalActionList;

    public State() {
        this.players = new ChessPlayer[2];
        this.cards = new ArrayList<>();

        this.opponentActions = 0;
        this.opponentHand = 0;
        this.cardNumberAndAction = new ArrayList<>();
        this.legalActionList = new ArrayList<>();

        this.myCreatureIndexList = new ArrayList<>();
        this.opponentCreatureList = new ArrayList<>();
    }

    public State(State state) {
        ChessPlayer myPlayer = new ChessPlayer(state.players[0]);
        ChessPlayer opponent = new ChessPlayer(state.players[1]);
        this.players = new ChessPlayer[]{myPlayer, opponent};
        this.cards = new ArrayList<>();
        for (Card card : state.cards) {
            Card card_copy = new Card(card);
            this.cards.add(card_copy);
        }
        this.opponentHand = state.opponentHand;
        this.opponentActions = state.opponentActions;
        this.cardNumberAndAction = new ArrayList<>();
        this.cardNumberAndAction.addAll(state.cardNumberAndAction);
        this.myCreatureIndexList = new ArrayList<>();
        this.myCreatureIndexList.addAll(state.myCreatureIndexList);
        this.opponentCreatureList = new ArrayList<>();
        this.opponentCreatureList.addAll(state.opponentCreatureList);
        this.legalActionList = new ArrayList<>();
    }

    public boolean isInDraft() {
        return players[0].mana == 0;
    }

    public ArrayList<Action> generateActionList() {
        legalActionList.clear();
        ChessPlayer myPlayer = players[0];
        Action action;
        for (Card card : cards) {

            switch (card.location) {
                case IN_HAND:
                    if (card.cost > myPlayer.mana) break;
                    switch (card.cardType) {
                        case CREATURE:
                            if (myCreatureIndexList.size() >= ConstantField.MAX_CREATURES_IN_PLAY) break;
                            action = new Action();
                            action.summon(card.index);
                            legalActionList.add(action);
                            break;
                        case BLUE_ITEM:
                            action = new Action();
                            action.use(card.index, ConstantField.OPPONENT_FACE);
                            legalActionList.add(action);
                            break;
                        case RED_ITEM:
                            if (opponentCreatureList.isEmpty()) break;
                            for (int index_target : opponentCreatureList) {
                                action = new Action();
                                action.use(card.index, index_target);
                                legalActionList.add(action);
                            }
                            break;
                        case GREEN_ITEM:
                            if (myCreatureIndexList.isEmpty()) break;
                            for (int index : myCreatureIndexList) {
                                action = new Action();
                                action.use(card.index, index);
                                legalActionList.add(action);
                            }
                            break;
                    }
                    break;
                case MINE:
                    if (card.cardType != CardType.CREATURE) break;
                    if (!card.canAttack) break;
                    // Attack an opponent's creature
                    ArrayList<Integer> guardList = new ArrayList<>();
                    if (!opponentCreatureList.isEmpty()) {
                        for (int index_target : opponentCreatureList) {
                            Card target = cards.get(index_target);
                            if (target.guard) {
                                guardList.add(index_target);
                            }
                        }
                    }
                    if (guardList.size() == 0) {
                        // Attack the opponent
                        action = new Action();
                        action.attack(card.index, ConstantField.OPPONENT_FACE);
                        legalActionList.add(action);
                        for (int index_target : opponentCreatureList) {
                            action = new Action();
                            action.attack(card.index, index_target);
                            legalActionList.add(action);
                        }
                    } else {
                        // Attack the guard first
                        for (int index_target : guardList) {
                            action = new Action();
                            action.attack(card.index, index_target);
                            legalActionList.add(action);
                        }
                    }
                    break;
                default:
                    break;
            }
        }
        return legalActionList;
    }

    public void update(Action action) {
        ChessPlayer myPlayer = players[0];
        ChessPlayer opponent = players[1];
        Card card = cards.get(action.index);
        switch (action.actionType) {
            case SUMMON:
                assert card.cardType == CardType.CREATURE : "Attempt to summon a non-creature card";
                assert card.cost <= myPlayer.mana : "Attempt to summon a creature without enough mana";

                card.location = CardLocation.MINE;
                card.canAttack = card.charge;

                myPlayer.mana -= card.cost;
                myCreatureIndexList.add(card.index);
                myPlayer.playerDraw += card.cardDraw;
                myPlayer.hp += card.hpChange;
                opponent.hp += card.hpChangeEnemy;
                break;
            case ATTACK:
                assert card.location == CardLocation.MINE : "Attempt to attack with an attacker that I do not control";
                if (!card.canAttack) break;
                boolean found_guard = false;
                boolean attacking_guard = false;
                for (int index_target : opponentCreatureList) {  // find guards
                    Card target = cards.get(index_target);
                    if (target.location == CardLocation.OPPONENT) {
                        if (target.guard) {
                            found_guard = true;
                            assert action.index_target != ConstantField.OPPONENT_FACE : "Attempt to attack the opponent face when he has guard(s)";
                            if (action.index_target == index_target) {
                                attacking_guard = true;
                            }
                        }
                    }
                }
                assert !(found_guard && !attacking_guard) : "Attempt to attack an opponent's creature when he has guard(s)";

                card.canAttack = false;
                if (action.index_target == ConstantField.OPPONENT_FACE) {
                    // Attack the opponent face
                    if (card.attack > 0) {
                        opponent.hp -= card.attack;
                    }
                } else {
                    // Attack a creature
                    Card target = cards.get(action.index_target);

                    // Breakthrough
                    if (card.breakthrough && !target.ward) {
                        int reminder = card.attack - target.defense;
                        if (reminder > 0) opponent.hp -= reminder;
                    }

                    // Drain active
                    if (card.attack > 0 && card.drain) {
                        if (!target.ward) {
                            myPlayer.hp += card.attack;
                        }
                    }

                    // Drain passive
                    if (target.attack > 0 && target.drain) {
                        if (!card.ward) {
                            opponent.hp += target.attack;
                        }
                    }

                    // Damage active
                    if (card.attack > 0) {
                        if (target.ward) target.ward = false;
                        else {
                            if (card.lethal) target.defense -= 99999;
                            else target.defense -= card.attack;
                        }
                    }

                    // Damage passive
                    if (target.attack > 0) {
                        if (card.ward) card.ward = false;
                        else {
                            if (target.lethal) card.defense -= 99999;
                            else card.defense -= target.attack;
                        }
                    }

                    if (card.defense <= 0) {
                        card.location = CardLocation.OUT_OF_PLAY;
                        myCreatureIndexList.remove((Integer) card.index);
                    }
                    if (target.defense <= 0) {
                        target.location = CardLocation.OUT_OF_PLAY;
                        opponentCreatureList.remove((Integer) target.index);
                    }
                }

                break;

            case USE:
                assert card.cardType != CardType.CREATURE : "Attempt to use a creature card";
                assert card.cost <= myPlayer.mana : "Attempt to use an item without enough mana";

                myPlayer.mana -= card.cost;
                myPlayer.playerDraw += card.cardDraw;
                myPlayer.hp += card.hpChange;
                opponent.hp += card.hpChangeEnemy;

                card.location = CardLocation.OUT_OF_PLAY;

                if (action.index_target != ConstantField.OPPONENT_FACE) {
                    Card target = cards.get(action.index_target);
                    if (card.cardType == CardType.GREEN_ITEM) {
                        // Keyword effects
                        target.breakthrough = card.breakthrough;
                        target.ward = card.ward;
                        target.drain = card.drain;
                        target.guard = card.guard;
                        target.lethal = card.lethal;
                        target.charge = card.charge;
                        target.canAttack = card.charge;
                    } else if (card.cardType == CardType.RED_ITEM) {
                        // Keyword effects
                        if (card.breakthrough) target.breakthrough = false;
                        if (card.ward) target.ward = false;
                        if (card.drain) target.drain = false;
                        if (card.guard) target.guard = false;
                        if (card.lethal) target.lethal = false;
                        if (card.charge) target.charge = false;
                        target.canAttack = target.charge;
                    }

                    if (card.attack >= 0 && card.defense >= 0) {
                        // Positive effect
                        target.attack += card.attack;
                        target.defense += card.defense;
                    } else {
                        if (target.ward) target.ward = false;
                        else {
                            target.attack += card.attack;
                            target.defense += card.defense;
                        }
                    }

                    if (target.defense <= 0) {
                        target.location = CardLocation.OUT_OF_PLAY;
                        opponentCreatureList.remove((Integer) target.index);
                    }
                }
                break;


            case PASS:
                break;
        }
    }
}
