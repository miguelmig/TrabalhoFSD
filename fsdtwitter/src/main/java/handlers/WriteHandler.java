package handlers;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class WriteHandler implements CompletionHandler<Integer, Object> {

    private AsynchronousSocketChannel socketChannel;
    private ByteBuffer buf;

    public WriteHandler(AsynchronousSocketChannel socketChannel, ByteBuffer buf) {
        this.socketChannel = socketChannel;
        this.buf = buf;
    }

    @Override
    public void completed(Integer result, Object attachment) {
        this.buf.flip();
        this.socketChannel.read(this.buf, null,
                new ReadHandler(this.socketChannel, this.buf));
    }

    @Override
    public void failed(Throwable exc, Object attachment) {
        exc.printStackTrace();
    }
}
