import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.cluster.MembershipEvent;
import com.hazelcast.cluster.MembershipListener;
import com.hazelcast.core.HazelcastInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.TimeUnit;

public class Main implements MembershipListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static void main(String[] args) throws Exception {

        Main main = new Main();
        main.initialize();

    }

    public void initialize() throws Exception {
        LOGGER.info("Initializing");

        ClientConfig config = ClientConfig.load();

        HazelcastInstance instance = HazelcastClient.newHazelcastClient(config);
        instance.getCluster().addMembershipListener(this);

        Thread.sleep((TimeUnit.SECONDS.toMillis(20)));

    }

    @Override
    public void memberAdded(MembershipEvent membershipEvent) {
        LOGGER.info("Member added <{}>", membershipEvent);
    }

    @Override
    public void memberRemoved(MembershipEvent membershipEvent) {
        LOGGER.info("Member removed <{}>", membershipEvent);
    }
}
