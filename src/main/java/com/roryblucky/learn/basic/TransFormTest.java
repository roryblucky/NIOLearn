package com.roryblucky.learn.basic;

import java.io.File;
import java.io.FileInputStream;
import java.nio.channels.FileChannel;

public class TransFormTest {

    public static void transform() throws Exception {
        FileInputStream fromInputStream = new FileInputStream(new File(
                FileIOTest.class.getClassLoader().getResource("file.txt").getFile()));
        FileChannel fromChannel = fromInputStream.getChannel();

        FileInputStream toFileInputStream = new FileInputStream(new File(
                FileIOTest.class.getClassLoader().getResource("toFile.txt").getFile()));
        FileChannel toChannel = toFileInputStream.getChannel();

        toChannel.transferFrom(fromChannel, 0, fromChannel.size());

    }

    public static void transTo() throws Exception {
        FileInputStream fromInputStream = new FileInputStream(new File(
                FileIOTest.class.getClassLoader().getResource("file.txt").getFile()));
        FileChannel fromChannel = fromInputStream.getChannel();

        FileInputStream toFileInputStream = new FileInputStream(new File(
                FileIOTest.class.getClassLoader().getResource("toFile.txt").getFile()));
        FileChannel toChannel = toFileInputStream.getChannel();

        fromChannel.transferTo(0, fromChannel.size(), toChannel);
    }
}
