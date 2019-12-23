package client.menus;

import client.Client;
import enums.MessageCode;

import java.util.Scanner;

public class PostMenu extends Menu {

    private Client client;

    public PostMenu(Client client) {
        this.client = client;
    }

    @Override
    public void display() {
        System.out.println("-------- POST MENU --------");
    }

    @Override
    public void handleEvents() {
        Scanner s = new Scanner(System.in);

        System.out.println("Novo post:");
        String text = s.nextLine();

        System.out.println("Tags:");
        String tags = s.nextLine();

        String info = text + "::" + tags;

        client.sendMessage(Client.MessageType.POST, info);

        String msg = client.readMessage();

        MessageCode code = MessageCode.valueOf(msg);
        switch (code) {

            case ERROR_POSTING_POST:
                System.out.println("Erro! Não foi possível publicar este post.");
                Menu mainMenu = new MainMenu(this.client);
                mainMenu.run();
                break;

            case OK_SUCCESSFUL_POST:
                System.out.println("Post publicado com sucesso!");
                mainMenu = new MainMenu(this.client);
                mainMenu.run();
                break;

            default:
                break;
        }
    }
}
