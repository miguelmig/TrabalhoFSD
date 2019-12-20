package server;
import client.Client;
import config.Config;
import handlers.AcceptHandler;
import io.atomix.utils.net.Address;
import net.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Server {
    //private static List<Process> processes = new ArrayList<>();
    private static List<Client> clients = new ArrayList<>();

    private static MessageHandler mh;
    private static int port;

    public static void main(String[] args) throws Exception {

        if (args.length < 1) {
            port = 8000;
        } else {
            port = Integer.parseInt(args[0]);
        }

        //BuildProcessList();
        AsynchronousChannelGroup g =
                AsynchronousChannelGroup.withFixedThreadPool(1,
                        Executors.defaultThreadFactory());

        AsynchronousServerSocketChannel ssc = AsynchronousServerSocketChannel.open(g);
        ssc.bind(new InetSocketAddress(port));

        ssc.accept(null, new AcceptHandler(ssc));
        System.out.println("Server listening on port: " + port);

        //mh = new MessageHandler(port);

        //mh.startMessageHandler();

        g.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
    }

    //private static void BuildProcessList()
    //{
    //    for(int port = Config.ADDR_START; port < Config.ADDR_START + Config.MAX_PROCESSES; ++port)
    //    {
    //        processes.add(new Process(port));
    //    }
    //}

    public static void BroadcastMessage(Message msg) {

    }

    public static void DeliverMsg(String message_type, Address addr, Message data) {
        System.out.println("Delivering message!");
    }

}

