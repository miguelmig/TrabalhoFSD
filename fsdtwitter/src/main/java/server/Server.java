package server;
import config.Config;
import config.JournalConfig;
import data.PostJournal;
import data.UserJournal;
import data.models.Post;
import data.models.User;
import enums.MessageCode;
import io.atomix.storage.journal.Journal;
import io.atomix.storage.journal.SegmentedJournal;
import io.atomix.utils.net.Address;
import net.*;
import spullara.nio.channels.FutureServerSocketChannel;
import spullara.nio.channels.FutureSocketChannel;
import utils.FutureLineBuffer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
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

    private static SegmentedJournal<StateMessage> log;
    private static boolean canCommit = true;

    private static int counter;

    private static String currentUser;

    // Client requests can only be handled after the state is loaded in this server
    private static boolean is_state_loaded = false;

    private static int leader_id = -1;

    public static void main(String[] args) throws Exception {

        if (args.length < 1) {
            port = 8000;
        } else {
            port = Integer.parseInt(args[0]);
        }

        boolean startLeaderElection = false;
        if(args.length > 1)
        {
            startLeaderElection = Integer.parseInt(args[1]) != 0;
        }



        //BuildProcessList();
        ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(1);
        mh = new MessageHandler(port, executor);
        mh.startMessageHandler();
        if(startLeaderElection)
            mh.startLeaderElectionProcess();
//

        executor.awaitTermination(60*60*24*7, TimeUnit.SECONDS);
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

        System.out.println("Recebi mensagem do cliente: " + msg);
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
            System.out.println("Register sucessful!");
            //TODO : enviar mensagem aos restantes servidores com o newUser
            onStateChange();
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
        onStateChange();
    }

    private static void handleSubscribe(FutureLineBuffer buf, String content) {

        String[] tokens = content.split(" ");

        List<String> tags = Arrays.asList(tokens);

        users.get(currentUser).addTags(tags);
        onStateChange();

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

    public static void onLeaderElected(int leader_id)
    {
        try
        {
            Server.leader_id = leader_id;
            ssc = new FutureServerSocketChannel();
            ssc.bind(new InetSocketAddress(port + Config.CLIENT_PORT_OFFSET));

            System.out.println("Server listening on port " + (port + Config.CLIENT_PORT_OFFSET));
            ssc.accept()
                    .thenAccept(Server::onAccept);
        }
        catch(IOException e)
        {
            System.err.println("Error creating the client socket!");
        }
    }

    public static void onStateReceived(StateMessage state)
    {
        // parse state message
        users = state.users.stream().collect(Collectors.toMap(User::getName, Function.identity()));
        posts = state.posts.stream().collect(Collectors.toMap(Post::getId, Function.identity()));

        counter = posts.size();
        is_state_loaded = true;
        System.out.println("State loaded, users: " + users.size() + " posts: " + counter);
    }

    public static void SendClientMessage(String msg) {

    }

    public static void loadState()
    {
        is_state_loaded = true;

        // Load the states from the journals
        PostJournal pj = new PostJournal(JournalConfig.getPostsLogName());
        UserJournal uj = new UserJournal(JournalConfig.getUsersLogName());

        users = uj.readAllJournal();
        posts = pj.readAllJournal();
        System.out.println("State loaded, users: " + users.size() + " posts: " + counter);

        // Broadcast state to other servers
        broadcastState();
    }

    public static void saveState()
    {
        // TODO: Save State to Journals if this is the leader process
        PostJournal pj = new PostJournal(JournalConfig.getPostsLogName());
        UserJournal uj = new UserJournal(JournalConfig.getUsersLogName());
        pj.writeJournal(posts);
        uj.writeJournal(users);

    }

    public static void broadcastState()
    {
        StateMessage state = new StateMessage();
        state.users = new ArrayList<>(users.values());
        state.posts = new ArrayList<>(posts.values());

        mh.broadcastState(state);
    }

    public static void onStateChange()
    {
        System.out.println("State changed, broadcasting!");
        broadcastState();
        if(leader_id == port - Config.ADDR_START)
        {
            System.out.println("We're the leader, updating journals!");
            saveState();
        }
    }

}

