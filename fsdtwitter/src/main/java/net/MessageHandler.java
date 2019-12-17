package net;

import io.atomix.cluster.messaging.ManagedMessagingService;
import io.atomix.cluster.messaging.MessagingConfig;
import io.atomix.cluster.messaging.impl.NettyMessagingService;
import io.atomix.utils.net.Address;
import io.atomix.utils.serializer.Serializer;
import io.atomix.utils.serializer.SerializerBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
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

    public MessageHandler(int port)
    {
        executor = new ScheduledThreadPoolExecutor(1);
        this.port = port;
        ms = new NettyMessagingService("twitter", Address.from(port), new MessagingConfig());

        s = new SerializerBuilder()
                .addType(Message.class)
                .addType(Post.class)
                .build();

        ms.registerHandler("state", (addr, data) -> {
            // Share state
        }, executor);

        ms.registerHandler("publish", (addr, data) -> {
            if(isServer(addr))
            {
                System.err.println("Server isn't supposed to send publish!");
                return;
            }
            Message msg = s.decode(data);
            Post new_post = s.decode(msg.getContent());

        }, executor);
    }

    public void startMessageHandler()
    {
        this.ms.start();
    }

    public void registerHandler(String message_type, BiConsumer<Address, byte[]> consumer)
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

            }
            consumer.accept(addr, data);
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

    public void onDeliverMessage(String message_type, Address addr, Message msg)
    {
        int process_id = addr.port() - Config.ADDR_START;
        delivered[process_id] = msg.getVectorClock()[process_id];

        Server.DeliverMsg(message_type, addr, msg);
    }

}
