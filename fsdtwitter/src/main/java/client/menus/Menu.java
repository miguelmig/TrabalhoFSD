package client.menus;

import client.Client;

public abstract class Menu {

    public abstract void display();

    public abstract void handleEvents();

    public void run() {
        this.display();
        this.handleEvents();
    }

    public void clear() {

    }
}
