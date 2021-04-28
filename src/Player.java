public class Player {

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
