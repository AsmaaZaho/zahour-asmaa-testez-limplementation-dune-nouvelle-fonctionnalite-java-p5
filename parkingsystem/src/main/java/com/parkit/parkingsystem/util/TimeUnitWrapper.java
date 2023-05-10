import java.util.concurrent.TimeUnit;

public class TimeUnitWrapper implements TimeDelayWrapper {
    @Override
    public void sleep(long duration) throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(duration);
    }
}
