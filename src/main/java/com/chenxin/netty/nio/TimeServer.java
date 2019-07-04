package com.chenxin.netty.nio;

/**
 * @author chenxin
 * @date 2019/07/04
 */
public class TimeServer {
    public static void main(String[] args) {
        int port = 8080;
        if (args != null && args.length > 0) {
            try {
                port = Integer.valueOf(args[0]);
            } catch (NumberFormatException e) {
                //使用默认值
            }
        }

    }
}
