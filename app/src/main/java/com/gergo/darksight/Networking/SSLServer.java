package com.gergo.darksight.Networking;

import android.content.Context;

import com.gergo.darksight.Logic.ChatEngine;
import com.gergo.darksight.Logic.Message;
import com.gergo.darksight.R;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
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
    private Context context;
    private String trustPasswd;
    private String keyStorePasswd;
    private static SSLContext sslContext;
    private SSLServerSocket serverSocket;
    private BufferedReader input;
    private ChatEngine chatEngine;
    private  SSLSocket accepted;

    public SSLServer(Context context,ChatEngine chatEngine) {
        this.context = context;
        this.chatEngine = chatEngine;
    }

    private SSLContext createSSLContext(){
        try{
            KeyStore trustStore = KeyStore.getInstance("BKS");
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            //InputStream trustInput = context.getResources().openRawResource(R.raw.server_trust_store);
            //trustStore.load(trustInput, trustPasswd.toCharArray());
            trustManagerFactory.init(trustStore);

            KeyStore keyStore = KeyStore.getInstance("BKS");
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            //InputStream keyStoreInput = context.getResources().openRawResource(R.raw.server_key_store);
            //keyStore.load(keyStoreInput, keyStorePasswd.toCharArray());

            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

            initializeServer();
            return sslContext;
        } catch (Exception ex){
            ex.printStackTrace();
        }
        return null;
    }

    public void initializeServer (){
        try{
            SSLServer.ServerThread thread = new SSLServer.ServerThread();
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
                    String msgRaw = input.readLine();
                    chatEngine.reciveMessage(msgRaw);
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
