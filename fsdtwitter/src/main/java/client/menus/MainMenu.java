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
                .append("2. Subscrever tópicos\n")
                .append("3. Consultar lista de tópicos subscrita\n")
                .append("4. Consultar as últimas mensagens " +
                        "enviadas para os tópicos subscritos");

        System.out.println(str);
    }

    @Override
    public void handleEvents() {
        Scanner s = new Scanner(System.in);
        String res = s.nextLine();

        switch (res) {
            case "1":
                Menu postMenu = new PostMenu(this.client);
                postMenu.run();
                break;

            case "2":
                Menu subscribeMenu = new SubscribeMenu(this.client);
                subscribeMenu.run();
                break;

            case "3":
                Menu topicsMenu = new TopicsMenu(this.client);
                topicsMenu.run();
                break;

            case "4":
                Menu lastPostsMenu = new LastPostsMenu(this.client);
                lastPostsMenu.run();
                break;

            default:
                System.out.println("Input inválido");
                this.run();
                break;
        }
    }
}
