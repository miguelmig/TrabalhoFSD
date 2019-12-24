package net;

import config.Config;
import io.atomix.cluster.messaging.ManagedMessagingService;
import io.atomix.cluster.messaging.MessagingConfig;
import io.atomix.cluster.messaging.impl.NettyMessagingService;
import io.atomix.utils.net.Address;
import io.atomix.utils.serializer.Serializer;
import io.atomix.utils.serializer.SerializerBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import data.models.*;
import server.Server;

public class MessageHandler
{
    private ManagedMessagingService ms;
    private int port;

    private int[] delivered = new int[Config.MAX_PROCESSES];
    private List<Message> deliveryQueue = new ArrayList<>();
    private List<Process> servers = new ArrayList<>();
    private List<Process> clients = new ArrayList<>();
    private ScheduledExecutorService executor;
    private Serializer s;

    private int leaderID = -1;
    private boolean leader_election_started = false;
    private Map<Address, LeaderElectionMessage> leaders = new HashMap<>();

    public MessageHandler(int port)
    {
        executor = new ScheduledThreadPoolExecutor(1);
        this.port = port;
        ms = new NettyMessagingService("twitter", Address.from(port), new MessagingConfig());

        s = new SerializerBuilder()
                .addType(Message.class)
                .addType(Post.class)
                .addType(LeaderElectionMessage.class)
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
        leader_election_started = true;
        leaders.clear();
        sendStartLeaderMessage();
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

        leader_election_started = false;
        leaderID = process_id;
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
                if(TS[process_id] - 1 != delivered[process_id] + 1)
                {
                    // There's still messages to be received from process_id, let's queue this one.
                    deliveryQueue.add(msg);
                    return;
                }

                // Causal Delivery part2
                for(int k = 0; k < Config.MAX_PROCESSES; k++)
                {
                    if(k != process_id)
                    {
                        if(delivered[k] < TS[k])
                        {
                            deliveryQueue.add(msg);
                            return;
                        }
                    }
                }

                onDeliverMessage(message_type, addr, msg);

                // TODO: See if any messages on the delivery queue can be delivered now

            }
        }, executor);
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

        // Handling internal messages related to leader election
        if(message_type.equals("start"))
        {

        }
        else if(message_type.equals("leader"))
        {

        }
        else
        {
            Server.DeliverMsg(message_type, addr, msg);
        }
    }

    public void broadcastMessage(String message_type, byte[] msg)
    {
        Message wrapper = new Message();
        int my_process_id = port - Config.ADDR_START;
        delivered[my_process_id] = delivered[my_process_id] + 1;
        wrapper.setVectorClock(delivered);
        wrapper.setContent(msg);


        for(int k = 0; k < Config.MAX_PROCESSES; k++)
        {
            ms.sendAsync(Address.from(Config.ADDR_START + k),
                    message_type,
                    s.encode(wrapper));
        }
    }

}
