package com.chenxin.netty.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.io.UnsupportedEncodingException;
import java.util.Date;

/**
 * @author chenxin
 * @date 2019/07/18
 *
 * 通常只需要关注channelRead和exceptionCaught两个方法
 */
public class TimeServerHandler extends ChannelHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws UnsupportedEncodingException {
        ByteBuf buf = (ByteBuf) msg;
        //readableBytes方法获取缓冲可读的字节数，并创建byte数组
        byte[] req = new byte[buf.readableBytes()];
        buf.readBytes(req);
        String body = new String(req, "UTF-8");
        System.out.println("The time server receive order : " + body);
        String currentTime = "QUERY TIME ORDER".equalsIgnoreCase(body) ? new Date(System.currentTimeMillis()).toString() : "BAD ORDER";
        ByteBuf resp = Unpooled.copiedBuffer(currentTime.getBytes());
        ctx.write(resp);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        //将消息发送队列中的消息写入到SocketChannel发送给对方
        //从性能方面考虑，为了防止频繁的唤醒Selector进行消息发送，Netty的write方法并不直接将消息写入到SocketChannel中，
        //调用writer方法只是把待发送的消息放到缓冲数组中，再通过调用flush方法，将发送缓冲区的消息全部写入到SocketChannel中
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        //当发生异常时，关闭ChannelHandlerContext，释放和ChannelHandlerContext相关的资源
        ctx.close();
    }
}
