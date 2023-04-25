package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChatClient {

  private Receiver receiver;

  private Sender sender;

  ObjectOutputStream write;

  ObjectInputStream read;

  private Socket socket;

  String username;

  String[] chats;

  String messageList;

  private Controller controller;


  public ChatClient(String chat, Controller controller) throws IOException {
    this.socket = new Socket("localhost", 1235);
    this.controller = controller;
    this.username = chat;
  }

  public boolean init() throws IOException, ClassNotFoundException {
    write = new ObjectOutputStream(socket.getOutputStream());
    read = new ObjectInputStream(socket.getInputStream());
    write.writeObject(new Message(username));
    Message message = (Message) read.readObject();
    if (message.getData().equals("invalid")) {
      return false;
    }
    receiver = new Receiver(this);
    sender = new Sender(this);
    receiver.start();
    return true;
  }

  public void sendMessage(Message message) throws IOException {
    sender.send(message);
  }

  public Controller getController() {
    return controller;
  }

  public void disconnected() {
    controller.disconnected();
  }

  public void updateContent() {
    controller.updateMessageList(messageList);
  }

  public static class Receiver extends Thread {

    ChatClient client;


    public Receiver(ChatClient client) {
      this.client = client;
    }

    @Override
    public void run() {
      while (true) {
        Message message = null;
        try {
          message = (Message) client.read.readObject();
          if (message.getStatus() == 8) {
            String data = message.getData();
            client.messageList = data;
            client.updateContent();
          }
          if (message.getStatus() == 0) {
            String[] userList = message.getChats();
            List<String> user = Arrays.asList(userList);
            List<String> stringArrayList = new ArrayList<>(user);
            stringArrayList.remove(client.getController().username);
            userList = stringArrayList.toArray(new String[] {});
            client.chats = userList;
            client.updateUser(userList);
          }
        } catch (SocketException e) {
          client.disconnected();
          //e.printStackTrace();
          break;
        } catch (Exception e) {
          //e.printStackTrace();

        }
      }
    }
  }

  private void updateUser(String[] users) {
    controller.updateUser(users);
  }

  public static class Sender {

    ChatClient client;

    public Sender(ChatClient client) {
      this.client = client;
    }

    public void send(Message message) throws IOException {
      client.write.writeObject(message);
    }
  }
}
