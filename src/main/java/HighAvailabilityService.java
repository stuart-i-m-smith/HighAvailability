import com.hazelcast.cluster.Member;
import com.hazelcast.cluster.MembershipEvent;
import com.hazelcast.cluster.MembershipListener;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.*;

public class HighAvailabilityService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final List<AvailabilityListener> availabilityListeners = new ArrayList<>();

    private HazelcastInstance hazelcastInstance;
    private String thisMemberId;
    private IMap<Long, String> memberMap;

    public void initialize(){

        if(hazelcastInstance != null){
            LOGGER.warn("Already initialized.");
            return;
        }

        this.hazelcastInstance = Hazelcast.newHazelcastInstance();
        this.thisMemberId = hazelcastInstance.getCluster().getLocalMember().getUuid().toString();
        this.memberMap = hazelcastInstance.getMap("members");

        Member localMember = hazelcastInstance.getCluster().getLocalMember();
        memberMap.put(System.nanoTime(), localMember.getUuid().toString());
        memberMap.flush();

        hazelcastInstance.getCluster().addMembershipListener(new MembershipListener() {
            @Override
            public void memberAdded(MembershipEvent membershipEvent) {
                LOGGER.info("Member added <{}>", membershipEvent);
            }

            @Override
            public void memberRemoved(MembershipEvent membershipEvent) {
                LOGGER.info("Member removed <{}>", membershipEvent);

                removeMember(membershipEvent);
                checkIfPrimary();
            }
        });

        checkIfPrimary();
    }

    public void addAvailabilityListener(AvailabilityListener availabilityListener){
        if(isPrimaryInstance()){
            availabilityListener.onMakeAvailable();
        }

        this.availabilityListeners.add(availabilityListener);
    }

    private void checkIfPrimary(){
        if(isPrimaryInstance()){
            LOGGER.info("This instance is the primary process.");
            notifyAllListeners();
        }else{
            LOGGER.info("Other instance(s) detected, waiting for failover.");
        }
    }

    private void removeMember(MembershipEvent membershipEvent){
        Optional<Map.Entry<Long, String>> oldMember = memberMap.entrySet().stream()
                .filter(e -> e.getValue().equals(membershipEvent.getMember().getUuid().toString())).findAny();

        oldMember.ifPresent(e -> memberMap.remove(e.getKey()));

        memberMap.flush();
    }

    private boolean isPrimaryInstance(){
        if(hazelcastInstance == null){
            return false;
        }

        Set<Member> members = hazelcastInstance.getCluster().getMembers();

        if(members.size() <= 1){
            return true;
        }

        long oldestInstance = memberMap.keySet().stream()
                .mapToLong(v -> v)
                .min().orElse(0);

        return thisMemberId.equals(memberMap.get(oldestInstance));
    }

    private void notifyAllListeners(){
        availabilityListeners.forEach(AvailabilityListener::onMakeAvailable);
    }
}