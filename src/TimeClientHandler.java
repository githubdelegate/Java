/**
 * Created by zhangyun on 2017/6/14.
 */

import java.io.IOException;
import java.lang.Runnable;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class TimeClientHandler implements Runnable {

    private String host;
    private int port;
    private Selector selector;
    private SocketChannel socketChannel;
    private volatile boolean stop;

    public TimeClientHandler(String host,int port){
        this.host = host == null ? "127.0.0.1" : host;
        this.port = port;

        try {
            selector = Selector.open();
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
        }catch (IOException e){
            e.printStackTrace();
            System.exit(1);
        }

    }

    @Override
    public void run() {

        try {
            doConnect();
        }catch (IOException e){
            e.printStackTrace();
            System.exit(1);
        }

        while (!stop){
            try {
                selector.select(1000);
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> it = selectionKeys.iterator();
                SelectionKey key = null;
                while (it.hasNext()){
                    key = it.next();
                    it.remove();
                    try {
                        handleInput(key);
                    }catch (Exception e){
                        if (key != null){
                            key.cancel();
                            if (key.channel() != null)
                                key.channel().close();
                        }
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
                System.exit(1);
            }
        }// while

        if (selector != null){
            try {
                selector.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    /**
     * 处理复选器 筛选出来的 这里处理 读取服务端返回的数据
     * @param key
     * @throws IOException
     */
    private void handleInput(SelectionKey key) throws IOException{
        if (key.isValid()){
            SocketChannel sc = (SocketChannel) key.channel();
            // 判断是否连接成功，
            if (key.isConnectable()){
                // channel 是否连接
                if (sc.finishConnect()){
                    sc.register(selector,SelectionKey.OP_READ);

                    // 发送请求
                    doWrite(sc);
                }else {
                    System.exit(1);
                }

                // 服务端是否有数据返回
                if (key.isReadable()){
                    ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                    int readBytes = sc.read(readBuffer);
                    if (readBytes > 0){
                        readBuffer.flip();
                        byte[] bytes = new byte[readBuffer.remaining()];
                        String body = new String(bytes,"UTF-8");
                        System.out.println("Now is :" + body);
                        this.stop = true;
                    }else if (readBytes < 0){
                        key.cancel();
                        sc.close();
                    }else {
                        ;
                    }

                }
            }
        }
    }

    /**
     * 发起链接
     * @throws IOException
     */
    private void doConnect() throws IOException {
        if (socketChannel.connect(new InetSocketAddress(host,port))){
            socketChannel.register(selector, SelectionKey.OP_READ);
            doWrite(socketChannel);
        }else{
            socketChannel.register(selector,SelectionKey.OP_CONNECT);
        }
    }

    /**
     * 发送请求
     * @param sc
     * @throws IOException
     */
    private void doWrite(SocketChannel sc) throws IOException{
        byte[] req = "QUERY TIME ORDER".getBytes();
        ByteBuffer writeBuffer = ByteBuffer.allocate(req.length);
        writeBuffer.put(req);
        writeBuffer.flip();
        sc.write(writeBuffer);
        if (!writeBuffer.hasRemaining()){
            System.out.println("Send order 2 server succeed");
        }
    }
}
