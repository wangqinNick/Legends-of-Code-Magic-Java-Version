public class Action {
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
