package utils;

import spullara.nio.channels.FutureSocketChannel;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

public class FutureLineBuffer {

    private FutureSocketChannel sc;
    private ByteBuffer input = ByteBuffer.allocate(1000);
    private ByteBuffer line  = ByteBuffer.allocate(1000);


    public FutureLineBuffer(FutureSocketChannel sc) {
        this.sc = sc;
        input.flip();
    }


    public CompletableFuture<String> readLine() {
        while(input.hasRemaining()) {
            byte c = input.get();
            if (c == '\n') {
                line.flip();

                byte[] data = new byte[line.remaining()];
                line.get(data);
                line.clear();

                return CompletableFuture.completedFuture(new String(data));
            }
            line.put(c);
        }

        input.clear();
        return sc.read(input)
                .thenCompose(n -> {
                    input.flip();
                    return readLine();
                });
    }

    public CompletableFuture<Integer> write(String msg) {
        byte[] data = msg.getBytes();

        ByteBuffer buf = ByteBuffer.allocate(data.length);
        buf.put(data);
        buf.flip();

        return sc.write(buf);
    }

    public CompletableFuture<Integer> writeln(String msg)
    {
        return write(msg + '\n');
    }

}
