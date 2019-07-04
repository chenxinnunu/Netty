package com.chenxin.netty.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * @author chenxin
 * @date 2019/07/04
 */
public class MultiplexerTimeServer implements Runnable {

    private Selector selector;

    private ServerSocketChannel serverChannel;

    private volatile boolean stop;

    /**
     * @author chenxin
     * @date 2019-07-04 10:33
     * @description 初始化多路复用器，绑定监听端口
     */

    public MultiplexerTimeServer(int port) {
        try {
            //第一步，打开ServerSocketChannel，监听客户端的连接，它是所有客户端连接的父管道
            serverChannel = ServerSocketChannel.open();

            //第二步，绑定监听端口，设置连接为异步非阻塞模式，他的backlog为1024
            serverChannel.socket().bind(new InetSocketAddress(port), 1024);
            serverChannel.configureBlocking(false);

            //开启多路复用器
            selector = Selector.open();

            //将ServerSocketChannel注册到Selector，监听SelectionKey.OP_ACCEPT位，如果失败则退出
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("The time server is start in : " + port);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void stop() {
        this.stop = true;
    }

    @Override
    public void run() {
          /*
          while循环体中循环遍历selector，休眠时间为1s，无论是否有读写等事件产生，selector每隔一秒都会被唤醒一次，
          selector也提供了一个无参的select方法，当有处于就绪状态的Channel时，selector将返回该Channel的SelectionKey集合。
          通过对就绪状态的Channel进行迭代，可以进行网络的异步读写操作。
          */
        while (!stop) {
            try {
                selector.select(1000);
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                SelectionKey key = null;
                while (iterator.hasNext()) {
                    key = iterator.next();
                    iterator.remove();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //private
}
