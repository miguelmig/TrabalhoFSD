package net;

public class Message
{
    public int[] vectorClock = new int[Config.MAX_PROCESSES];
    public byte[] content;
}
