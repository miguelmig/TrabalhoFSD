package client.menus;

import client.Client;

import java.util.Scanner;

public class TopicsMenu extends Menu {

    private Client client;

    public TopicsMenu(Client client) {
        this.client = client;
    }

    @Override
    public void display() {
        System.out.println("-------- TÓPICOS SUBSCRITO --------");
    }

    @Override
    public void handleEvents() {

        Scanner s = new Scanner(System.in);

        client.sendMessage(Client.MessageType.GET_TOPICS, null);

        String msg = client.readMessage();

        String[] tags = msg.split("::");
        for (String tag : tags) {
            System.out.println(tag);
        }

        System.out.println("\n1. Voltar");
        String res = s.nextLine();

        if ("1".equals(res)) {
            Menu mainMenu = new MainMenu(this.client);
            mainMenu.run();
        } else {
            System.out.println("Input inválido!");
            this.run();
        }
    }
}
