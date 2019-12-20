package client;


import client.menus.Menu;
import client.menus.StartMenu;
import handlers.WriteHandler;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.Future;

public class Client {

    private AsynchronousSocketChannel socketChannel;
    private ByteBuffer buf;

    public static void main(String[] args) throws Exception {

        Client client = new Client();

        String hostname = "localhost";

        client.connectToServer(hostname);

        Menu menu = new StartMenu(client);
        menu.run();
    }

    private void connectToServer(String hostname) throws Exception {

        this.socketChannel = AsynchronousSocketChannel.open();
        SocketAddress serverAddress = new InetSocketAddress(hostname, 8000);

        Future<Void> result = this.socketChannel.connect(serverAddress);
        result.get();
        System.out.println("Connected to server!");
    }

    private void send(String message) {

        byte[] data = message.getBytes();
        ByteBuffer temporaryBuf = ByteBuffer.allocate(data.length);
        temporaryBuf.put(data).flip();

        System.out.println("Client is writing...");
        this.socketChannel.write(temporaryBuf, null,
                new WriteHandler(this.socketChannel, this.buf));
    }

}