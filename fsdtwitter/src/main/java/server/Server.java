package server;
import client.Client;
import config.Config;
import handlers.AcceptHandler;
import io.atomix.utils.net.Address;
import net.*;
import spullara.nio.channels.FutureServerSocketChannel;
import spullara.nio.channels.FutureSocketChannel;
import utils.FutureLineBuffer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Server {
    //private static List<Process> processes = new ArrayList<>();
    //private static List<Client> clients = new ArrayList<>();

    private static MessageHandler mh;
    private static int port;
    private static FutureServerSocketChannel ssc;
    private static List<FutureLineBuffer> bufs = new ArrayList<>();

    public static void main(String[] args) throws Exception {

        if (args.length < 1) {
            port = 8000;
        } else {
            port = Integer.parseInt(args[0]);
        }

        ssc = new FutureServerSocketChannel();
        ssc.bind(new InetSocketAddress(port));

        System.out.println("Server listening on port " + port);
        ssc.accept()
                .thenAccept(Server::onAccept);

        while (true) {
            Thread.sleep(1000);
        }
        //BuildProcessList();
//
        ////mh = new MessageHandler(port);
//
        ////mh.startMessageHandler();
    }

    private static void onAccept(FutureSocketChannel sc) {
        System.out.println("Client Connected!");

        FutureLineBuffer buf = new FutureLineBuffer(sc);
        bufs.add(buf);
        buf.writeln("Eu sou o servidor");
        buf.readLine()
                .thenCompose(msg -> onRead(buf, msg));

        ssc.accept()
                .thenAccept(Server::onAccept);
    }

    private static CompletableFuture<String> onRead(FutureLineBuffer buf, String msg) {

    buf.write(msg + '\n');
        // parse reading
        System.out.println("Recebi mensagem");

        return buf.readLine()
                .thenCompose(newMsg -> onRead(buf, newMsg));
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

