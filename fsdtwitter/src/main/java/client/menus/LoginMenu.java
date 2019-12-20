package client.menus;

import java.util.Scanner;

public class LoginMenu implements Menu {

    @Override
    public void display() {
        System.out.println("LOGIN MENU");
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
