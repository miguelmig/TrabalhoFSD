package client.menus;

import client.Client;

import java.util.Scanner;

public class MainMenu extends Menu {

    private Client client;

    public MainMenu(Client client) {
        this.client = client;
    }


    @Override
    public void display() {
        StringBuilder str = new StringBuilder();
        str.append("-------- MAIN MENU --------\n")
                .append("O que deseja fazer?\n")
                .append("1. Publicar uma mensagem\n")
                .append("2. Consultar lista de tópicos subscrita\n")
                .append("3. Consultar as últimas mensagens enviadas para os tópicos subscritos");

        System.out.println(str);
    }

    @Override
    public void handleEvents() {
        Scanner s = new Scanner(System.in);
        int res = s.nextInt();

        switch (res) {
            case 1:
                client.sendMessage(Client.MessageType.POST, null);
                Menu postMenu = new PostMenu(this.client);
                postMenu.run();
                break;

            case 2:
                client.sendMessage(Client.MessageType.GET_TOPICS, null);
                Menu topicsMenu = new TopicsMenu(this.client);
                topicsMenu.run();
                break;

            case 3:
                client.sendMessage(Client.MessageType.GET_LAST_POSTS, null);
                Menu lastPostsMenu = new LastPostsMenu(this.client);
                lastPostsMenu.run();
                break;

            default:
                System.out.println("Input inválido");
                break;
        }

    }
}
