package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Process
{
    private final int port;
    private AsynchronousServerSocketChannel ssc;

    public Process(int port)
    {
        this.port = port;
    }


    public void start() {
        try {
            AsynchronousChannelGroup g =
                    AsynchronousChannelGroup.withFixedThreadPool(1,
                            Executors.defaultThreadFactory());

            ssc = AsynchronousServerSocketChannel.open(g);
            ssc.bind(new InetSocketAddress(12345));

            //ssc.accept(null, new ClientState(ssc));

            g.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}

