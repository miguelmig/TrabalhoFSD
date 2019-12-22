package client.menus;

import client.Client;

public class PostMenu extends Menu {

    private Client client;

    public PostMenu(Client client) {
        this.client = client;
    }

    @Override
    public void display() {
        System.out.println("-------- POST MENU --------");
    }

    @Override
    public void handleEvents() {

    }
}
