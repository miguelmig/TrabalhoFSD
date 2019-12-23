package client.menus;

import client.Client;

import java.util.ArrayList;
import java.util.List;

public class LastPostsMenu extends Menu {

    private Client client;

    public LastPostsMenu(Client client) {
        this.client = client;
    }

    @Override
    public void display() {
        System.out.println("-------- LAST POSTS --------");
    }

    @Override
    public void handleEvents() {
        client.sendMessage(Client.MessageType.GET_LAST_POSTS, null);




    }
}
