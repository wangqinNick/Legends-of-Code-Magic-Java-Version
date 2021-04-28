import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

import static java.lang.Float.NEGATIVE_INFINITY;
import static java.lang.Float.POSITIVE_INFINITY;

public class Agent {
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
