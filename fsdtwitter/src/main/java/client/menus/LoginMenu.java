package client.menus;

import client.Client;

import java.util.Scanner;

public class LoginMenu extends Menu {

    private Client client;

    public LoginMenu(Client client) {
        this.client = client;
    }

    @Override
    public void display() {
        System.out.println("-------- LOGIN MENU --------");
    }

    @Override
    public void handleEvents() {
        Scanner s = new Scanner(System.in);

        System.out.print("username: ");
        String username = s.nextLine();

        System.out.print("password: ");
        String password = s.nextLine();

        String info = username + " " + password;
        client.sendMessage(Client.MessageType.LOGIN, info);

        //TODO confirmar login
        boolean valid_login = true;
        if (valid_login) {
            Menu newMenu = new MainMenu(this.client);
            newMenu.run();
        } else {
            System.out.println("Login inválido. Tente outra vez.");
            Menu newMenu = new LoginMenu(this.client);
            newMenu.run();
        }
    }
}
