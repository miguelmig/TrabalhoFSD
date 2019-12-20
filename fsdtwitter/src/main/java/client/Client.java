package client;


import client.menus.Menu;
import client.menus.StartMenu;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.Future;

public class Client {

    public static void main(String[] args) throws Exception {

        Client client = new Client();

        String hostname = "localhost";

        client.connectToServer(hostname);

        Menu menu = new StartMenu();
        menu.run();
    }

    private void connectToServer(String hostname) throws Exception {

        AsynchronousSocketChannel socketChannel = AsynchronousSocketChannel.open();
        SocketAddress serverAddress = new InetSocketAddress(hostname, 8000);

        Future<Void> result = socketChannel.connect(serverAddress);
        result.get();
        System.out.println("Connected to server!");
    }



}