package net;

import io.atomix.cluster.messaging.ManagedMessagingService;
import io.atomix.cluster.messaging.MessagingConfig;
import io.atomix.cluster.messaging.impl.NettyMessagingService;
import io.atomix.utils.net.Address;
import io.atomix.utils.serializer.Serializer;
import io.atomix.utils.serializer.SerializerBuilder;

public class MessageHandler
{
    private ManagedMessagingService ms;
    private int port;
    public MessageHandler(int port)
    {
        this.port = port;
        ms = new NettyMessagingService("twitter", Address.from(port), new MessagingConfig());

        Serializer s = new SerializerBuilder()
                .addType(Message.class)
                .build();


    }

    public void StartMessageHandler()
    {
        this.ms.start();

    }
}
