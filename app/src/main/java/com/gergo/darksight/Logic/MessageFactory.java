package com.gergo.darksight.Logic;

import com.gergo.darksight.Encryption.Decryptor;
import com.gergo.darksight.Encryption.Encryptor;

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
        String encryptedUserName;
        String encryptedMessage;

        if (isAdvancedEnc) {
            encryptedUserName = Encryptor.advancedEncrypt(userName);
            encryptedMessage = Encryptor.advancedEncrypt(msg);
        } else {
            encryptedUserName = Encryptor.encrypt(userName);
            encryptedMessage = Encryptor.encrypt(msg);
        }
        try {
            message.put("userName", encryptedUserName);
            message.put("message", encryptedMessage);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return message;
    }

    public JSONObject convertMesseage(JSONObject msg, boolean isAdvancedEnc) {
        JSONObject responseMsg = new JSONObject();
        if (isAdvancedEnc) {
            try {
                responseMsg.put("userName", Decryptor.advancedDecrypt(msg.getString("userName")));
                responseMsg.put("messeage", Decryptor.advancedDecrypt(msg.getString("message")));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            try {
                responseMsg.put("userName", Decryptor.decrypt(msg.getString("userName")));
                responseMsg.put("messeage", Decryptor.decrypt(msg.getString("message")));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return responseMsg;
    }

    public static MessageFactory getMessageFactory() {
        if (msgFact == null) {
            msgFact = new MessageFactory();
        }
        return msgFact;
    }
    public JSONObject createJson(String messeage){
        JSONObject outputMesseage = new JSONObject();
        try {
            new JSONObject(messeage);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return outputMesseage;
    }
}
