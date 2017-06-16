import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CountDownLatch;

/**
 * Created by zhangyun on 2017/6/15.
 */
public class AsynTimeServerHandler implements Runnable {

    private int port;
    CountDownLatch latch;
    AsynchronousServerSocketChannel asynchronousServerSocketChannel;


    public AsynTimeServerHandler(int port){

        this.port = port;
        try {
            asynchronousServerSocketChannel = AsynchronousServerSocketChannel.open();
            asynchronousServerSocketChannel.bind(new InetSocketAddress(port));
            System.out.println("Time server is start in port :" + port);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        latch = new CountDownLatch(1);
        doAccept();
        try {
//       Causes the current thread to wait until the latch has counted down to zero,
// unless the thread is interrupted.
            latch.await();
        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    public void doAccept(){
        /*
        * This method initiates an asynchronous operation to accept a connection made to this channel's socket.
         * The handler parameter is a completion handler that is invoked when a connection is accepted (or the operation fails).
        * The result passed to the completion handler is the AsynchronousSocketChannel to the new connection.
        * */
        asynchronousServerSocketChannel.accept(this, new AcceptCompletonHandler());
    }
}


