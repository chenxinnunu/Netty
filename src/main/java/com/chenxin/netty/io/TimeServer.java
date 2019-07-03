package com.chenxin.netty.io;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author chenxin
 * @date 2019/07/03
 * 同步阻塞I/O服务端
 */
public class TimeServer {
    public static void main(String[] args) throws IOException {

        int port = 8080;
        if (args != null && args.length > 0) {
            try {
                port = Integer.valueOf(args[0]);
            } catch (NumberFormatException e) {
                //如果没有入参，采用默认值8080
            }
        }
            ServerSocket server = null;
            try {
                server = new ServerSocket(port);
                System.out.println("The time server is start in port :" + port);
                Socket socket = null;
                while (true) {
                    socket = server.accept();
                    new Thread(new TimeServerHandler(socket)).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (server != null) {
                    System.out.println("The server is close");
                    server.close();
                    server = null;
                }
            }
    }
}
