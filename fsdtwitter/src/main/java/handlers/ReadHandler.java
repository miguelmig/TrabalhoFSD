package handlers;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class ReadHandler implements CompletionHandler<Integer, Object> {

    private AsynchronousSocketChannel socketChannel;
    private ByteBuffer buf;

    public ReadHandler(AsynchronousSocketChannel socketChannel, ByteBuffer buf) {
        this.socketChannel = socketChannel;
        this.buf = buf;
    }

    @Override
    public void completed(Integer result, Object attachment) {
        this.buf.flip();
        this.socketChannel.write(this.buf, null,
                new WriteHandler(this.socketChannel, this.buf));
    }

    @Override
    public void failed(Throwable exc, Object attachment) {

    }
}
