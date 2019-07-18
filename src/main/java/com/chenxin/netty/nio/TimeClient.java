package com.chenxin.netty.nio;

/**
 * @author chenxin
 * @date 2019/07/07
 */
public class TimeClient {
    public static void main(String[] args) {
        int port = 8080;

        if(args != null && args.length > 0) {
            try {
                port = Integer.valueOf(args[0]);
            } catch (NumberFormatException e) {
                //用默认值
            }
        }
        new Thread(new TimeClientHandle(port), "TimeClient-001").start();
    }
}
