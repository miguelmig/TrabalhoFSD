package client.menus;

import client.Client;

public class LastPostsMenu extends Menu {

    private Client client;

    public LastPostsMenu(Client client) {
        this.client = client;
    }

    @Override
    public void display() {
        System.out.println("-------- LAST POSTS MENU --------");
    }

    @Override
    public void handleEvents() {

    }
}
