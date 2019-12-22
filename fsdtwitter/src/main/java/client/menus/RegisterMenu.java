package client.menus;

import client.Client;

import java.util.Scanner;

public class RegisterMenu extends Menu {

    private Client client;

    public RegisterMenu(Client client) {
        this.client = client;
    }

    @Override
    public void display() {
        System.out.println("-------- REGISTER MENU --------");
    }

    @Override
    public void handleEvents() {
        Scanner s = new Scanner(System.in);

        System.out.print("username: ");
        String username = s.nextLine();

        System.out.print("password: ");
        String password = s.nextLine();
    }
}
