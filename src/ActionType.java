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
