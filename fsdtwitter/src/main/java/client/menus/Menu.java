package client.menus;

public interface Menu {

    void display();

    void handleEvents();

    default void run() {
        this.display();
        this.handleEvents();
    }

    default void clear() {

    }
}
