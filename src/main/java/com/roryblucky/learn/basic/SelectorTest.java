package com.roryblucky.learn.basic;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class SelectorTest {
    public static void trySelect() throws IOException {
        ServerSocketChannel socketChannel = ServerSocketChannel.open(); //创建ServerSocketChannel
        socketChannel.configureBlocking(false);
        ServerSocket ss = socketChannel.socket();
        InetSocketAddress address = new InetSocketAddress(8888);
        ss.bind(address);

        Selector selector = Selector.open();//创建Selector
        socketChannel.register(selector, SelectionKey.OP_ACCEPT);//向Selector注册Channel，并注册监听事件
        final ByteBuffer msg = ByteBuffer.wrap("Hello!\r\n".getBytes());
        while (true) {
            int readyChannels = selector.select();//询问有没有准备好的事件
            if (readyChannels == 0) {
                continue;
            }
            //SelectionKey代表在selector上注册的Channel
            Set<SelectionKey> selectionKeys = selector.selectedKeys();//拿到selector上监听Channel的SelectionKeys
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {//遍历SelectionKey
                SelectionKey key = iterator.next();
                //https://stackoverflow.com/questions/36041054/why-iterate-selectionkey-with-iterator-remove-in-java-nio-selector
                iterator.remove();//处理Channel时需要remove，如果不remove，下次循环进来，key还会在set中。
                if (key.isAcceptable()) { //如果当前Channel是准备就绪的(由于是ServerSocketChannel，准备就绪意味着接受了新的连接)
                    ServerSocketChannel server = (ServerSocketChannel) key.channel();
                    SocketChannel client = server.accept();//拿到客户端通道
                    client.configureBlocking(false);
                    client.register(selector, SelectionKey.OP_WRITE |
                            SelectionKey.OP_READ, msg);//注册selector监听客户端Channel，并附上额外信息，可以被获取
                    System.out.println("Accepted connection from " + client);
                    //此时ServerSocketChannel已经接受了新的连接，由于又向Selector注册监听了客户端Channel，所以下一次循环中，还会
                    //处理客户端Channel，客户端Channel是可写的。
                }

                if (key.isWritable()) {//处理客户端Channel
                    SocketChannel client = (SocketChannel) key.channel();
                    ByteBuffer byteBuffer = (ByteBuffer) key.attachment();//获取到注册监听时，附上的额外信息。
                    while (byteBuffer.hasRemaining()) {//向客户端Channel写数据，此时客户端会收到Hello!信息。
                        if (client.write(byteBuffer) == 0) {
                            break;
                        }
                    }

                    client.close();
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        trySelect();
    }
}
