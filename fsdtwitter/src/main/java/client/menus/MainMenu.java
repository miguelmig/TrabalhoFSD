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


    }
}
