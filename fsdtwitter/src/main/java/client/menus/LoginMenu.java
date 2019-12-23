package client.menus;

import client.Client;
import enums.MessageCode;

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

        String msg = client.readMessage();

        MessageCode code = MessageCode.valueOf(msg);
        switch (code) {

            case ERROR_USER_DOESNT_EXIST:
            case ERROR_WRONG_PASSWORD:
                System.out.println("Erro! Login inválido. Tente outra vez.");
                Menu startMenu = new StartMenu(this.client);
                startMenu.run();
                break;

            case OK_SUCCESSFUL_LOGIN:
                System.out.println("Login efetuado com sucesso!");
                Menu mainMenu = new MainMenu(this.client);
                mainMenu.run();
                break;

            default:
                break;
        }
    }
}
