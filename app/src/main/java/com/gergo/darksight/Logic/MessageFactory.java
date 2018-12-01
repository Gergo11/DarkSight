package com.gergo.darksight.Logic;

import org.json.JSONException;
import org.json.JSONObject;

public class MessageFactory {

    private String msg = "";
    private String userName = "";
    private static MessageFactory msgFact = null;

    public MessageFactory() {
    }

    public JSONObject createMesseage(String userName, String msg, boolean isAdvancedEnc) {
        JSONObject message = new JSONObject();
        try {
            message.put("userName", userName);
            message.put("message", msg);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return message;
    }

    public JSONObject convertMesseage(JSONObject msg) {
        JSONObject responseMsg = new JSONObject();
        try {
            if (msg != null) {
                responseMsg.put("userName", msg.getString("userName"));
                responseMsg.put("message", msg.getString("message"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return responseMsg;
    }

    public static MessageFactory getMessageFactory() {
        if (msgFact == null) {
            msgFact = new MessageFactory();
        }
        return msgFact;
    }

    public JSONObject createJson(String messeage) {
        JSONObject outputMesseage = null;
        if (!messeage.equals("")) {
            try {
                outputMesseage = new JSONObject(messeage);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            outputMesseage = new JSONObject();
        }
        return outputMesseage;
    }
}
