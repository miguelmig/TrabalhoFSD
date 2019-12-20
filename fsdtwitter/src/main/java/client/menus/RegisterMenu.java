package client.menus;

import java.util.Scanner;

public class RegisterMenu implements Menu {

    @Override
    public void display() {
        System.out.println("REGISTER MENU");
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
