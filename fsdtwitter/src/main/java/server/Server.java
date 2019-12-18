package server;
import config.Config;
import io.atomix.utils.net.Address;
import net.*;

import java.util.ArrayList;
import java.util.List;

public class Server
{
    private static List<Process> processes = new ArrayList<>();

    private static MessageHandler mh;
    private static int port;

    public static void main(String[] args)
    {
        if (args.length < 1)
        {
            port = 8000;
        }
        else
        {
            port = Integer.parseInt(args[0]);
        }

        BuildProcessList();
        
        mh = new MessageHandler(port);

        mh.startMessageHandler();
    }

    private static void BuildProcessList()
    {
        for(int port = Config.ADDR_START; port < Config.ADDR_START + Config.MAX_PROCESSES; ++port)
        {
            processes.add(new Process(port));
        }
    }

    public static void BroadcastMessage(Message msg)
    {

    }

    public static void DeliverMsg(String message_type, Address addr, Message data)
    {
        System.out.println("Delivering message!");
    }

}
