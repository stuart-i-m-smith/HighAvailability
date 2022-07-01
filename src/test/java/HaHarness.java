import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.net.InetAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public class HaHarness {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Test
    public void harness(){

        HighAvailabilityService haService = new HighAvailabilityService();
        haService.initialize();

        haService.addAvailabilityListener(() -> Executors.newFixedThreadPool(1).execute(() -> {
            LOGGER.warn("<{}> Start doing stuff!", getMachine());
            while(true){
                LOGGER.warn("<{}> I'm doing stuff.", getMachine());
                LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(1));
            }
        }));

        LockSupport.parkNanos(TimeUnit.MINUTES.toNanos(2));
    }

    private String getMachine() {
        String hostName = "Unknown";

        try{
            hostName = InetAddress.getLocalHost().getHostName();
        }catch (Exception e){
            LOGGER.error("Cannot resolve hostname.", e);
        }

        return hostName;
    }
}
