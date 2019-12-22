package client;

import client.menus.Menu;
import client.menus.StartMenu;
import handlers.WriteHandler;
import spullara.nio.channels.FutureSocketChannel;
import utils.FutureLineBuffer;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Future;

public class Client {

    private static FutureSocketChannel socketChannel;
    private static FutureLineBuffer buf;


    public static void main(String[] args) throws Exception {

        Client client = new Client();

        String hostname = "localhost";

        client.connectToServer(hostname);

        buf = new FutureLineBuffer(socketChannel);
        buf.write("Eu sou o cliente");

        client.handleMessages();

        while (true) {
            Thread.sleep(1000);
        }
        //Menu menu = new StartMenu(client);
        //menu.run();
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



    private void connectToServer(String hostname) throws Exception {

        socketChannel = new FutureSocketChannel();
        SocketAddress serverAddress = new InetSocketAddress(hostname, 8000);

        socketChannel.connect(serverAddress)
                .thenRun(() -> System.out.println("Connected to server!"));
    }

    private void send(String msg) {

        buf.write(msg);
        System.out.println("Escrevi");
    }
}