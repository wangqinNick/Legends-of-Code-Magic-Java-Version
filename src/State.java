import java.util.ArrayList;

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