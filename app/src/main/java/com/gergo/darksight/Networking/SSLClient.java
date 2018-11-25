package com.gergo.darksight.Networking;

import android.content.Context;
import android.os.AsyncTask;

import com.gergo.darksight.Encryption.Encryptor;
import com.gergo.darksight.Encryption.LevelOneRSA;
import com.gergo.darksight.Encryption.LevelThreeAES;
import com.gergo.darksight.Encryption.LevelTwoElliptic;
import com.gergo.darksight.Logic.ChatEngine;
import com.gergo.darksight.Logic.Common;
import com.gergo.darksight.Logic.Message;
import com.gergo.darksight.R;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.params.RSAKeyParameters;
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
import java.security.KeyFactory;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public class SSLClient{
    private static SSLContext sslContext;
    private final String passwd = "asdf1234";
    private final String keyStorePasswd = "asdf1234";
    private String serverIpAddress;
    private SSLSocket clientSocket;
    private int serverPort = 1212;
    private BufferedReader input;
    private ChatEngine chatEngine;
    private ClientThread thread;
    private String messeageToBeSent;


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
            keyManagerFactory.init(keyStore, keyStorePasswd.toCharArray());
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

    public void initializeConnection(String serverIp) {
        this.serverIpAddress = serverIp;
        try {
            thread = new ClientThread();
            thread.start();
            chatEngine.setSslClient(this);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void sendMesseage(String messeage) {
        messeageToBeSent = messeage;
    }




    class ClientThread extends Thread {
        @Override
        public void run() {
            try {
                InetAddress serverAddr = InetAddress.getByName(serverIpAddress);
                SSLSocketFactory socketFactory = (SSLSocketFactory) SSLClient.sslContext.getSocketFactory();
                clientSocket = (SSLSocket) socketFactory.createSocket(serverAddr, serverPort);
                clientSocket.startHandshake();
                input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter outPut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())), true);
                while (clientSocket.isConnected()) {
                    if (Common.secretConnectionInProgress) {
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
                    }
                    String msgRaw = input.readLine();
                    chatEngine.reciveMessage(msgRaw);
                    outPut.println(messeageToBeSent);
                }

            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (InvalidKeySpecException e) {
                e.printStackTrace();
            } catch (NoSuchProviderException e) {
                e.printStackTrace();
            }
        }
    }
}
