package com.chenxin.netty.iopool;

import com.chenxin.netty.io.TimeServerHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author chenxin
 * @date 2019/07/03
 * 利用线程池避免线程个数太多或者内存溢出
 * 客户端和TimeClient一样
 */
public class TimePoolServer {
    public static void main(String[] args) throws IOException {
        int port = 8080;
        if (args != null && args.length > 0) {
            try {
                port = Integer.valueOf(args[0]);
            } catch (NumberFormatException e) {
                //用默认值
            }
        }
        ServerSocket server = null;
        try {
            server = new ServerSocket(port);
            System.out.println("The time server is start in port :" + port);
            Socket socket = null;
            TimeServerHandlerExecutePool timeServerHandlerExecutePool = new TimeServerHandlerExecutePool(5, 5);
            while (true) {
                socket = server.accept();
                timeServerHandlerExecutePool.execute(new TimeServerHandler(socket));
                System.out.println();
            }
        } finally {
            if (server != null) {
                System.out.println("The server is close");
                server.close();
                server = null;
            }
        }
    }
}
