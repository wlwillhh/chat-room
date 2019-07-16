package Chat;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
//必须可以接收多个客户端连接请求---多线程
//每当有一个客户端连接到服务器后，就把socket包装作为一个线程处理，不同的线程在不同的子线程中处理，互不影响--线程池

//难点1.如何保存多个客户端的连接
// 用Map实现，里面有键值对----ConcurrentHashMap<> ()--保证多线程注册时，用户名一定只有一个，保证其安全
// 多个用户能同时注册到服务器，线程如果不安全，用户名有可能重复，就导致之前注册的用户信息无效

public class MulThreadServer {
    //用来保存用户的客户端的列表
    private static Map<String, Socket> clientLists = new ConcurrentHashMap<> ();

    //把socket包装作为一个线程,专门用来处理每个客户端的输入，输出请求
    private static class ExecuteClientRequest implements Runnable {
        //需要接收一个客户端
        private Socket client;

        public ExecuteClientRequest(Socket client) {
            this.client = client;
        }

        private void info(String clientName) {

        }

        //具体处理每个客户端的输入输出请求
        //服务器的作用，数据的转发
        @Override
        public void run() {
            //获取用户输入流,读取用户发来的信息
            try {
                Scanner in = new Scanner (client.getInputStream ());
                //从用户发来的信息
                String strFromClient = "";
                //得不停的输入输出
                while (true) {
                    if (in.hasNext ()) {
                        //获取到她的输入
                        strFromClient = in.nextLine ();
                        //window下消除用户输入自带的\r
                        //正则表达式,Pattern--表示识别的时那种格式
                        //将\r替换为空字符串
                        Pattern pattern = Pattern.compile ("\r");
                        Matcher matcher = pattern.matcher (strFromClient);//要替换的字符串放进去
                        strFromClient = matcher.replaceAll ("");
                    }

                    //根据输入的内容，分析是群聊信息还是私聊还是退出
                    //userName:wl------注册---到我的服务器
                    if (strFromClient.startsWith ("userName")) {
                        //1.拆出用户名
                        String userName = strFromClient.split ("\\:")[1];
                        userRegister (userName, client);
                    }
                    //G：----群聊内容
                    if (strFromClient.startsWith ("G:")) {
                        String groupMsg = strFromClient.split ("\\:")[1];
                        groupChat (groupMsg);
                    }
                    //P:私聊--告诉给谁发--用户名--私聊内容
                    if (strFromClient.startsWith ("P:")) {
                        String[] thing = strFromClient.split ("\\:");
                        String userName = thing[1];
                        String privateMsg = thing[2];
                        privateChat (userName, privateMsg);
                    }
                    //用户退出：wl:exit
                    if (strFromClient.contains ("exit")) {
                        String userName = strFromClient.split ("\\:")[0];
                        useOffLine (userName);
                        break;
                    }

                }
            } catch (IOException e) {
                e.printStackTrace ();
            }

        }

        //1.注册
        private void userRegister(String userName, Socket socket) throws IOException {
            //实际就是把信息放在Map中
            clientLists.put (userName, socket);
            //可以判断一下用户名是否存在，socket是否关闭
            System.out.println ("用户" + userName + "上线了");
            System.out.println ("当前聊天室人数为：" + clientLists.size ());
            //告诉用户已注册成功
            PrintStream out = new PrintStream (socket.getOutputStream (), true, "UTF-8");
            out.println (userName+"注册成功");
            out.println ("当前聊天室人数为：" + clientLists.size ());
        }

        //2.获取到群聊信息
        //群聊方法：遍历Map，向每个客户端输出一遍
        private void groupChat(String groupMsg) {
            //1.将Map变为
            Set<Map.Entry<String, Socket>> clientEnter = clientLists.entrySet ();
            //2.取得迭代器Set
            Iterator<Map.Entry<String, Socket>> iterator = clientEnter.iterator ();
            //3.遍历
            while (iterator.hasNext ()) {
                //取出每一个客户端实体，就是取得了输出流
                Map.Entry<String, Socket> client = iterator.next ();
                //拿到客户端输出流输出群聊信息

                try {
                    PrintStream out = new PrintStream (client.getValue ()
                            .getOutputStream (), true, "UTF-8");
                    out.println ("群聊信息为：" + groupMsg);
                } catch (IOException e) {
                    e.printStackTrace ();
                }
            }
        }

        //3.私聊方法
        private void privateChat(String userName, String privateMsg) throws IOException {

            //1.取出userName对应的Socket
            Socket client = clientLists.get (userName);
            //2.获取输出流
            PrintStream out = new PrintStream (client.getOutputStream (), true, "UTF-8");
            out.println ("用户" + this.client + "发给你的私聊信息为" + privateMsg);

        }

        private void useOffLine(String userName) {
            //删除Map中的用户实体
            clientLists.remove (userName);
            System.out.println ("用户" + userName + "已下线");
            System.out.println ("当前聊天室人数为：" + clientLists.size ());

        }
    }


    public static void main(String[] args) throws Exception {
        //1.建立基站
        ServerSocket serverSocket = new ServerSocket (5210);
        //2.使用线程池来同时处理多个客户端连接
        //创建一个大小为20的线程池来处理请求
        ExecutorService executorService = Executors
                .newFixedThreadPool (20);
        System.out.println ("等待客户端连接");

        for (int i = 0; i < 20; i++) {
            Socket client = serverSocket.accept ();
            System.out.println ("有新的客户端连接，端口号为" + client.getPort ());

            PrintStream out = new PrintStream (client
                    .getOutputStream (), true, "UTF-8");
            out.println ("服务器发来的介绍信息为：" +"\n"+
                    "    “使用指南”   "+"\n"+
                    "1.第一步：注册，" +
                    "使用userName+<你想要给自己起的名字>"+"\n"+
                    "2.第二步：发消息的使用方式"+"\n"+
                    "         群发消息：输入：G:+<你想要群发的内容>"+"\n"+
                    "         私法消息：输入：P:+<想要发的人的userName+<你想要私发的内容>"+"\n"+
                    "         退出聊天：输入：exit");

            //将收到客户端信息传入上面的方法中,提交一个任务
            executorService.submit (new ExecuteClientRequest (client));
        }
        //当多于20个线程时，关闭线程池与服务端
        executorService.shutdown ();
        serverSocket.close ();
    }
}

