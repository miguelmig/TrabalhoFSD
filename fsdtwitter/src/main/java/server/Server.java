package server;
import data.models.Post;
import data.models.User;
import enums.MessageCode;
import io.atomix.utils.net.Address;
import net.*;
import spullara.nio.channels.FutureServerSocketChannel;
import spullara.nio.channels.FutureSocketChannel;
import utils.FutureLineBuffer;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


public class Server {
    //private static List<Process> processes = new ArrayList<>();
    //private static List<Client> clients = new ArrayList<>();

    private static MessageHandler mh;
    private static int port;
    private static FutureServerSocketChannel ssc;
    private static List<FutureLineBuffer> bufs = new ArrayList<>();

    private static Map<String, User> users = new HashMap<>();
    private static Map<Integer, Post> posts = new HashMap<>();

    private static int counter;

    private static String currentUser;

    public static void main(String[] args) throws Exception {

        if (args.length < 1) {
            port = 8000;
        } else {
            port = Integer.parseInt(args[0]);
        }

        counter = posts.size();

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

        //System.out.println("Recebi mensagem do cliente: " + msg);
        handleClientMessage(buf, msg);


        return buf.readLine()
                .thenCompose(newMsg -> onRead(buf, newMsg));
    }

    private static void handleClientMessage(FutureLineBuffer buf, String msg) {

        String[] tokens = msg.split(" ", 2);

        String messageType = tokens[0];
        String content = tokens[1];
        switch (messageType) {
            case "/register":
                handleRegister(buf, content);
                break;

            case "/login":
                handleLogin(buf, content);
                break;

            case "/post":
                handlePost(buf, content);
                break;

            case "/subscribe":
                handleSubscribe(buf, content);
                break;

            case "/get_topics":
                handleGetTopics(buf, content);
                break;

            case "/get_last_posts":
                handleGetLastPosts(buf, content);
                break;

            default:
                break;
        }
    }

    private static void handleRegister(FutureLineBuffer buf, String content) {

        String[] tokens = content.split(" ");

        String username = tokens[0];
        String password = tokens[1];
        if (users.containsKey(username)) {
            buf.writeln(MessageCode.ERROR_USER_ALREADY_EXISTS.name());
        } else {
            buf.writeln(MessageCode.OK_SUCCESSFUL_REGISTER.name());

            User newUser = new User(username, password, new ArrayList<>());
            users.put(username, newUser);

            //TODO : enviar mensagem aos restantes servidores com o newUser
        }
    }


    private static void handleLogin(FutureLineBuffer buf, String content) {

        String[] tokens = content.split(" ");

        String username = tokens[0];
        String password = tokens[1];

        if (users.containsKey(username)) {

            User u = users.get(username);
            if (u.getPassword().equals(password)) {
                buf.writeln(MessageCode.OK_SUCCESSFUL_LOGIN.name());
                currentUser = username;
            } else {
                buf.writeln(MessageCode.ERROR_WRONG_PASSWORD.name());
            }

        } else {
            buf.writeln(MessageCode.ERROR_USER_DOESNT_EXIST.name());
        }
    }

    private static void handlePost(FutureLineBuffer buf, String content) {

        String[] tokens = content.split("::");

        String text = tokens[0];
        List<String> tags = Arrays.asList(tokens[1].split(" "));

        Post newPost = new Post(counter, text, tags, currentUser);
        posts.put(counter, newPost);
        counter++;

        buf.writeln(MessageCode.OK_SUCCESSFUL_POST.name());

        // TODO: Enviar mensagem aos restantes servidores com o newPost
    }

    private static void handleSubscribe(FutureLineBuffer buf, String content) {

        String[] tokens = content.split(" ");

        List<String> tags = Arrays.asList(tokens);

        users.get(currentUser).addTags(tags);

        buf.writeln(MessageCode.OK_SUCCESSFUL_SUBSCRIBE.name());
    }

    private static void handleGetTopics(FutureLineBuffer buf, String content) {

        String msg = String.join("::", users.get(currentUser).getTags());

        buf.writeln(msg);
    }

    private static void handleGetLastPosts(FutureLineBuffer buf, String content) {

        List<String> subscribedTags = users.get(currentUser).getTags();

        String msg = posts.values().stream()
                .filter(post -> post.getTags().stream()
                        .anyMatch(subscribedTags::contains)
                )
                .sorted(Comparator.comparing(Post::getDate).reversed())
                .limit(10)
                .map(Post::toString)
                .collect(Collectors.joining("::"));

        buf.writeln(msg);
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

    public static void SendClientMessage(String msg) {

    }

}

