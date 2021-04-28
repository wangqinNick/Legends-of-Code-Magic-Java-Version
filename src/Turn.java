import java.util.ArrayList;

public class Turn {
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
