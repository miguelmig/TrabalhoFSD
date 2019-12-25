package net;

import config.Config;
import io.atomix.cluster.messaging.ManagedMessagingService;
import io.atomix.cluster.messaging.MessagingConfig;
import io.atomix.cluster.messaging.impl.NettyMessagingService;
import io.atomix.utils.net.Address;
import io.atomix.utils.serializer.Serializer;
import io.atomix.utils.serializer.SerializerBuilder;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import data.models.*;
import org.apache.commons.math3.analysis.function.Add;
import server.Server;

import javax.swing.plaf.nimbus.State;

public class MessageHandler
{
    class MessageWrapped
    {
        public String message_type;
        public Address from;
        public Message msg;
    }
    private ManagedMessagingService ms;
    private int port;

    private int[] delivered = new int[Config.MAX_PROCESSES];
    private List<MessageWrapped> deliveryQueue = new ArrayList<>();
    private List<Process> servers = new ArrayList<>();
    private List<Process> clients = new ArrayList<>();
    private ScheduledExecutorService executor;
    private Serializer s;

    private int leaderID = -1;
    private boolean leader_election_started = false;
    private Map<Address, LeaderElectionMessage> leaders = new HashMap<>();

    public MessageHandler(int port, ScheduledExecutorService e)
    {
        executor = e;
        this.port = port;
        ms = new NettyMessagingService("twitter", Address.from(port), new MessagingConfig());

        s = new SerializerBuilder()
                .addType(Message.class)
                .addType(Post.class)
                .addType(LocalDate.class)
                .addType(User.class)
                .addType(LeaderElectionMessage.class)
                .addType(StateMessage.class)
                .build();

        registerMessage("state");

        // Internal operations related to Leader Election
        registerMessage("start");
        registerMessage("leader");

        /*
        ms.registerHandler("publish", (addr, data) -> {
            if(isServer(addr))
            {
                System.err.println("Server isn't supposed to send publish!");
                return;
            }
            Message msg = s.decode(data);
            Post new_post = s.decode(msg.getContent());

        }, executor);
        */

    }

    public void startLeaderElectionProcess()
    {
        System.out.println("Sending out start leader election message!");
        leader_election_started = true;
        leaders.clear();
        sendStartLeaderMessage();
        broadcastMessage("leader", buildElectionMessage());
        executor.schedule(this::electLeader, Config.LEADER_CHOOSE_TIME, TimeUnit.SECONDS);
    }

    private void electLeader()
    {
        int ranking = -1;
        int process_id = -1;
        for (Map.Entry<Address, LeaderElectionMessage> entry : leaders.entrySet())
        {
            if(entry.getValue().ranking > ranking) {
                ranking = entry.getValue().ranking;
                process_id = getProcessId(entry.getKey());
            }
        };

        if(process_id == -1 || ranking == -1)
        {
            System.err.println("Unable to choose a leader! No one candidated!");
            return;
        }

        System.out.println("Leader elected, server id: " + process_id);
        leader_election_started = false;
        leaderID = process_id;
        Server.onLeaderElected(leaderID);
        if(leaderID == getProcessId(Address.from(port)))
        {
            // We're the leader, we're responsible for loading the state from the journals
            // And broadcasting it to other servers
            Server.loadState();
        }
    }

    private void sendStartLeaderMessage()
    {
        broadcastMessage("start", s.encode(null));
    }

    public void startMessageHandler()
    {
        this.ms.start();
    }

    public void registerMessage(String message_type)
    {
        ms.registerHandler(message_type, (addr, data) -> {
            Message msg = s.decode(data);
            if(isServer(addr)) {
                int process_id = addr.port() - Config.ADDR_START;
                int[] TS = msg.getVectorClock();

                // Fifo Delivery
                if(addr.port() != port)
                {
                    System.out.println("TS on receive message: " + message_type);
                    for(int i = 0; i < Config.MAX_PROCESSES; i++)
                    {
                        System.out.println("TS[" + i + "] = " + TS[i]);
                    }


                    if (TS[process_id] - 1 != delivered[process_id]) {
                        // There's still messages to be received from process_id, let's queue this one.
                        MessageWrapped msg_wrapped = new MessageWrapped();
                        msg_wrapped.message_type = message_type;
                        msg_wrapped.from = addr;
                        msg_wrapped.msg = msg;
                        deliveryQueue.add(msg_wrapped);
                        System.out.println("Still messages to be received, TS: " + (TS[process_id] - 1) + " D:" + (delivered[process_id] + 1) + " from process: " + process_id);
                        return;
                    }

                    // Causal Delivery part2
                    for (int k = 0; k < Config.MAX_PROCESSES; k++) {
                        if (k != process_id) {
                            if (delivered[k] < TS[k]) {
                                MessageWrapped msg_wrapped = new MessageWrapped();
                                msg_wrapped.message_type = message_type;
                                msg_wrapped.from = addr;
                                msg_wrapped.msg = msg;
                                deliveryQueue.add(msg_wrapped);
                                return;
                            }
                        }
                    }
                }

                onDeliverMessage(message_type, addr, msg);

                tryDeliverMessages(process_id);



            }
        }, executor);
    }

