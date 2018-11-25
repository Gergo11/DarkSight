package com.gergo.darksight.Networking;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.gergo.darksight.Encryption.Encryptor;
import com.gergo.darksight.Encryption.LevelOneRSA;
import com.gergo.darksight.Encryption.LevelThreeAES;
import com.gergo.darksight.Encryption.LevelTwoElliptic;
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
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

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
    private String messeageToBeSent;

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
            KeyStore keyStore = KeyStore.getInstance("BKS");
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore,keyStorePasswd.toCharArray());
            InputStream keyStoreInput = context.getResources().openRawResource(R.raw.server_key_store);
            keyStore.load(keyStoreInput, keyStorePasswd.toCharArray());
            sslContext = SSLContext.getInstance("TLS");
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

    public void sendMesseage(String messeage) {
        messeageToBeSent = messeage;
    }
    class ServerThread extends Thread {
        @Override
        public void run() {
            try{
                SSLServerSocketFactory socketFactory = (SSLServerSocketFactory) SSLServer.sslContext.getServerSocketFactory();
                serverSocket = (SSLServerSocket) socketFactory.createServerSocket(serverPort);
                accepted = (SSLSocket) serverSocket.accept();
                input = new BufferedReader(new InputStreamReader( accepted.getInputStream()));
                PrintWriter outPut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(accepted.getOutputStream())), true);
                while(accepted.isConnected()){
                    if(!Common.isConnected){
                        Common.isConnected = true;
                        if(Common.inBackGround){
                           //sendNotification
                        }
                        else{
                            mainActivity.showDialog();
                        }
                    }else {
                        if (Common.secretConnectionInProgress) {
                            Encryptor encryptor = Encryptor.getEncryptor();
                            if (Common.ADVANCED_ENCRYPTION) {
                                for (int i = 0; i <= 2; i++) {
                                    switch (i) {
                                        case 0:
                                            X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(input.readLine().getBytes());
                                            KeyFactory keyFactory = KeyFactory.getInstance("RSA", "BC");
                                            encryptor.setRsaKey(keyFactory.generatePublic(pubKeySpec));
                                        case 1:
                                            X509EncodedKeySpec pubKeySpec1 = new X509EncodedKeySpec(input.readLine().getBytes());
                                            KeyFactory keyFactory1 = KeyFactory.getInstance("EC", "BC");
                                            encryptor.setEccKey(keyFactory1.generatePublic(pubKeySpec1));
                                        case 2:
                                            X509EncodedKeySpec pubKeySpec2 = new X509EncodedKeySpec(input.readLine().getBytes());
                                            KeyFactory keyFactory2 = KeyFactory.getInstance("AES");
                                            encryptor.setAesCipherParameters(keyFactory2.generatePublic(pubKeySpec2));
                                    }
                                }
                            } else {
                                X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(input.readLine().getBytes());
                                KeyFactory keyFactory = KeyFactory.getInstance("RSA", "BC");
                                encryptor.setRsaKey(keyFactory.generatePublic(pubKeySpec));
                            }
                            Common.secretConnectionInProgress = false;
                            if(Common.ADVANCED_ENCRYPTION) {
                                LevelOneRSA levelOneRSA = LevelOneRSA.getLevelOneRSA();
                                LevelTwoElliptic levelTwoElliptic = LevelTwoElliptic.getLevelTwoElliptic();
                                LevelThreeAES levelThreeAES = LevelThreeAES.getLevelThreeAES();
                                outPut.println(levelOneRSA.getPublicKey().toString());
                                outPut.println(levelTwoElliptic.getPublicKey().toString());
                                outPut.println(levelThreeAES.getIvAndKey().toString());
                            }
                            else {
                                LevelOneRSA levelOneRSA = LevelOneRSA.getLevelOneRSA();
                                outPut.println(levelOneRSA.getPublicKey().toString());
                            }
                            outPut.close();
                        }
                        String msgRaw = input.readLine();
                        chatEngine.reciveMessage(msgRaw);

                        outPut.println(messeageToBeSent);
                    }
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (NoSuchProviderException e) {
                e.printStackTrace();
            } catch (InvalidKeySpecException e) {
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
