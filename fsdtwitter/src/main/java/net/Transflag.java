package net;

public class Transflag {
    private boolean flag;

    public Transflag() {
        this.flag = true;
    }

    public synchronized void canStartTransaction() throws InterruptedException {
        while(!this.flag)
            this.wait();

        this.flag = false;

    }

    public synchronized void endTransaction() { this.flag = true; this.notify(); }

}
