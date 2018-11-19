package com.gergo.darksight.Networking;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.gergo.darksight.Logic.ChatEngine;
import com.gergo.darksight.Logic.Common;
import com.gergo.darksight.Logic.ConnectDialog;
import com.gergo.darksight.Logic.Message;
import com.gergo.darksight.MainActivity;
import com.gergo.darksight.R;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.UnknownHostException;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;


public class SSLServer {
    private int serverPort = 1212;
    private MainActivity mainActivity;
    private Activity context;
    private String trustPasswd = "asdf1234";
    private String keyStorePasswd = "asdf1234";
    private static SSLContext sslContext;
    private SSLServerSocket serverSocket;
    private BufferedReader input;
    private ChatEngine chatEngine;
    private  SSLSocket accepted;
    private  SSLServer.ServerThread thread = null;

    public SSLServer(MainActivity mainActivity,ChatEngine chatEngine) {
        this.mainActivity = mainActivity;
        this.context = mainActivity;
        this.chatEngine = chatEngine;
    }

    private void createSSLContext(){
        try{
            KeyStore trustStore = KeyStore.getInstance("BKS");
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            InputStream trustInput = context.getResources().openRawResource(R.raw.server_trust_store);
            trustStore.load(trustInput, trustPasswd.toCharArray());
            trustManagerFactory.init(trustStore);
           // Log.i("sslcontext",trustStore.toString()+"Kecske---------------------------------------------");
            KeyStore keyStore = KeyStore.getInstance("BKS");
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore,keyStorePasswd.toCharArray());
            InputStream keyStoreInput = context.getResources().openRawResource(R.raw.server_key_store);
            keyStore.load(keyStoreInput, keyStorePasswd.toCharArray());
            sslContext = SSLContext.getInstance("TLS");
            Log.i("sslcontext","Kecske---------------------------------------------");
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public void initializeServer (){
        try{
            createSSLContext();
            thread = new SSLServer.ServerThread();
            thread.start();
            chatEngine.setSslServer(this);
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public void sendMesseage(Message messeage) {
        try {
            JSONObject msg = messeage.getMessageData();
            String toBeSent = msg.toString();
            PrintWriter outPut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(accepted.getOutputStream())), true);
            outPut.println(toBeSent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    class ServerThread extends Thread {
        @Override
        public void run() {
            try{
                SSLServerSocketFactory socketFactory = (SSLServerSocketFactory) SSLServer.sslContext.getServerSocketFactory();
                serverSocket = (SSLServerSocket) socketFactory.createServerSocket(serverPort);
                accepted = (SSLSocket) serverSocket.accept();
                input = new BufferedReader(new InputStreamReader( accepted.getInputStream()));
                while(accepted.isConnected()){
                    if(Common.isConnected == false){
                        Common.isConnected = true;
                        if(Common.inBackGround){
                           //sendNotification
                        }
                        else{
                            mainActivity.showDialog();
                        }
                    }else {
                        String msgRaw = input.readLine();
                        chatEngine.reciveMessage(msgRaw);
                    }
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void restartServer(){
        try {
            accepted.close();
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(thread.isAlive()){
            thread.interrupt();
            Common.isConsent=false;
        }
       initializeServer();
    }

}
