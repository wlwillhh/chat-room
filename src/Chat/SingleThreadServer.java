package Chat;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class SingleThreadServer {

    public static void main(String[] args) throws Exception {
        int port = 60000;
        ServerSocket server = new ServerSocket(port);
        Socket socket = server.accept();

        OutputStream os = socket.getOutputStream();
        InputStream is = socket.getInputStream();
        System.out.println("==============服务端开始接收数据===============");
        for (byte[] b = new byte[1024]; is.read(b) != -1; b = new byte[1024]) {
            String str = new String(b).trim();
            if("e".equals(str)) {
                os.write(("exit").getBytes());
                System.out.println("==============服务端关闭连接===============");
                socket.close();
                server.close();
                break;
            }
            System.out.println("{"+ str +"}");
            //写入到客户端
            os.write(("recieve:"+str).getBytes());
        }
    }


}
