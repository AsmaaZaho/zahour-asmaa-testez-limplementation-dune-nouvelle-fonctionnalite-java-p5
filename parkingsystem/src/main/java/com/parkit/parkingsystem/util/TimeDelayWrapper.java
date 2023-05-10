import java.util.concurrent.TimeUnit;

// Define a wrapper interface for time delays
public interface TimeDelayWrapper {
    void sleep(long duration) throws InterruptedException;
}

