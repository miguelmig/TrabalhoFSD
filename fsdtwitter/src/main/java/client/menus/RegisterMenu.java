package client.menus;

import client.Client;
import enums.MessageCode;

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


        String msg = client.readMessage();

        MessageCode code = MessageCode.valueOf(msg);
        switch (code) {

            case ERROR_USER_ALREADY_EXISTS:
                System.out.println("Erro! Já existe um utilizador com este nome.");
                Menu registerMenu = new RegisterMenu(this.client);
                registerMenu.run();
                break;

            case OK_SUCCESSFUL_REGISTER:
                System.out.println("Registo efetuado com sucesso!");
                Menu startMenu = new StartMenu(this.client);
                startMenu.run();
                break;

            default:
                break;
        }
    }
}
