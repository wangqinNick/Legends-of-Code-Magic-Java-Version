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
