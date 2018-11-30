package com.gergo.darksight.Networking;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;

import com.gergo.darksight.Encryption.Encryptor;
import com.gergo.darksight.Encryption.LevelOneRSA;
import com.gergo.darksight.Encryption.LevelThreeAES;
import com.gergo.darksight.Logic.ChatEngine;
import com.gergo.darksight.Logic.Common;
import com.gergo.darksight.MainActivity;
import com.gergo.darksight.R;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
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
    private ChatEngine chatEngine;
    private SSLSocket accepted;
    private SSLSocket tempClientSocket;
    private Thread thread = null;
    private CommunicationThread commThread;

    public SSLServer(MainActivity mainActivity, ChatEngine chatEngine) {
        this.mainActivity = mainActivity;
        this.context = mainActivity;
        this.chatEngine = chatEngine;
    }

    private void createSSLContext() {
        try {
            KeyStore trustStore = KeyStore.getInstance("BKS");
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            InputStream trustInput = context.getResources().openRawResource(R.raw.server_trust_store);
            trustStore.load(trustInput, trustPasswd.toCharArray());
            trustManagerFactory.init(trustStore);
            KeyStore keyStore = KeyStore.getInstance("BKS");
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, keyStorePasswd.toCharArray());
            InputStream keyStoreInput = context.getResources().openRawResource(R.raw.server_key_store);
            keyStore.load(keyStoreInput, keyStorePasswd.toCharArray());
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void initializeServer() {
        try {
            createSSLContext();
            thread = new Thread(new ServerThread());
            thread.start();
            chatEngine.setSslServer(this);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void sendMesseage(String message) {
        new SendTask(tempClientSocket, message).execute();
    }

    public void disconnect() {
        new SendTask(tempClientSocket, "Disconnect").execute();
        if (commThread != null) {
            commThread = null;
        }
    }

    class ServerThread implements Runnable {
        @Override
        public void run() {
            try {
                SSLServerSocketFactory socketFactory = (SSLServerSocketFactory) SSLServer.sslContext.getServerSocketFactory();
                serverSocket = (SSLServerSocket) socketFactory.createServerSocket(serverPort);
                Log.e("TAG", "Server thread started!");
                if (serverSocket != null) {
                    while (!Thread.currentThread().isInterrupted()) {
                        Common.secretConnectionInProgress = true;
                        accepted = (SSLSocket) serverSocket.accept();
                        commThread = new CommunicationThread(accepted);
                        new Thread(commThread).start();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class CommunicationThread implements Runnable {
        private SSLSocket clientSocket;
        private BufferedReader bufferedReader;
        private String readLine = "";

        CommunicationThread(SSLSocket clientSocket) {
            this.clientSocket = clientSocket;
            tempClientSocket = clientSocket;
            try {
                this.bufferedReader = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    if (!Common.isConnected) {
                        Common.isConnected = true;
                        if (Common.inBackGround) {
                            mainActivity.sendNotification();
                        } else {
                            mainActivity.showDialog();
                        }
                    } else {
                        String tempLine = bufferedReader.readLine();
                        if (!tempLine.equals("Done")) {
                            readLine = readLine + tempLine + "\n";
                            Log.e("TAG2", "message:" + readLine);
                        } else {
                            if (null == readLine || "Disconnect".contentEquals(readLine)) {
                                Thread.interrupted();
                                //readLine = "Client disconnected!";
                                //disconnect
                                break;
                            }
                            if (readLine.equals(Common.advancedEncryptionCode+"\n")) {
                                Common.ADVANCED_ENCRYPTION = true;
                                readLine = "";
                                setSwitch();
                            }
                            if (Common.secretConnectionInProgress) {
                                Encryptor encryptor = Encryptor.getEncryptor();
                                if (Common.ADVANCED_ENCRYPTION) {
                                    if (Common.RECIEVE_KEYS) {
                                        if (!readLine.equals("") && Common.isAdvRSARec) {
                                            byte[] keyBytes = Base64.decode(readLine.getBytes(), Base64.DEFAULT);
                                            X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(keyBytes);
                                            readLine = "";
                                            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                                            PublicKey clientPub = keyFactory.generatePublic(pubKeySpec);
                                            encryptor.setRsaKey(clientPub);
                                            Log.e("TAG2", "----------RSA--------------" + "gotkey" + clientPub.toString());
                                            Common.isAdvRSARec = false;
                                        }
                                        if (!readLine.equals("") && Common.isAdvAESRec) {
                                            byte[] keyBytes  = Base64.decode(readLine.getBytes(),Base64.DEFAULT);
                                            SecretKey aesKey = new SecretKeySpec(keyBytes,0,keyBytes.length,"AES");
                                            Log.e("TAG2", "--------AES-----------------" + "gotkey" + Base64.encodeToString(aesKey.getEncoded(),Base64.DEFAULT));
                                            encryptor.setAesKey(aesKey);
                                            readLine = "";
                                            Common.isAdvAESRec = false;
                                            Common.RECIEVE_KEYS = false;
                                        }
                                    }
                                    if (Common.SEND_KEYS && !Common.RECIEVE_KEYS) {
                                        LevelOneRSA levelOneRSA = LevelOneRSA.getLevelOneRSA();
                                        LevelThreeAES levelThreeAES = LevelThreeAES.getLevelThreeAES();
                                        Log.e("TAG","Sending keys!");
                                        sendMesseage(levelOneRSA.getPublicKey());
                                        Log.e("TAG","Sending keys!(RSA)");
                                        sendMesseage(levelThreeAES.getKey());
                                        Log.e("TAG","Sending keys!(AES)");
                                        Common.SEND_KEYS = false;
                                        Common.secretConnectionInProgress = false;
                                    }
                                } else {
                                    if (Common.RECIEVE_KEYS) {
                                        byte[] keyBytes = Base64.decode(readLine.getBytes(), Base64.DEFAULT);
                                        X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(keyBytes);
                                        readLine = "";
                                        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                                        PublicKey clientPub = keyFactory.generatePublic(pubKeySpec);
                                        encryptor.setRsaKey(clientPub);
                                        Common.RECIEVE_KEYS = false;
                                    }
                                    if (Common.SEND_KEYS) {
                                        LevelOneRSA levelOneRSA = LevelOneRSA.getLevelOneRSA();
                                        sendMesseage(levelOneRSA.getPublicKey());
                                        Common.SEND_KEYS = false;
                                        Common.secretConnectionInProgress = false;
                                    }
                                }
                            } else {
                                Handler mainHandler = new Handler(Looper.getMainLooper());
                                if (readLine != null) {
                                    if (!readLine.equals("")) {
                                        Runnable myRunnable = new Runnable() {
                                            @Override
                                            public void run() {
                                                chatEngine.reciveMessage(readLine);
                                                readLine = "";
                                            }
                                        };
                                        mainHandler.post(myRunnable);
                                    }
                                }
                            }
                        }
                    }
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (InvalidKeySpecException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void setSwitch() {
        chatEngine.setSwitch();
    }

    public class SendTask extends AsyncTask<Void, Void, Void> {
        private SSLSocket socket;
        private String message;

        public SendTask(SSLSocket socket, String message) {
            this.socket = socket;
            this.message = message;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                if (null != socket) {
                    PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));
                    out.println(message);
                    Thread.sleep(50);
                    out.println("Done");
                    out.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

    }

    public void restartServer() {
        try {
            accepted.close();
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (thread.isAlive()) {
            thread.interrupt();
            Common.isConsent = false;
        }
        initializeServer();
    }
}