    void tryDeliverMessages(int process_id)
    {
        Iterator it = deliveryQueue.iterator();
        while(it.hasNext())
        {
            MessageWrapped m = (MessageWrapped)it.next();
            int[] TS = m.msg.getVectorClock();
            if (TS[process_id] - 1 != delivered[process_id]) {
                continue;
            }

            // Causal Delivery part2
            for (int k = 0; k < Config.MAX_PROCESSES; k++) {
                if (k != process_id) {
                    if (delivered[k] < TS[k]) {
                        continue;
                    }
                }
            }
            System.out.println("Delivering old message from: " + m.from + " TS[" + process_id + "]= " + TS[process_id]);
            onDeliverMessage(m.message_type, m.from, m.msg);
            deliveryQueue.remove(m);
            tryDeliverMessages(process_id);
        }
    }

    public boolean isServer(Address addr)
    {
        if(addr.port() >= Config.ADDR_START && addr.port() < Config.ADDR_START + Config.MAX_PROCESSES)
        {
            return true;
        }
        return false;
    }

    public int getProcessId(Address addr)
    {
        if(!isServer(addr))
            return -1;
        return addr.port() - Config.ADDR_START;
    }

    public void onDeliverMessage(String message_type, Address addr, Message msg)
    {
        int process_id = getProcessId(addr);
        delivered[process_id] = msg.getVectorClock()[process_id];
        System.out.println("On Deliver: Set D[" + process_id + "] = " + delivered[process_id]);
        // Handling internal messages related to leader election
        if(message_type.equals("start"))
        {
            System.out.println("Received start message for leader election");
            if(!leader_election_started)
            {
                leader_election_started = true;
                leaders.clear();
                executor.schedule(this::electLeader, Config.LEADER_CHOOSE_TIME, TimeUnit.SECONDS);
            }

            if(addr.port() != port)
                broadcastMessage("leader", buildElectionMessage());

        }
        else if(message_type.equals("leader"))
        {
            LeaderElectionMessage electionMsg = s.decode(msg.getContent());
            leaders.put(addr, electionMsg);
        }
        else if(message_type.equals("state"))
        {
            if(addr.port() != port) {
                StateMessage stateMsg = s.decode(msg.getContent());
                Server.onStateReceived(stateMsg);
            }
        }
        else
        {
            if(leaderID == -1)
            {
                // no leader, rejecting message.
                return;
            }
            Server.DeliverMsg(message_type, addr, msg);
        }
    }

    public byte[] buildElectionMessage()
    {
        LeaderElectionMessage payload = new LeaderElectionMessage();
        payload.ranking = (int)ProcessHandle.current().pid();
        return s.encode(payload);
    }

    public void broadcastMessage(String message_type, byte[] msg)
    {
        Message wrapper = new Message();
        int my_process_id = port - Config.ADDR_START;
        delivered[my_process_id] = delivered[my_process_id] + 1;
        System.out.println("On Broadcast: " + message_type +" set D[" + my_process_id + "] = " + delivered[my_process_id]);
        wrapper.setVectorClock(delivered);
        wrapper.setContent(msg);

        for(int k = 0; k < Config.MAX_PROCESSES; k++)
        {
            ms.sendAsync(Address.from(Config.ADDR_START + k),
                    message_type,
                    s.encode(wrapper));
        }
    }

    public void broadcastState(StateMessage state)
    {
        System.out.println("Broadcasting state!");
        broadcastMessage("state", s.encode(state));
    }

}
