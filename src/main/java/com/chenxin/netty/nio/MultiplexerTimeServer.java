package com.chenxin.netty.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

/**
 * @author chenxin
 * @date 2019/07/04
 */
public class MultiplexerTimeServer implements Runnable {

    private Selector selector;

    private ServerSocketChannel servChannel;

    private volatile boolean stop;

    /**
     * @author chenxin
     * @date 2019-07-04 10:33
     * @description 初始化多路复用器，绑定监听端口
     */

    public MultiplexerTimeServer(int port) {
        try {
            //开启多路复用器
            selector = Selector.open();

            //打开ServerSocketChannel，监听客户端的连接，它是所有客户端连接的父管道
            servChannel = ServerSocketChannel.open();
            //设置为非阻塞模式
            servChannel.configureBlocking(false);

            //绑定监听端口，他的backlog为1024
            servChannel.socket().bind(new InetSocketAddress(port), 1024);

            //将ServerSocketChannel注册到Selector，监听SelectionKey.OP_ACCEPT位，如果失败则退出
            servChannel.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("The time server is start in : " + port);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void stop() {
        this.stop = true;
    }

    //独立的I/O线程，用于轮询多路复用器Selector
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
                Iterator<SelectionKey> it = selectionKeys.iterator();
                SelectionKey key = null;
                while (it.hasNext()) {
                    key = it.next();
                    it.remove();
                    try {
                        handleInput(key);
                    } catch (IOException e) {
                        if (key != null) {
                            key.cancel();
                            if (key.channel() != null) {
                                key.channel().close();
                            }
                        }
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //多路复用器关闭后，所有注册在上面的Channel和Pipe等资源都会被自动去注册并关闭，所以不需要重复释放资源
        if (selector != null) {
            try {
                selector.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleInput(SelectionKey key) throws IOException {
        if (key.isValid()) {
            /*
            处理新接入的请求消息,根据Selctionkey的操作位进行判断即可获知网络时间的类型，通过ServerSocketChannel的accpet
            接收客户端的连接请求并创建SocketChannel实力。完成上述操作后，相当于完成了TCP的三次握手，TCP物理链路正式建立。
            注意，我们需要将新建立的SocketChannel设置为异步非阻塞，同时也可以对其TCP参数进行设置，例如TCP接收和发送缓冲区的大小。
            但这是入门的例子，这里没有进行额外的参数设置。
            */
            if (key.isAcceptable()) {
                //accept the new connection
                ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
                SocketChannel sc = ssc.accept();
                sc.configureBlocking(false);
                //add the new connection to the selector
                sc.register(selector, SelectionKey.OP_READ);
            }
            /*
            这里是读取客户端的消息。首先创建一个ByteBuffer，由于事先无法得知客户端传送的码流大小，作为例程，我们开辟一个IMB的缓存区。
            然后调用SocketChannel的read方法读取请求码流。注意，由于已经将SocketChannel设置为异步非阻塞的模式，因此它的read也是
            非阻塞的。使用返回值进行判断，看读取到的字节数，返回值有三种结果：
            返回值大于0：读到了字节，对字节进行编解码
            返回值等于0：没有读取到字节，属于正常的场景，忽略
            返回值小于0：链路已经关闭，需要关闭SocketChannel，释放资源
            当读取到码流之后，进行解码。首先对readBuffer进行filp操作，它的作用是将缓冲区当前的limit设置为position，position设置为0，
            用于后续对缓冲区的读取操作。然后根据缓冲区可读的字节个数创建字节数组，调用ByteBuffer的get操作将缓冲区可读的字节数组复制到新创建
            的字节数组中，最后调用字符串的构造函数创建请求消息体并打印。如果请求消息为“QUERY TIME ORDER”，则把服务器的当前时间编码后返回给客户端。

             */
            if (key.isReadable()) {
                //Read the data
                SocketChannel sc = (SocketChannel)key.channel();
                ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                int readBytes = sc.read(readBuffer);
                if (readBytes > 0) {
                    readBuffer.flip();
                    byte[] bytes = new byte[readBuffer.remaining()];
                    readBuffer.get(bytes);
                    //byte[]转为String
                    String body = new String(bytes, "UTF-8");
                    System.out.println("The time server receive order : " + body);
                    String currentTime = "QUERY TIME ORDER".equalsIgnoreCase(body)
                            ? new Date(System.currentTimeMillis()).toString() : "BAD ORDER";
                    doWrite(sc, currentTime);
                } else if (readBytes < 0) {
                    //对端链路关闭
                    key.cancel();
                    sc.close();
                } else {
                    //读到零字节，忽略
                    ;
                }
            }
        }
    }
    /**
     * @author chenxin
     * @date 2019-07-07 17:13
     * @description 将应答消息异步发送给客户端
     * 首先将字符串b编码成字节数组，根据字符数组的容量创建ByteBuffer，调用ByteBuffer的put操作将字节数组复制到缓冲区中，
     * 然后对缓冲区进行flip操作，最后电影SocketChannel的writer方法将缓冲区的字节数组发送出去。
     * 需要注意的是，由于SocketChannel是异步非阻塞的，它并不能保证一次能够把需要发送的字节数组发送完，此时会出现“写半包“问题。
     * 我们需要注册写操作，不断轮询Selector将没用发送的ByteBuffer发送完毕，然后可以通过ByteBuffer的hasRemain（）方法判断
     * 消息是否发送完成。
     * 这只是简单的教程，在后面会有“写半包“的详细说明
     */
    private void doWrite(SocketChannel channel, String response) throws IOException {
        if (response != null && response.trim().length() > 0) {
            byte[] bytes = response.getBytes();
            ByteBuffer writeBuffer = ByteBuffer.allocate(bytes.length);
            writeBuffer.put(bytes);
            writeBuffer.flip();
            channel.write(writeBuffer);
        }
    }
}
