import com.hazelcast.cluster.MembershipEvent;
import com.hazelcast.cluster.MembershipListener;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Main implements MembershipListener, AvailabilityListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private HazelcastInstance instance = null;
    private final List<AvailabilityListener> availabilityListeners = new ArrayList<>();
    
    public static void main(String[] args) throws Exception {
        Main main = new Main();
        main.initialize();
    }

    public void initialize() throws Exception {
        LOGGER.info("Initializing");

        instance = Hazelcast.newHazelcastInstance();
        instance.getCluster().addMembershipListener(this);
        addAvailabilityListener(this);

        if(isPrimaryInstance()){
            LOGGER.info("This is the primary instance.");
            notifyAllListeners();
        }

        Thread.sleep((TimeUnit.SECONDS.toMillis(30)));
    }

    private boolean isPrimaryInstance() {
        return instance.getCluster().getMembers().size() <= 1;
    }

    private void notifyAllListeners() {
        availabilityListeners.forEach(AvailabilityListener::onMakeAvailable);
    }

    public void addAvailabilityListener(AvailabilityListener listener){
        availabilityListeners.add(listener);
    }

    @Override
    public void memberAdded(MembershipEvent membershipEvent) {
        LOGGER.info("Member added <{}>", membershipEvent);
    }

    @Override
    public void memberRemoved(MembershipEvent membershipEvent) {
        LOGGER.info("Member removed <{}>", membershipEvent);

        if(isPrimaryInstance()){
            LOGGER.info("Failover event detected <{}>", membershipEvent);
            notifyAllListeners();
        }
    }

    @Override
    public void onMakeAvailable() {
        LOGGER.info("Oh I better start doing stuff now!");
    }
}
