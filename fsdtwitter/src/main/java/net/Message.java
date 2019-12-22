package net;

import config.Config;

public class Message
{

    private int[] vectorClock = new int[Config.MAX_PROCESSES];
    private byte[] content;


    public int[] getVectorClock() {
        return vectorClock;
    }
    public void setVectorClock(int[] vc) { vectorClock = vc; }

    public byte[] getContent() {
        return this.content;
    }
    public void setContent(byte[] cont) { content = cont; }
}
