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
