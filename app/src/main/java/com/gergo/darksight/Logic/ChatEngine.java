package com.gergo.darksight.Logic;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.gergo.darksight.Audio.AudioMaker;
import com.gergo.darksight.Encryption.Decryptor;
import com.gergo.darksight.Encryption.Encryptor;
import com.gergo.darksight.Networking.SSLClient;
import com.gergo.darksight.Networking.SSLServer;
import com.gergo.darksight.R;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;

public class ChatEngine extends BaseAdapter {

    private static ChatEngine chatEngine = null;
    private MessageFactory messageFactory = null;
    private Context context = null;
    private List<Message> messageList = new ArrayList<Message>();
    private SSLClient sslClient;
    private SSLServer sslServer;
    private AudioMaker audioMaker;
    private Encryptor encryptor;
    private Decryptor decryptor;

    public ChatEngine() {
        messageFactory = MessageFactory.getMessageFactory();
        audioMaker = AudioMaker.getAudioMaker();
        encryptor = Encryptor.getEncryptor();
        decryptor = Decryptor.getDecryptor();
    }

    public void disconnect() {
        if (Common.isClientMode) {
            sslClient.disconnect();
        } else {
            sslServer.disconnect();
        }
        clearMesseageList();
        reloadCommon();
    }

    public void reloadCommon() {
        Common.RECIEVE_KEYS = true;
        Common.SEND_KEYS = true;
        Common.isConnected = false;
        Common.isConsent = false;
        Common.secretConnectionInProgress = false;
        Common.isClientMode = false;
        Common.isAdvAESRec = true;
        Common.isAdvRSARec = true;
    }

    public void clearMesseageList() {
        messageList.clear();
    }

    public void sendMessage(String message) {

        JSONObject msgJson = messageFactory.createMesseage(Common.USER_NAME, message, Common.ADVANCED_ENCRYPTION);
        Message msg = new Message(msgJson, true);
        messageList.add(msg);
        String toBeSent = null;
        if (Common.ADVANCED_ENCRYPTION) {
            toBeSent = encryptor.advancedEncrypt(msg);
        } else {
            toBeSent = encryptor.encrypt(msg);

        }
        this.notifyDataSetChanged();
        if (Common.isClientMode) {
            sslClient.sendMesseage(toBeSent);
        } else {
            sslServer.sendMesseage(toBeSent);
        }
    }

    public void reciveMessage(String msgRaw) {
        String messeageDataDecrypted = null;
        if (Common.SOUND) {
            audioMaker.ping();
        }
        if (Common.ADVANCED_ENCRYPTION) {
            messeageDataDecrypted = decryptor.advancedDecrypt(msgRaw);
        } else {
            messeageDataDecrypted = decryptor.decrypt(msgRaw);
        }
        JSONObject msgJson = messageFactory.convertMesseage(messageFactory.createJson(messeageDataDecrypted));
        Message msg = new Message(msgJson, false);
        if (!Common.isConsent) {
            if (msg.getMessageString() == Common.connectionCode) {
                Common.isConsent = true;
            }
        } else {
            messageList.add(msg);

            notifyDataSetChanged();
        }
    }

    public static ChatEngine getChatEngine() {
        if (chatEngine == null) {
            chatEngine = new ChatEngine();
        }
        return chatEngine;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return messageList.size();
    }

    @Override
    public Object getItem(int i) {
        return messageList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        MessageViewHolder messageHolder = new MessageViewHolder();
        LayoutInflater messageInfalter = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        Message message = messageList.get(i);
        if (message.isLocalMessage()) {
            view = messageInfalter.inflate(R.layout.local_message, null);
            messageHolder.messageBody = (TextView) view.findViewById(R.id.message_text);
            view.setTag(messageHolder);
            messageHolder.messageBody.setText(message.getMessageString());
        } else {
            view = messageInfalter.inflate(R.layout.remote_message, null);
            messageHolder.avatar = (View) view.findViewById(R.id.avatar);
            messageHolder.userName = (TextView) view.findViewById(R.id.user_name);
            messageHolder.messageBody = (TextView) view.findViewById(R.id.message_text);
            view.setTag(messageHolder);
            messageHolder.userName.setText(message.getUsername());
            messageHolder.messageBody.setText(message.getMessageString());
        }
        return view;
    }

    public void setSslClient(SSLClient sslClient) {
        this.sslClient = sslClient;
    }

    public void setSslServer(SSLServer sslServer) {
        this.sslServer = sslServer;
    }

    public void setSSLSocket() {
        if (Common.isClientMode) {

        } else {
            Log.e("TAG", "ssl server:" + sslServer.toString());
            sslServer.setSocket();
        }
    }
}

class MessageViewHolder {
    public View avatar;
    public TextView userName;
    public TextView messageBody;
}
