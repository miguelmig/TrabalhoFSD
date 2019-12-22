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

        String info = username + " " + password;
        client.sendMessage(Client.MessageType.REGISTER, info);

        //TODO confirmar registo
        boolean valid_register = true;
        if (valid_register) {
            Menu newMenu = new StartMenu(this.client);
            newMenu.run();
        } else {
            Menu newMenu = new RegisterMenu(this.client);
            newMenu.run();
        }
    }
}
