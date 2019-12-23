package client.menus;

import client.Client;
import enums.MessageCode;

import java.util.Scanner;

public class SubscribeMenu extends Menu {

    private Client client;

    public SubscribeMenu(Client client) {
        this.client = client;
    }

    @Override
    public void display() {
        System.out.println("-------- SUBSCREVER TÓPICOS --------");
    }

    @Override
    public void handleEvents() {
        Scanner s = new Scanner(System.in);

        System.out.println("Que tópicos deseja subscrever?");
        String tags = s.nextLine();

        client.sendMessage(Client.MessageType.SUBSCRIBE, tags);

        String msg = client.readMessage();
        MessageCode code = MessageCode.valueOf(msg);

        switch (code) {
            case OK_SUCCESSFUL_SUBSCRIBE:
                System.out.println("Subscrição efetuada com sucesso!");
                Menu mainMenu = new MainMenu(this.client);
                mainMenu.run();
                break;

            default:
                break;
        }
    }
}
