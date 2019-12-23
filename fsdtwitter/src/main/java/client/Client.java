package client;

import client.menus.Menu;
import client.menus.StartMenu;
import spullara.nio.channels.FutureSocketChannel;
import utils.FutureLineBuffer;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

public class Client {

    private static FutureSocketChannel socketChannel;
    private static FutureLineBuffer buf;

    public enum MessageType {
        REGISTER,
        LOGIN,
        POST,
        SUBSCRIBE,
        GET_TOPICS,
        GET_LAST_POSTS
    }

    public static void main(String[] args) throws Exception {

        Client client = new Client();

        String hostname = "localhost";

        client.connectToServer(hostname)
                .thenRun(() -> {
                    System.out.println("Connected to server!");
                    buf = new FutureLineBuffer(socketChannel);

                    //client.handleMessages();

                    client.startMenu();
                    //Menu menu = new StartMenu(client);
                    //menu.run();
                });

        while (true) {
            Thread.sleep(1000);
        }
        //Menu menu = new StartMenu(client);
        //menu.run();
    }

    private void startMenu() {
        Menu menu = new StartMenu(this);
        menu.run();
    }


    private void handleMessages() {
        buf.readLine()
                .thenCompose(msg -> onRead(buf, msg));
    }



    private CompletionStage<String> onRead(FutureLineBuffer buf, String msg) {
        System.out.println("Recebi mensagem do servidor: " + msg);

        return buf.readLine()
                .thenCompose(newMsg -> onRead(buf, newMsg));
    }



    private CompletableFuture<Void> connectToServer(String hostname) throws Exception {

        socketChannel = new FutureSocketChannel();
        SocketAddress serverAddress = new InetSocketAddress(hostname, 8000);

        return socketChannel.connect(serverAddress);
    }


    public String readMessage() {
        String msg = null;
        try {

            msg = buf.readLine().get();

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return msg;
    }

    public void sendMessage(MessageType type, String content) {

        StringBuilder msg = new StringBuilder();

        switch (type) {
            case REGISTER:
                msg.append("/register ");
                break;

            case LOGIN:
                msg.append("/login ");
                break;

            case POST:
                msg.append("/post ");
                break;

            case SUBSCRIBE:
                msg.append("/subscribe ");
                break;

            case GET_TOPICS:
                msg.append("/get_topics ");
                break;

            case GET_LAST_POSTS:
                msg.append("/get_last_posts ");
                break;

            default:
                break;
        }

        msg.append(content);

        buf.writeln(msg.toString());
    }

}