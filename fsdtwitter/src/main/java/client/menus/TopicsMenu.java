package client.menus;

import client.Client;

public class TopicsMenu extends Menu {

    private Client client;

    public TopicsMenu(Client client) {
        this.client = client;
    }

    @Override
    public void display() {
        System.out.println("-------- TOPICS MENU --------");

    }

    @Override
    public void handleEvents() {

    }
}
