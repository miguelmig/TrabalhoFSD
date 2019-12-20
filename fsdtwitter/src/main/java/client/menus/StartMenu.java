package client.menus;

import java.util.Scanner;

public class StartMenu implements Menu {

    @Override
    public void display() {
        StringBuilder str = new StringBuilder();
        str.append("Select one option:\n")
                .append("1. Login\n")
                .append("2. Register");

        System.out.println(str);
    }

    @Override
    public void handleEvents() {
        Scanner s = new Scanner(System.in);
        int res = s.nextInt();

        switch (res) {
            case 1:
                clear();
                Menu loginMenu = new LoginMenu();
                loginMenu.run();
                break;

            case 2:
                clear();
                Menu registerMenu = new RegisterMenu();
                registerMenu.run();
                break;

            default:
                break;
        }
    }


}
