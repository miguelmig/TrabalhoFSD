package client.menus;

import client.Client;

import java.util.Scanner;

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

        String msg = client.readMessage();
        String[] posts = msg.split("::");

        for (String post : posts) {
            System.out.println(post);
        }

        Scanner s = new Scanner(System.in);
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
