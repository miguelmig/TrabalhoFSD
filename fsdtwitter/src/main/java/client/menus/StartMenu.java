package client.menus;

import client.Client;

import java.util.Scanner;

public class StartMenu extends Menu {

    private Client client;

    public StartMenu(Client client) {
        this.client = client;
    }

    @Override
    public void display() {
        StringBuilder str = new StringBuilder();
        str.append("Select one option:\n")
                .append("1. Login\n")
                .append("2. Register");

        System.out.println(str);
    }

    @Override
    public void handleEvents() {
        Scanner s = new Scanner(System.in);
        int res = s.nextInt();

        switch (res) {
            case 1:
                clear();
                Menu loginMenu = new LoginMenu(this.client);
                loginMenu.run();
                break;

            case 2:
                clear();
                Menu registerMenu = new RegisterMenu(this.client);
                registerMenu.run();
                break;

            default:
                break;
        }
    }


}
