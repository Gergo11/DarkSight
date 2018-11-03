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

    public String getMessageString() throws JSONException {
        return messageData.getString("message");
    }

    public String getUsername () throws JSONException {
        return messageData.getString("userName");
    }

    public JSONObject getMessageData() {
        return messageData;
    }
}
