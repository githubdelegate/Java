import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

/**
 * Created by zhangyun on 2017/6/15.
 */
public class AcceptCompletonHandler implements CompletionHandler<AsynchronousSocketChannel,AsynTimeServerHandler>{
        @Override
        public void completed(AsynchronousSocketChannel result, AsynTimeServerHandler attachment) {
            // 开始等待别的客户端的连接请求
            attachment.asynchronousServerSocketChannel.accept(attachment,this);
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            /*
            * This method initiates an asynchronous read operation to read a sequence of bytes from this channel into the given buffer.
            * The handler parameter is a completion handler that is invoked when the read operation completes (or fails).
            * The result passed to the completion handler is the number of bytes read or -1 if no bytes could be
            * read because the channel has reached end-of-stream.*/
            // 传递result 是为了方便后面操作channel
            result.read(buffer,buffer,new ReadCompletonHandler(result));
        }

        @Override
        public void failed(Throwable exc, AsynTimeServerHandler attachment) {
            exc.printStackTrace();
            attachment.latch.countDown();
        }
}
