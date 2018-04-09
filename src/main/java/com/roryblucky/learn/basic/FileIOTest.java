package com.roryblucky.learn.basic;

import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FileIOTest {

    public static void normalFileIO() throws Exception {
        FileInputStream inputStream = new FileInputStream(new File(
                FileIOTest.class.getClassLoader().getResource("file.txt").getFile()));
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            System.out.println(new String(buffer, 0, len));
        }

        inputStream.close();
    }

    public static void fileNIO() throws Exception {
        RandomAccessFile randomAccessFile = new RandomAccessFile(
                FileIOTest.class.getClassLoader().getResource("file.txt").getFile(), "rw");
        FileChannel fileChannel = randomAccessFile.getChannel();
        ByteBuffer bf = ByteBuffer.allocate(1024);

        while (fileChannel.read(bf) != -1) {//将数据读入buffer中
            bf.flip();//反转buffer，从buffer中读取数据
            byte[] bytes = new byte[bf.remaining()];
            bf.get(bytes);
            System.out.println(new String(bytes));
            bf.clear();
        }
    }

    public static void main(String[] args) throws Exception {
        normalFileIO();
        fileNIO();
    }
}
