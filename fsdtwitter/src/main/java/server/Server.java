package server;
import client.Client;
import config.Config;
import data.PostJournal;
import data.UserJournal;
import data.models.User;
import handlers.AcceptHandler;
import io.atomix.utils.net.Address;
import net.*;
import org.graalvm.compiler.lir.LIRInstruction;
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

    // Journals
    private static UserJournal users;
    private static PostJournal posts;

    
    private User currentUser;

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
        buf.readLine()
                .thenCompose(msg -> onRead(buf, msg));

        ssc.accept()
                .thenAccept(Server::onAccept);
    }

    private static CompletableFuture<String> onRead(FutureLineBuffer buf, String msg) {

        handleClientMessage(msg);
        //System.out.println("Recebi mensagem do cliente: " + msg);

        return buf.readLine()
                .thenCompose(newMsg -> onRead(buf, newMsg));
    }

    private static void handleClientMessage(String msg) {

        String[] tokens = msg.split(" ", 2);

        String messageType = tokens[0];
        String content = tokens[1];
        switch (messageType) {
            case "/register":
                handleRegister(content);
                break;

            case "/login":
                handleLogin(content);
                break;

            case "/post":
                handlePost(content);
                break;

            case "/get_topics":
                handleGetTopics(content);
                break;

            case "/get_last_posts":
                handleGetLastPosts(content);
                break;

            default:
                break;
        }
    }

    private static void handleRegister(String content) {
        /*
         * Verificar se o nome de utilizador já existe
         * Se não existe:
         *      - regista o utilizador no user journal
         *      - envia mensagem ao cliente a confirmar o registo
         *      - envia mensagem aos outros servidores com a informação deste
         *      utilizador para estes atualizarem os seus journals
         *
         * Se já existe:
         *      - envia mensagem ao cliente a dizer que o username já existe
         */
    }

    private static void handleLogin(String content) {
        /*
         * Verificar se o nome e a password do utilizador estão corretas
         */
    }

    private static void handlePost(String content) {
        /*
         * Registar o post no post journal
         * Enviar mensagem aos outros servidores para estes atualizarem os seus
         * journals com este post
         */
    }

    private static void handleGetTopics(String content) {
        /*
         * Ir ao journal buscar a lista de tópicos subscrita pelo o utilizador
         */
    }

    private static void handleGetLastPosts(String content) {
        /*
         * Ir ao post journal buscar os ultimos 10 posts com os tópicos subscritos
         * pelo o utilizador
         */
    }


    //private static void BuildProcessList()
    //{
    //    for(int port = Config.ADDR_START; port < Config.ADDR_START + Config.MAX_PROCESSES; ++port)
    //    {
    //        processes.add(new Process(port));
    //    }
    //}

    public static void BroadcastMessage(String msg) {
        bufs.forEach(buf -> buf.writeln(msg));
    }

    public static void DeliverMsg(String message_type, Address addr, Message data) {
        System.out.println("Delivering message!");
    }



}

