import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CountDownLatch;

/**
 * Created by zhangyun on 2017/6/15.
 */
public class AsynTimeClientHandler implements CompletionHandler<Void,AsynTimeClientHandler>,Runnable{

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
    public void run() {
        /*
        * count - the number of times countDown must be invoked before threads can pass through await
        * */
        latch = new CountDownLatch(1);

        client.connect(new InetSocketAddress(host,port), this,this);
        try {
            latch.await();
        }catch (InterruptedException e){
            e.printStackTrace();
        }

        try {
            client.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }


    @Override
    public void completed(Void result, AsynTimeClientHandler attachment) {
        byte[] req = "QUERY TIME ORDER".getBytes();
        ByteBuffer writeBuffer = ByteBuffer.allocate(req.length);
        writeBuffer.put(req);
        writeBuffer.flip();
        /*
        * This method initiates an asynchronous write operation to write a sequence of bytes
        * to this channel from the given buffer. The handler parameter is a completion handler
        * that is invoked when the write operation completes (or fails).
        * The result passed to the completion handler is the number of bytes written.*/
        client.write(writeBuffer, writeBuffer, new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed(Integer result, ByteBuffer attachment) {
                if (attachment.hasRemaining()){
                    client.write(attachment,attachment,this);
                }else{
                    ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                    /*
                    * This method initiates an asynchronous read operation to read a
                    * sequence of bytes from this channel into the given buffer.
                    * The handler parameter is a completion handler that is invoked when
                     * the read operation completes (or fails). The result passed to the
                     * completion handler is the number of bytes read or -1 if no bytes
                     * could be read because the channel has reached end-of-stream.*/
                    client.read(readBuffer, readBuffer, new CompletionHandler<Integer, ByteBuffer>() {
                        @Override
                        public void completed(Integer result, ByteBuffer attachment) {
                            attachment.flip();
                            byte[] bytes = new byte[attachment.remaining()];
                            attachment.get(bytes);
                            String body;
                            try {
                                body = new String(bytes,"UTF-8");
                                System.out.println("Now is:" + body);
                                /*
                                *
                                * Decrements the count of the latch, releasing all waiting threads if the count reaches zero.
                                        If the current count is greater than zero then it is decremented. If the new count is zero
                                        then all waiting threads are re-enabled for thread scheduling purposes.
                                * */
                                latch.countDown();
                            }catch (UnsupportedEncodingException e){
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void failed(Throwable exc, ByteBuffer attachment) {
                            try {
                                client.close();
                                latch.countDown();
                            }catch (IOException e){

                            }
                        }
                    });
                }

            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                try {
                    client.close();
                    latch.countDown();
                }catch (IOException e){

                }
            }
        });
    }


    @Override
    public void failed(Throwable exc, AsynTimeClientHandler attachment) {
        try {
            client.close();
            latch.countDown();
        }catch (IOException e){

        }
    }
}
