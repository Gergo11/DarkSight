package com.gergo.darksight.Logic;

import org.json.JSONException;
import org.json.JSONObject;

public class Message {
    private JSONObject messageData = null;
    private boolean isLocalMessage = false;

    public Message(JSONObject messageData, boolean isLocalMessage) {
        this.messageData = messageData;
        this.isLocalMessage = isLocalMessage;
    }

    public boolean isLocalMessage() {
        return isLocalMessage;
    }

    public String getMessageString()  {
        try {
            return messageData.getString("message");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "Message error!";
    }

    public String getUsername () {
        try {
            return messageData.getString("userName");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "Message error!";
    }

    public JSONObject getMessageData() {
        return messageData;
    }
}
