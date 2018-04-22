package com.roryblucky.learn.basic;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

public class AIOTimeServer {
    public static void main(String[] args) {
        AsyncTimeServerHandler serverHandler = new AsyncTimeServerHandler(9000);
        Thread thread = new Thread(serverHandler);
        thread.start();
    }

    static class AsyncTimeServerHandler implements Runnable {

        private int port;
        private CountDownLatch countDownLatch;
        private AsynchronousServerSocketChannel serverSocketChannel;

        public AsyncTimeServerHandler(int port) {
            this.port = port;
            try {
                this.serverSocketChannel = AsynchronousServerSocketChannel.open();
                serverSocketChannel.bind(new InetSocketAddress(this.port));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            countDownLatch = new CountDownLatch(1);
            doAccept();
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        private void doAccept() {
            serverSocketChannel.accept(this, new CompletionHandler<AsynchronousSocketChannel, AsyncTimeServerHandler>() {
                @Override
                public void completed(AsynchronousSocketChannel result, AsyncTimeServerHandler attachment) {
                    attachment.serverSocketChannel.accept(attachment, this);
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    result.read(buffer, buffer, new ReadCompletionHandler(result));
                }

                @Override
                public void failed(Throwable exc, AsyncTimeServerHandler attachment) {
                    attachment.countDownLatch.countDown();
                }
            });
        }
    }

    static class ReadCompletionHandler implements CompletionHandler<Integer, ByteBuffer> {

        private AsynchronousSocketChannel channel;

        public ReadCompletionHandler(AsynchronousSocketChannel channel) {
            if (this.channel == null) {
                this.channel = channel;
            }
        }

        @Override
        public void completed(Integer result, ByteBuffer attachment) {
            attachment.flip();
            byte[] body = new byte[attachment.remaining()];
            attachment.get(body);
            try {
                String req = new String(body, "UTF-8");
                System.out.println("received: "+ req);
                doWrite(new Date(System.currentTimeMillis()).toString());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

        }

        private void doWrite(String date) {
            if (date != null && date.trim().length() > 0) {
                byte[] bytes = date.getBytes();
                ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
                buffer.put(bytes);
                buffer.flip();

                channel.write(buffer, buffer, new CompletionHandler<Integer, ByteBuffer>() {
                    @Override
                    public void completed(Integer result, ByteBuffer attachment) {
                        if (attachment.hasRemaining()) {
                            channel.write(attachment, attachment, this);
                        }
                    }

                    @Override
                    public void failed(Throwable exc, ByteBuffer attachment) {
                        try {
                            channel.close();
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
                this.channel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
