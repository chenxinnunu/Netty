package com.chenxin.netty.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * @author chenxin
 * @date 2019/07/18
 */
public class TimeServer {
    public void bind(int port) throws InterruptedException {
        //创建两个NioEventLoopGroup实例，NioEventLoopGroup是个线程组，他包含了一组Nio线程，
        //专门用于网络事件的处理，实际上他们就是Reactor线程组。
        //这里创建两个的原因是一个用于服务端接受客户端的连接，另一个用于SocketChannel的网络读写。
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            //创建ServerBootstrap对象，他是Netty用于启动Nio服务端的辅助启动类，目的是降低服务端的开发复杂度。
            ServerBootstrap b = new ServerBootstrap();

            b.group(bossGroup, workerGroup)
                    //对应于NIO的ServerSocketChannel类。
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    //网络I/O事件的处理类，记录日志，对消息编解码等。
                    .childHandler(new ChildChannelHandler());
            //绑定端口，同步阻塞等待绑定操作完成，返回ChannelFuture，用于异步操作的通知回调
            ChannelFuture f = b.bind(port).sync();
            //阻塞等待服务器监听端口关闭
            f.channel().closeFuture().sync();
        } finally {
            //优雅的退出，释放线程池资源，他会释放跟shutdownGracefully相关联的资源
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
    private class ChildChannelHandler extends ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(SocketChannel arg0) throws Exception {
            arg0.pipeline().addLast(new TimeServerHandler());
        }
    }

    public static void main(String[] args) throws InterruptedException {
        int port = 8080;
        if (args != null && args.length >0) {
            try {
                port = Integer.valueOf(args[0]);
            } catch (NumberFormatException e) {
                //采用默认值
            }
        }
        new TimeServer().bind(port);
    }
}
