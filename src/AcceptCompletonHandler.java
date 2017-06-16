import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

/**
 * Created by zhangyun on 2017/6/15.
 */
public class AcceptCompletonHandler implements CompletionHandler<AsynchronousSocketChannel,AsynTimeServerHandler>{
        @Override
        public void completed(AsynchronousSocketChannel result, AsynTimeServerHandler attachment) {
            attachment.asynchronousServerSocketChannel.accept(attachment,this);
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            result.read(buffer,buffer,new ReadCompletonHandler(result));
        }

        @Override
        public void failed(Throwable exc, AsynTimeServerHandler attachment) {
            exc.printStackTrace();
            attachment.latch.countDown();
        }
}
