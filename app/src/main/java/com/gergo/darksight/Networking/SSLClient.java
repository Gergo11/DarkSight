package com.gergo.darksight.Networking;

import android.content.Context;

import com.gergo.darksight.Logic.ChatEngine;
import com.gergo.darksight.Logic.Message;
import com.gergo.darksight.R;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public class SSLClient {
    private static SSLContext sslContext;
    private final String passwd = "asdf1234";
    private final String keyStorePasswd = "asdf1234";
    private String serverIpAddress;
    private SSLSocket clientSocket;
    private int serverPort = 1212;
    private BufferedReader input;
    private ChatEngine chatEngine;


    public SSLClient(Context context, ChatEngine chatEngine) {
        this.chatEngine = chatEngine;
        try {
            KeyStore trustStore = KeyStore.getInstance("BKS");
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            InputStream trustInput = context.getResources().openRawResource(R.raw.client_trust_store);
            trustStore.load(trustInput, passwd.toCharArray());
            trustManagerFactory.init(trustStore);

            KeyStore keyStore = KeyStore.getInstance("BKS");
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore,keyStorePasswd.toCharArray());
            InputStream keyStoreInput = context.getResources().openRawResource(R.raw.client_key_store);
            keyStore.load(keyStoreInput, keyStorePasswd.toCharArray());

            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        }
    }
    public void initializeConnection (String serverIp){
        this.serverIpAddress = serverIp;
        try{
            ClientThread thread = new ClientThread();
            thread.start();
            chatEngine.setSslClient(this);
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public void sendMesseage(Message messeage){
        try {
            JSONObject msg = messeage.getMessageData();
            String toBeSent = msg.toString();
            PrintWriter outPut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())), true);
            outPut.println(toBeSent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class ClientThread extends Thread {
        @Override
        public void run() {
            try{
                InetAddress serverAddr = InetAddress.getByName(serverIpAddress);
                SSLSocketFactory socketFactory = (SSLSocketFactory) SSLClient.sslContext.getSocketFactory();
                clientSocket = (SSLSocket) socketFactory.createSocket(serverAddr,serverPort);
                clientSocket.startHandshake();
                input = new BufferedReader(new InputStreamReader( clientSocket.getInputStream()));
                while(clientSocket.isConnected()){
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
