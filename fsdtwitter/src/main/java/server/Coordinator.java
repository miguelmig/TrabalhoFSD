package server;

import io.atomix.cluster.messaging.ManagedMessagingService;
import io.atomix.cluster.messaging.MessagingConfig;
import io.atomix.cluster.messaging.impl.NettyMessagingService;
import io.atomix.utils.net.Address;
import io.atomix.utils.serializer.Serializer;
import net.MessageHandler;
import net.StateMessage;

import java.util.List;

public class Coordinator {

    private static ManagedMessagingService ms;
    private static final int port = 12345;

    private static Serializer s;
    private static List<StateMessage> logData;

    public static void main(String[] args) {

        ms = new NettyMessagingService(
                "2pcommit",
                Address.from(port),
                new MessagingConfig()
        );

        ms.start();



    }
}
