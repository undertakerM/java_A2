package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.Message;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class ChatWindowsController implements Initializable {
    public Stage stage;
    @FXML
    public Label chatWithTitle;

    Controller controller;

    private String chatWith;

    @FXML
    public ListView<String> chatWithContent;

    @FXML
    public TextArea inputWithArea;

    @FXML
    public void doSendMessage(ActionEvent actionEvent) {
        if (inputWithArea.getText() != null && !inputWithArea.getText().isEmpty()) {
            controller.doMessSender(chatWith, inputWithArea.getText());
            inputWithArea.clear();
        }
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;

    }

    public void updateChatWithContent(ListView<String> from) {

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
//                chatWithContent = from;
                chatWithContent.setItems(from.getItems());
            }
        });

//        System.out.println(chatWithContent.getItems());
    }

    public Controller getController() {
        return controller;
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
//        chatWithContent.setCellFactory(new Controller.MessageCellFactory(controller.username));
    }

    public String getChatWith() {
        return chatWith;
    }

    public void setChatWith(String chatWith) {
        this.chatWith = chatWith;
    }
}
