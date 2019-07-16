package Chat;

//基于多线程的客户端
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;
//实现的功能：注册。群聊，私聊
//读取服务器发来信息的线程
class ReadFromServer implements Runnable {
    private Socket client;
    //通过构造方法把共享的Socket客户端传进来，读和写是同一个Socket
    public ReadFromServer(Socket client) {
        this.client = client;
    }
    @Override
    public void run() {
        //获取输入流来取得服务器发来的信息
        try {
            Scanner in = new Scanner (client.getInputStream ());//这里只读取了一次
            while (true) {//进行多次读取
                if (client.isClosed ()) {
                    System.out.println ("客户端已关闭");
                    in.close ();
                    break;
                }
                if (in.hasNext ()) {
                    System.out.println ("服务器发来的信息为：" + in.nextLine ());
                }
            }
        } catch (IOException e) {
            e.printStackTrace ();
        }
    }
}
//向服务器发送信息线程
class  SendMsgToServer implements Runnable{
    private Socket client;
    //通过构造方法把共享的Socket客户端传进来，读和写是同一个Socket
    public SendMsgToServer(Socket client) {
        this.client = client;
    }
    @Override
    public void run() {
        //获取输出流，向服务器发送信息
        try {
            PrintStream printStream=new PrintStream (client.getOutputStream (),true,"UTF-8");
        //获取用户输入,从键盘获取
            Scanner scanner=new Scanner (System.in);
            while(true){
                System.out.println ("请输入要向服务器发送的信息");
                //将用户的输入保存以下
                String strFromUser="";
                if(scanner.hasNext ()){
                    //获取用户
                    strFromUser=scanner.nextLine ();
                }
                //不管输出什么，都应该将内容输出到服务端
                printStream.println (strFromUser);
                //判断退出的条件，字符串包含exit
                if(strFromUser.contains ("exit")){
                    System.out.println ("您已退出聊天室");

                    scanner.close ();
                    printStream.close ();
                    client.close ();
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace ();
        }
    }
}
public class MulThreadClient {
    public static void main(String[] args) throws IOException {
        //根据指定的IP和端口号建立连接
    Socket client=new Socket ("127.0.0.1",5210);
    //启动读线 程与输出线程
        Thread readThread=new Thread (new ReadFromServer (client));
        Thread sendThread= new Thread (new SendMsgToServer(client));
        readThread.start ();
        sendThread.start ();
    }
}

