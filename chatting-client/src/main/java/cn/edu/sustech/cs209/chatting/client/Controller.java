package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.Message;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.util.*;

import static javafx.scene.control.Alert.AlertType.INFORMATION;
import static javafx.scene.control.Alert.AlertType.WARNING;

public class Controller implements Initializable {

  Map<String, List<String>> messageListRoom = new HashMap<>();

  Map<String, ChatWindowsController> chatCtlMap = new HashMap<>();

  String currentChat;

  String currentSendTo;

  int currentScrollTo = 0;

  @FXML
  ListView<String> chatContentList; // display
//    ListView<String> chatContentList; // display

  @FXML
  ListView<String> chatList;

  @FXML
  Label msgAlert;

  @FXML
  ListView<String> onlineUser;

  @FXML
  TextArea inputArea;

  @FXML
  Label currentOnlineCnt;

  @FXML
  Label currentUsername;

  public String username;

  ChatClient client;

  public void updateUser(String[] userList) {

    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        ObservableList<String> strList = FXCollections.observableArrayList();
        strList.addAll(userList);
        onlineUser.setItems(strList);
        currentOnlineCnt.setText("Online: " + (strList.size() + 1) + "");
      }
    });
  }

  public void updateMessageList(String data) {
    try {
      Platform.runLater(new Runnable() {
        @Override
        public void run() {
          msgAlert.setText("MSG: you have new msg! ");
          String[] roomMess = data.split("####");
          String chatWith;
          if (roomMess[0].startsWith("(")) {
            String[] names = roomMess[0].substring(1, roomMess[0].length() - 1).split(", ");
            chatWith = names[0];
            if (names[0].equals(username)) {
              chatWith = names[1];
            }
            messageListRoom.put(chatWith, Arrays.asList(roomMess[1].split("\n")));
          } else {
            chatWith = roomMess[0];
            messageListRoom.put(roomMess[0], Arrays.asList(roomMess[1].split("\n")));
          }
          System.out.println("Chat with. " + chatWith);
          if (currentChat == null || currentChat.isEmpty()) {
            currentChat = chatWith;
            if (currentChat.startsWith("[")) {
              currentSendTo = currentChat;
            } else {
              if (currentChat.compareTo(username) > 0) {
                currentSendTo = String.format("(%s, %s)", username, currentChat);
              } else {
                currentSendTo = String.format("(%s, %s)", currentChat, username);
              }
            }
            chatList.getItems().add(chatWith);
            chatList.getSelectionModel().select(currentScrollTo);
            createStage(chatWith);
            chatCtlMap.get(chatWith).updateChatWithContent(chatContentList);
          } else if (!chatList.getItems().contains(chatWith)) {
            chatList.getItems().add(chatWith);
            chatList.getSelectionModel().select(currentScrollTo);
            createStage(chatWith);
            ObservableList<String> strList = FXCollections.observableArrayList();
            strList.addAll(messageListRoom.get(chatWith));
            chatContentList.setItems(strList);
            chatCtlMap.get(chatWith).updateChatWithContent(chatContentList);
          }

          if (chatWith.equals(currentChat)) {
            try {
              ObservableList<String> strList = FXCollections.observableArrayList();
              strList.addAll(messageListRoom.get(chatWith));
              chatContentList.setItems(strList);
              chatCtlMap.get(chatWith).updateChatWithContent(chatContentList);

            } catch (Exception e) {
              System.out.println("parse message failed.");
            }
          }


        }
      });
    } catch (Exception e) {
//            e.printStackTrace();
    }


  }

  private List<Message> dataToMessages(List<String> data) {
//        %s %s said: %s
    List<Message> messageList = new ArrayList<>();
    for (String datum : data) {
      String[] das = datum.split(" said: ");
      Message message = new Message();
      message.setData(das[1]);
      // add try
      String[] usrTime = das[0].split(" ");
      message.setDateString(usrTime[0] + " " + usrTime[1]);
      message.setSentBy(usrTime[2]);
      messageList.add(message);
    }
    return messageList;
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {

    Dialog<String> dialog = new TextInputDialog();
    dialog.setTitle("Login");
    dialog.setHeaderText(null);
    dialog.setContentText("Username:");

    Optional<String> input = dialog.showAndWait();
    if (input.isPresent() && !input.get().isEmpty()) {
      username = input.get();
      try {
        while (true) {

          client = new ChatClient(username, this);
          if (!client.init()) {
            dialog.close();
            Alert alert = new Alert(INFORMATION, "Username existed, change another one.");
            alert.showAndWait();
            input = dialog.showAndWait();
            username = input.get();
            continue;
          }
          currentUsername.setText("Current User: " + username);
          inputArea.setWrapText(true);
          currentOnlineCnt.setText("OK");
          break;
        }

      } catch (Exception e) {
        new Alert(WARNING, "Error Occurs").showAndWait();
//                e.printStackTrace();
        currentOnlineCnt.setText("LOST");
      }
    } else {
      System.out.println("Invalid username " + input + ", exiting");
      Platform.exit();
    }
//        chatContentList.setCellFactory(new MessageCellFactory());
    chatList.setOnMouseClicked(new EventHandler<MouseEvent>() {
      @Override
      public void handle(MouseEvent mouseEvent) {
        System.out.println(chatList.getSelectionModel().getSelectedItem());

        if (chatList.getSelectionModel().getSelectedItem() == null) {
          return;
        }
        currentChat = chatList.getSelectionModel().getSelectedItem();
        if (currentChat.startsWith("[")) {
          currentSendTo = currentChat;
        } else {
          if (currentChat.compareTo(username) > 0) {
            currentSendTo = String.format("(%s, %s)", username, currentChat);
          } else {
            currentSendTo = String.format("(%s, %s)", currentChat, username);
          }
        }

        ObservableList<String> strList = FXCollections.observableArrayList();
        strList.addAll(
          messageListRoom.get(chatList.getSelectionModel().getSelectedItem()));
        chatContentList.setItems(strList);
      }
    });
//  chatContentList.setCellFactory(new MessageCellFactory(username));
  }


  @FXML
  public void createPrivateChat() throws IOException {
    if (client != null) {

      Stage stage = new Stage();
      ComboBox<String> userSel = new ComboBox<>();
      userSel.getItems().addAll(client.chats);
      Button okBtn = new Button("OK");
      okBtn.setOnAction(e -> {
        String s = userSel.getSelectionModel().getSelectedItem();
        currentChat = s;
        if (s == null) {

          stage.close();

          return;
        }
        stage.close();
        ObservableList<String> items = chatList.getItems();
        boolean isAdd = false;
        for (int i = 0; i < items.size(); i++) {
          String item = items.get(i);
          if (item.equals(s)) {
            chatList.getSelectionModel().select(i);
            chatList.scrollTo(i);
            currentScrollTo = i;
            isAdd = true;
            System.out.println(i);
            chatCtlMap.get(s).getStage().show();
            chatCtlMap.get(s).getStage().toFront();
            break;
          }
        }
        if (!isAdd) {
          chatList.getItems().add(s);
          chatList.getSelectionModel().select(chatList.getItems().size() - 1);
          chatList.scrollTo(chatList.getItems().size() - 1);
          currentScrollTo = chatList.getItems().size() - 1;
          createStage(s);
        }

        if (s.compareTo(username) > 0) {
          currentSendTo = String.format("(%s, %s)", username, s);
        } else {
          currentSendTo = String.format("(%s, %s)", s, username);
        }
        if (!messageListRoom.containsKey(currentChat)) {
          messageListRoom.put(currentChat, new ArrayList<>());
        }
        ObservableList<String> strList = FXCollections.observableArrayList();
        strList.addAll(messageListRoom.get(currentChat));
        chatContentList.setItems(strList);


      });

      HBox box = new HBox(10);
      box.setAlignment(Pos.CENTER);
      box.setPadding(new Insets(20, 20, 20, 20));
      box.getChildren().addAll(userSel, okBtn);
      stage.setScene(new Scene(box));
      stage.setResizable(false);
      stage.showAndWait();
    } else {
      new Alert(WARNING, "Disconnected...");
    }
  }

  private void createStage(String s) {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("chatwindows2.fxml"));
    ChatWindowsController chatCtl = new ChatWindowsController();
    chatCtl.setChatWith(s);
    loader.setController(chatCtl);
    Parent root = null;
    try {
//                        ChatWindowsController chatCtl =;
      chatCtl.setController(this);
      root = loader.load();
      Stage stage1 = new Stage();
      stage1.setTitle(s);
      stage1.setResizable(false);
      Platform.runLater(new Runnable() {
        @Override
        public void run() {
          chatCtl.chatWithTitle.setText("I'm " + username + ", chat with " + s + ".  ");
        }
      });

      stage1.setScene(new Scene(root));
      stage1.show();
      chatCtl.setStage(stage1);
      chatCtl.setStage(stage1);
      chatCtl.updateChatWithContent(chatContentList);
      chatCtlMap.put(s, chatCtl);

    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  public void disconnected() {
    String s = "Server gets some WRONGS! Please exit and try again";
    client = null;
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        new Alert(WARNING, "Error Occurs").showAndWait();
        currentOnlineCnt.setText("LOST");
//                chatList.setItems(null);
      }
    });
  }

  @FXML
  public void createGroupChat() {
    if (client != null) {
      Stage stage = new Stage();
      List<String> list = new ArrayList<>();
      ListView<CheckBox> listView = new ListView<>();
      ObservableList<CheckBox> checkBoxes = FXCollections.observableArrayList();
      for (int i = 0; i < client.chats.length; i++) {
        CheckBox checkBox = new CheckBox(client.chats[i]);
        checkBoxes.add(checkBox);
      }
      listView.setItems(checkBoxes);
      Button okBtn = new Button("OK");
      okBtn.setOnAction(actionEvent -> {
        listView.getItems().forEach(checkBox -> {
          if (checkBox.isSelected()) {
            list.add(checkBox.getText());
          }
        });
        if (list.size() != 0) {
          list.add(username);
          Collections.sort(list);
          currentSendTo = list.toString();
          currentChat = list.toString();
          ObservableList<String> items = chatList.getItems();
          boolean isAdd = false;
          for (int i = 0; i < items.size(); i++) {
            String item = items.get(i);
            if (item.equals(list.toString())) {
              chatList.getSelectionModel().select(i);
              chatList.scrollTo(i);
              isAdd = true;
              System.out.println(i);
              chatCtlMap.get(currentChat).getStage().show();
              chatCtlMap.get(currentChat).getStage().toFront();

            }
          }
          if (!isAdd) {
            chatList.getItems().add(list.toString());
            chatList.getSelectionModel().select(chatList.getItems().size() - 1);
            chatList.scrollTo(chatList.getItems().size() - 1);

            createStage(list.toString());
          }
          if (!messageListRoom.containsKey(currentChat)) {
            messageListRoom.put(currentChat, new ArrayList<>());
          }
          ObservableList<String> strList = FXCollections.observableArrayList();
          strList.addAll(messageListRoom.get(currentChat));
          chatContentList.setItems(strList);
        }
        stage.close();
      });
      HBox box = new HBox(10);
      box.setAlignment(Pos.CENTER);
      box.setPadding(new Insets(20, 20, 20, 20));
      box.getChildren().addAll(listView, okBtn);
      stage.setScene(new Scene(box));
      stage.setResizable(false);
      stage.showAndWait();
    } else {
      new Alert(WARNING, "Error Occurs").showAndWait();
    }
  }

  public void doSendMessage(ActionEvent actionEvent) {
    doMessSender();

  }

  public void doMessSender() {
    if (!inputArea.getText().isEmpty()) {
      doMessSender(inputArea.getText());
    }
  }

  public void doMessSender(String text) {
    if (currentChat == null || currentChat.isEmpty()) {
      new Alert(WARNING, "Please create a chat!!!").showAndWait();
    } else {
      Message message = new Message(System.currentTimeMillis(), username, currentSendTo, text);
      message.setStatus(8);
      try {
        client.sendMessage(message);
      } catch (IOException e) {
        new Alert(WARNING, "Send Failed...Try...").showAndWait();
      }

    }
    inputArea.clear();
  }

  public void doMessSender(String cw, String text) {
    currentChat = cw;
    if (currentChat.startsWith("[")) {
      currentSendTo = currentChat;
    } else {
      if (currentChat.compareTo(username) > 0) {
        currentSendTo = String.format("(%s, %s)", username, currentChat);
      } else {
        currentSendTo = String.format("(%s, %s)", currentChat, username);
      }
    }

    Message message = new Message(System.currentTimeMillis(), username, currentSendTo, text);
    message.setStatus(8);
    try {
      client.sendMessage(message);
    } catch (IOException e) {
      new Alert(WARNING, "Send Failed...Try...").showAndWait();


    }
    inputArea.clear();
  }

  @FXML
  public void onKeyPressedTextArea(KeyEvent keyEvent) {
//        if (keyEvent.getCode() == KeyCode.ENTER) {
//            doMessSender();
//        }
  }

  /**
   * You may change the cell factory if you changed the design of {@code Message} model.
   * Hint: you may also define a cell factory for the chats displayed in the left panel, or simply override the toString method.
   */
  static class MessageCellFactory implements Callback<ListView<Message>, ListCell<Message>> {
    private String currentUser = "";

    public MessageCellFactory(String currentUser) {
      this.currentUser = currentUser;
    }

    @Override
    public ListCell<Message> call(ListView<Message> param) {
      return new ListCell<Message>() {

        @Override
        public void updateItem(Message msg, boolean empty) {
          super.updateItem(msg, empty);
          if (empty || Objects.isNull(msg)) {
            return;
          }

          HBox wrapper = new HBox();
          Label timeLabel = new Label(msg.getDateString());
          Label nameLabel = new Label(msg.getSentBy() + " said: ");
          Label msgLabel = new Label(msg.getData());

          timeLabel.setPrefSize(120, 20);
          timeLabel.setWrapText(true);
          timeLabel.setStyle("-fx-border-color: black; -fx-border-width: 1px;");

          nameLabel.setPrefSize(100, 20);
          nameLabel.setWrapText(true);
          nameLabel.setStyle("-fx-border-color: black; -fx-border-width: 1px;");

          if (currentUser.equals(msg.getSentBy())) {
            wrapper.setAlignment(Pos.TOP_RIGHT);
            wrapper.getChildren().addAll(timeLabel, nameLabel, msgLabel);
            msgLabel.setPadding(new Insets(0, 0, 0, 10));
          } else {
            wrapper.setAlignment(Pos.TOP_LEFT);
            wrapper.getChildren().addAll(timeLabel, nameLabel, msgLabel);
            msgLabel.setPadding(new Insets(0, 0, 0, 10));
          }

          setGraphic(wrapper);
        }
      };
    }
  }
}
