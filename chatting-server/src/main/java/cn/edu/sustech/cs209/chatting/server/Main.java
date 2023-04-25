package cn.edu.sustech.cs209.chatting.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        System.out.println("Starting Chat Server...");
        ServerSocket serverSocket = new ServerSocket(1235);
        System.out.println("Done.");
        while (true) {

            Socket newUser = serverSocket.accept();

            ChatServer serverUser = new ChatServer(newUser);
            if (!serverUser.init()) {
                serverUser = null;
                System.gc();
                continue;
            }
            ChatServer.setChats();
            serverUser.start();
        }
    }
}
