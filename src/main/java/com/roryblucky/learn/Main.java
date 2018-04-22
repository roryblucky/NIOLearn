package com.roryblucky.learn;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws IOException {
        System.out.println("Java NIO Learning");
        File file = new File("/Users/rory/Gitrepository/NIOLearn/src/main/resources/aaa");
        file.mkdir();

        Path dir = Paths.get("/Users/rory/Gitrepository/NIOLearn/src/main/resources/bbb");
        Files.createDirectory(dir);

        System.out.println(System.getSecurityManager());
    }

}
