package net;

import io.atomix.cluster.messaging.ManagedMessagingService;
import io.atomix.cluster.messaging.MessagingConfig;
import io.atomix.cluster.messaging.impl.NettyMessagingService;
import io.atomix.utils.net.Address;
import io.atomix.utils.serializer.Serializer;
import io.atomix.utils.serializer.SerializerBuilder;

import java.util.ArrayList;
import java.util.List;

public class MessageHandler
{
    private ManagedMessagingService ms;
    private int port;
    private int[] delivered = new int[Config.MAX_PROCESSES];
    private List<Message> deliveryQueue = new ArrayList<>();

    public MessageHandler(int port)
    {
        this.port = port;
        ms = new NettyMessagingService("twitter", Address.from(port), new MessagingConfig());

        Serializer s = new SerializerBuilder()
                .addType(Message.class)
                .build();

        ms.registerHandler("state", (addr, data) -> {

        });
    }

    public void startMessageHandler()
    {
        this.ms.start();

    }
}
