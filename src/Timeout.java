

public class Timeout {
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
