/**
 * Created by zhangyun on 2017/6/12.
 */

import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;

public class TimeServer {

    public  static void main(String[] args) throws IOException {
        int port = 8081;
        if (args != null && args.length > 0){
            try {
                port = Integer.valueOf(args[0]);
            }catch (NumberFormatException e){

            }
        }

        ServerSocket server = null;

        try {
            server = new ServerSocket(port);
            System.out.println("The time server is start in port :" + port);
            Socket socket = null;
            TimeServerHandlerExecutePool singleExecutor = new TimeServerHandlerExecutePool(50,10000);

            while (true){
                socket = server.accept();
                singleExecutor.execute(new TimeServerHandler(socket));
//                new Thread(new TimeServerHandler(socket)).start();
            }
        }finally {
            if (server != null){
                System.out.println("The time server close");
                server.close();
                server = null;
            }
        }
    }
}
