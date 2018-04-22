package com.roryblucky.learn.basic;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CountDownLatch;

public class AIOTimeClient {

    public static void main(String[] args) {
        Thread thread = new Thread(new AsyncTimeClientHandler("127.0.0.1", 9000));
        thread.start();
    }




    static class AsyncTimeClientHandler implements CompletionHandler<Void, AsyncTimeClientHandler>, Runnable {

        private AsynchronousSocketChannel client;
        private String host;
        private int port;
        private CountDownLatch downLatch;


        public AsyncTimeClientHandler(String host, int port) {
            this.host = host;
            this.port = port;
            try {
                this.client = AsynchronousSocketChannel.open();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            this.downLatch = new CountDownLatch(1);
            client.connect(new InetSocketAddress(this.host, port), this, this);
            try {
                downLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void completed(Void result, AsyncTimeClientHandler attachment) {
            byte[] req = "Hello!".getBytes();
            ByteBuffer buffer = ByteBuffer.allocate(req.length);
            buffer.put(req);
            buffer.flip();
            client.write(buffer, buffer, new CompletionHandler<Integer, ByteBuffer>() {
                @Override
                public void completed(Integer result, ByteBuffer attachment) {
                    if (attachment.hasRemaining()) {
                        client.write(attachment, attachment, this);
                    } else {
                        ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                        client.read(readBuffer, readBuffer, new CompletionHandler<Integer, ByteBuffer>() {
                            @Override
                            public void completed(Integer result, ByteBuffer attachment) {
                                attachment.flip();
                                byte[] resp = new byte[attachment.remaining()];
                                attachment.get(resp);
                                try {
                                    String body = new String(resp, "UTF-8");
                                    System.out.println("resp: " + body);
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }
                                downLatch.countDown();
                            }

                            @Override
                            public void failed(Throwable exc, ByteBuffer attachment) {
                                try {
                                    client.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }

                @Override
                public void failed(Throwable exc, ByteBuffer attachment) {
                    try {
                        client.close();
                        downLatch.countDown();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        @Override
        public void failed(Throwable exc, AsyncTimeClientHandler attachment) {
            try {
                client.close();
                downLatch.countDown();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
