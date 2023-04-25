package cn.edu.sustech.cs209.chatting.server;

import cn.edu.sustech.cs209.chatting.common.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;

public class ChatServer extends Thread {

    static Map<String, ChatServer> chats = new HashMap<>();

    static List<String> user = new ArrayList<>();

    static Map<String, List<Message>> allMessages = new HashMap<>();

    private String username;

    ObjectInputStream msgIn;

    ObjectOutputStream msgOut;

    public ChatServer(Socket socket) throws IOException {
        msgIn = new ObjectInputStream(socket.getInputStream());
        msgOut = new ObjectOutputStream(socket.getOutputStream());
    }

    public boolean init() throws IOException, ClassNotFoundException {
        Message usernameInit = (Message) msgIn.readObject();
        this.username = usernameInit.getData();
        if (ChatServer.user.contains(username)) {
            Message message = new Message("invalid");
            msgOut.writeObject(message);
            return false;
        } else {
            Message message = new Message("OK");
            msgOut.writeObject(message);
            ChatServer.user.add(username);
            ChatServer.chats.put(username, this);
            ChatServer.setChats();
            return true;
        }
    }

    public static void setChats() {
        String[] userSend = ChatServer.user.toArray(new String[]{});
        Message message = new Message(userSend);
        ChatServer.chats.forEach((s, server) -> {
            try {
                server.msgOut.writeObject(message);
            } catch (Exception e) {
//                e.printStackTrace();
            }
        });
    }

    private String generateMessageStr(List<Message> messageList) {
        StringBuilder result = new StringBuilder();
        for (Message message : messageList) {
            result.append(String.format("%s %s said: %s", message.getTimestampStr(), message.getSentBy(), message.getData()));
            result.append("\n");
        }
        return result.toString().trim();
    }

    @Override
    public void run() {
        try {
            while (true) {

                Message message = (Message) msgIn.readObject();
                if (message.getStatus() == 8) {
                    String sendTo = message.getSendTo();
                    if (!allMessages.containsKey(sendTo)) {
                        allMessages.put(sendTo, new ArrayList<>());
                    }
                    allMessages.get(sendTo).add(message);
                    if (sendTo.startsWith("[")) {
                        String[] sendsTo = sendTo.substring(1, sendTo.length() - 1).split(", ");
                        for (String s : sendsTo) {
                            ChatServer send = chats.get(s);
                            if (send != null) {
                                Message messageSend = new Message(String.format("%s####%s", sendTo, generateMessageStr(allMessages.get(sendTo))));
                                messageSend.setStatus(8);
                                send.msgOut.writeObject(messageSend);
                            }
                        }
                    } else if (sendTo.startsWith("(")) {
                        String[] sendsTo = sendTo.substring(1, sendTo.length() - 1).split(", ");
                        for (String s : sendsTo) {
                            ChatServer send = chats.get(s);
                            if (send != null) {
                                Message messageSend = new Message(String.format("%s####%s", sendTo, generateMessageStr(allMessages.get(sendTo))));
                                messageSend.setStatus(8);
                                send.msgOut.writeObject(messageSend);
                            }
                        }
                    }
                }
            }
        } catch (SocketException e) {
            if (e.getMessage().toLowerCase(Locale.ROOT).contains("reset")) {
                ChatServer.user.remove(username);
                ChatServer.chats.remove(username);
                ChatServer.setChats();
            }


        } catch (Exception e) {
            System.out.println("error occurs");
        }
    }

}
