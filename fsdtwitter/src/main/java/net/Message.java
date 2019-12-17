package net;

public class Message
{

    private int[] vectorClock = new int[Config.MAX_PROCESSES];
    private byte[] content;

    public int[] getVectorClock() { return vectorClock; }
    public byte[] getContent()
    {
        return this.content;
    }
}
