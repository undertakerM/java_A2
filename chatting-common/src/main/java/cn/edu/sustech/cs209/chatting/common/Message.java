package cn.edu.sustech.cs209.chatting.common;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class Message implements Serializable {

    private Long timestamp;

    private String sentBy;

    private String sendTo;

    private String data;

    private String[] chats;

    private int status;

    private String dateString;

    public Message(Long timestamp, String sentBy, String sendTo, String data) {
        this.timestamp = timestamp;
        this.sentBy = sentBy;
        this.sendTo = sendTo;
        this.data = data;

    }

    public Message(String[] chats) {
        this.chats = chats;
        status = 0;
    }

    public Message(String data) {
        this.data = data;

    }

    public Message() {

    }

    public void setDateString(String dateString) {
        this.dateString = dateString;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public void setSentBy(String sentBy) {
        this.sentBy = sentBy;
    }

    public void setSendTo(String sendTo) {
        this.sendTo = sendTo;
    }

    public void setData(String data) {
        this.data = data;
    }

    public void setChats(String[] chats) {
        this.chats = chats;
    }

    public void setStatus(int model) {
        this.status = model;
    }

    public String getTimestampStr() {
        Timestamp ts = new Timestamp(timestamp);
        DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        return sdf.format(ts);
    }

    public String getSentBy() {
        return sentBy;
    }

    public String getSendTo() {
        return sendTo;
    }

    public String getData() {
        return data;
    }

    public String[] getChats() {
        return chats;
    }

    public int getStatus() {
        return status;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public String getDateString() {
        return dateString;
    }
}
