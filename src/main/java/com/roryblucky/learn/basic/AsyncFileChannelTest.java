package com.roryblucky.learn.basic;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.Future;

public class AsyncFileChannelTest {

    public static void asyncReadFileChannelWithFuture() throws IOException {
        Path path = Paths.get(AsyncFileChannelTest.class.getClassLoader().getResource("file.txt").getFile());
        AsynchronousFileChannel asyncFileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.READ);

        ByteBuffer buffer = ByteBuffer.allocate(1024);
        long position = 0;
        Future<Integer> operation = asyncFileChannel.read(buffer, position);

        while(!operation.isDone());

        buffer.flip();

        byte[] bytes = new byte[buffer.limit()];
        buffer.get(bytes);
        System.out.println(new String(bytes));
        buffer.clear();
        asyncFileChannel.close();
    }

    public static void asyncReadFileChannelWithHandler() throws IOException {
        Path path = Paths.get(AsyncFileChannelTest.class.getClassLoader().getResource("file.txt").getFile());
        AsynchronousFileChannel asyncFileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.READ);

        ByteBuffer buffer = ByteBuffer.allocate(1024);
        long position = 0;

        asyncFileChannel.read(buffer, position, "哈哈哈", new CompletionHandler<Integer, String>() {
            @Override
            public void completed(Integer result, String attachment) {
                byte[] bytes = new byte[result];
                buffer.flip();
                buffer.get(bytes);
                System.out.println(new String(bytes));
                buffer.clear();
            }

            @Override
            public void failed(Throwable exc, String attachment) {
                System.out.println(exc.getMessage());
            }
        });
        //给主线程留时间显示Console
        delay();
    }


    public static void asyncWriteFileChannelWithFuture() throws IOException {
        Path path = Paths.get(AsyncFileChannelTest.class.getClassLoader().getResource("toFile.txt").getFile());
        AsynchronousFileChannel asyncFileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.WRITE);
        byte[] data = "test data".getBytes();
//        ByteBuffer byteBuffer = ByteBuffer.allocate(data.length);
//        byteBuffer.put(data);
        ByteBuffer byteBuffer = ByteBuffer.wrap(data);
        byteBuffer.flip();
        Future<Integer> future = asyncFileChannel.write(byteBuffer, 0);
        byteBuffer.clear();
        while(!future.isDone());

        System.out.println("Write done");
    }

    public static void asyncWriteFileChannelWithHandler() throws IOException {
        Path path = Paths.get(AsyncFileChannelTest.class.getClassLoader().getResource("toFile.txt").getFile());
        AsynchronousFileChannel asyncFileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.WRITE);
        byte[] data = "test data1".getBytes();
        ByteBuffer byteBuffer = ByteBuffer.wrap(data);
        asyncFileChannel.write(byteBuffer, 0, null, new CompletionHandler<Integer, Object>() {
            @Override
            public void completed(Integer result, Object attachment) {
                System.out.println("Write done, Totally write " + result + "data.");
            }

            @Override
            public void failed(Throwable exc, Object attachment) {

            }
        });
        //给主线程留时间显示Console
        delay();
    }

    private static void delay() {
        int cTime = 0;
        while(cTime < 5) {
            try {
                Thread.sleep(500);
                ++cTime;
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        asyncReadFileChannelWithFuture();
        asyncReadFileChannelWithHandler();

        asyncWriteFileChannelWithFuture();

        asyncWriteFileChannelWithHandler();
    }
}
