import java.io.IOException;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CountDownLatch;

/**
 * Created by zhangyun on 2017/6/15.
 */
public class AsynTimeClientHandler implements CompletionHandler<Void,AsynTimeServerHandler> {

    private AsynchronousSocketChannel client;
    private String host;
    private int port;
    private CountDownLatch latch;

    public AsynTimeClientHandler(String host,int port){
        this.host = host;
        this.port = port;

        try {
            client = AsynchronousSocketChannel.open();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void completed(Void result, AsynTimeServerHandler attachment) {

    }

    @Override
    public void failed(Throwable exc, AsynTimeServerHandler attachment) {

    }
}
