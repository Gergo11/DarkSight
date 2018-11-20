package com.gergo.darksight.Logic;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.gergo.darksight.Audio.AudioMaker;
import com.gergo.darksight.Networking.SSLClient;
import com.gergo.darksight.Networking.SSLServer;
import com.gergo.darksight.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ChatEngine extends BaseAdapter{

    public static boolean isAdvancedEnc = false;
    public static String userName = Common.USER_NAME;
    private static ChatEngine chatEngine = null;
    private MessageFactory messageFactory = null;
    private Context context = null;
    private List<Message> messageList = new ArrayList<Message>();
    private SSLClient sslClient;
    private SSLServer sslServer;
    private AudioMaker audioMaker;

    public ChatEngine() {
        messageFactory = MessageFactory.getMessageFactory();
        audioMaker = AudioMaker.getAudioMaker();
    }

    public void sendMessage(String message){
        JSONObject msgJson = messageFactory.createMesseage(userName,message,isAdvancedEnc);
        Message msg = new Message(msgJson,true);
        messageList.add(msg);
        this.notifyDataSetChanged();
        if(sslClient != null){
            sslClient.sendMesseage(msg);
        }
        if(sslServer != null){
           sslServer.sendMesseage(msg);
        }
    }
    public void reciveMessage (String msgRaw){
        if(Common.SOUND){
            audioMaker.ping();
        }
        JSONObject msgJson = messageFactory.convertMesseage(messageFactory.createJson(msgRaw),isAdvancedEnc);
        Message msg = new Message(msgJson,false);
        if(!Common.isConsent) {
            if (msg.getMessageString()== Common.connectionCode){
                Common.isConsent = true;
            }
        }else {
            messageList.add(msg);
            notifyDataSetChanged();
        }
    }

    public static ChatEngine getChatEngine() {
        if(chatEngine == null){
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
        LayoutInflater messageInfalter = (LayoutInflater)context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        Message message = messageList.get(i);
        if(message.isLocalMessage()){
            view = messageInfalter.inflate(R.layout.local_message, null);
            messageHolder.messageBody=(TextView)view.findViewById(R.id.message_text);
            view.setTag(messageHolder);
            messageHolder.messageBody.setText(message.getMessageString());
        }else{
            view = messageInfalter.inflate(R.layout.remote_message,null);
            messageHolder.avatar = (View) view.findViewById(R.id.avatar);
            messageHolder.userName = (TextView) view.findViewById(R.id.user_name);
            messageHolder.messageBody = (TextView) view.findViewById(R.id.message_text);
            view.setTag(messageHolder);
            messageHolder.userName.setText(message.getUsername());
            messageHolder.messageBody.setText(message.getMessageString());
            GradientDrawable avatar = (GradientDrawable) messageHolder.avatar.getBackground();
            avatar.setColor(Color.green(20));
        }
        return view;
    }

    public void setSslClient(SSLClient sslClient) {
        this.sslClient = sslClient;
    }

    public void setSslServer(SSLServer sslServer) {
        this.sslServer = sslServer;
    }

}
class MessageViewHolder{
    public View avatar;
    public TextView userName;
    public TextView messageBody;
}
