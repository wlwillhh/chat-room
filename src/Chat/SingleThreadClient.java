package Chat;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

public class SingleThreadClient {

    public static void main(String[] args) throws Exception {
        String host = "127.0.0.1";
        int port = 60000;
        Socket socket = new Socket(host, port);
        OutputStream os = socket.getOutputStream();
        InputStream is = socket.getInputStream();
        System.out.println("==============客户端开始发送数据===============");

        InputStreamReader isr = new InputStreamReader(System.in);
        BufferedReader out = new BufferedReader(isr);
        while (true) {
            String input = out.readLine();
            os.write(input.getBytes());
            System.out.println("==============客户端开始接收数据===============");
            byte[] b = new byte[1024];
            int r = is.read(b);
            if(r>-1){
                String str = new String(b).trim();
                if("exit".equals(str)) {
                    System.out.println("==============客户端关闭连接===============");
                    socket.close();
                    break;
                }
                System.out.println( str );
            }
        }
    }
}