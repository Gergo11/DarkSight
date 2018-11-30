package com.gergo.darksight.Networking;

import android.content.Context;
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
import com.gergo.darksight.R;
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
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
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
    private int serverPort = 1212;
    private ChatEngine chatEngine;
    private ClientThread clientThread;
    private Thread thread;

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
            clientThread = new ClientThread();
            thread = new Thread(clientThread);
            thread.start();
            chatEngine.setSslClient(this);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void sendMesseage(String messeage) {
        clientThread.sendMesseage(messeage);
    }

    class ClientThread implements Runnable {
        private SSLSocket clientSocket;
        private BufferedReader input;
        private String message = "";
        private boolean alreadyRan = false;

        @Override
        public void run() {
            try {
                InetAddress serverAddr = InetAddress.getByName(serverIpAddress);
                SSLSocketFactory socketFactory = (SSLSocketFactory) SSLClient.sslContext.getSocketFactory();
                clientSocket = (SSLSocket) socketFactory.createSocket(serverAddr, serverPort);
                clientSocket.startHandshake();
                this.input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                Log.e("TAG", "input:" + input.toString());
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String tempLine = input.readLine();
                    if (!tempLine.equals("Done")) {
                        message = message + tempLine + "\n";
                        Log.e("TAG2", "message:" + message);
                    } else {
                        if (null == message || "Disconnect".contentEquals(message)) {
                            Thread.interrupted();
                            break;
                        }
                        if (message.equals(Common.connectionCode + "\n")) {
                            Common.isConsent = true;
                            message = "";
                        }
                        if (message.equals(Common.advancedEncryptionCode + "\n")) {
                            Common.ADVANCED_ENCRYPTION = true;
                            setSwitch();
                            message = "";
                        } else {
                            if (Common.ADVANCED_ENCRYPTION && !alreadyRan) {
                                sendMesseage(Common.advancedEncryptionCode);
                                alreadyRan = true;
                            }
                        }
                        if (Common.isConsent) {
                            Log.e("TAG", "consent");
                            if (Common.secretConnectionInProgress) {
                                Log.e("TAG2", "client secretconnection");
                                Encryptor encryptor = Encryptor.getEncryptor();
                                if (Common.ADVANCED_ENCRYPTION) {
                                    if (Common.SEND_KEYS) {
                                        LevelOneRSA levelOneRSA = LevelOneRSA.getLevelOneRSA();
                                        LevelThreeAES levelThreeAES = LevelThreeAES.getLevelThreeAES();
                                        sendMesseage(levelOneRSA.getPublicKey());
                                        sendMesseage(levelThreeAES.getKey());
                                        Common.SEND_KEYS = false;
                                    }
                                    if (Common.RECIEVE_KEYS) {
                                        if (!message.equals("") && Common.isAdvRSARec) {
                                            Log.e("TAG", "Waiting on RSA");
                                            byte[] keyBytes = Base64.decode(message.getBytes(), Base64.DEFAULT);
                                            X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(keyBytes);
                                            message = "";
                                            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                                            PublicKey clientPub = keyFactory.generatePublic(pubKeySpec);
                                            encryptor.setRsaKey(clientPub);
                                            Log.e("TAG2", "----------RSA--------------" + "gotkey " + clientPub.toString());
                                            Common.isAdvRSARec = false;
                                        }
                                        if (!message.equals("") && Common.isAdvAESRec) {
                                            Log.e("TAG", "Waiting on AES");
                                            byte[] keyBytes = Base64.decode(message.getBytes(), Base64.DEFAULT);
                                            SecretKey aesKey = new SecretKeySpec(keyBytes, 0, keyBytes.length, "AES");
                                            Log.e("TAG2", "-------------------------" + "gotkey " + Base64.encodeToString(aesKey.getEncoded(),Base64.DEFAULT));
                                            encryptor.setAesKey(aesKey);
                                            Common.secretConnectionInProgress = false;
                                            Common.RECIEVE_KEYS = false;
                                            Common.isAdvAESRec = false;
                                            message = "";

                                        }
                                    }
                                } else {
                                    Log.e("TAG", "Not advanced");
                                    if (Common.SEND_KEYS) {
                                        LevelOneRSA levelOneRSA = LevelOneRSA.getLevelOneRSA();
                                        sendMesseage(levelOneRSA.getPublicKey());
                                        Common.SEND_KEYS = false;
                                        Log.e("TAG", "" + Common.SEND_KEYS + "<<<<Send key ");
                                    }
                                    Log.e("TAG", "message ");
                                    if (Common.RECIEVE_KEYS && message != null) {
                                        Log.e("TAG", "message " + input.toString());
                                        if (!message.equals("")) {
                                            Log.e("TAG", "" + message + "<ez a message");
                                            byte[] keyBytes = Base64.decode(message.getBytes(), Base64.DEFAULT);
                                            X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(keyBytes);
                                            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                                            PublicKey clientPub = keyFactory.generatePublic(pubKeySpec);
                                            encryptor.setRsaKey(clientPub);
                                            Log.e("TAG2", "-------------------------" + "gotkey" + clientPub.toString());
                                            Common.RECIEVE_KEYS = false;
                                            Common.secretConnectionInProgress = false;
                                            message = "";
                                        }
                                    }
                                }
                            } else {
                                Handler mainHandler = new Handler(Looper.getMainLooper());
                                if (message != null) {
                                    Runnable myRunnable = new Runnable() {
                                        @Override
                                        public void run() {
                                            chatEngine.reciveMessage(message);
                                            message = "";
                                        }
                                    };
                                    mainHandler.post(myRunnable);
                                }
                            }
                        }
                    }
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (InvalidKeySpecException e) {
                    e.printStackTrace();
                }
            }
        }

        public void sendMesseage(String message) {
            new SendTask(message, this.clientSocket).execute();
        }

        public void disconnect() {
            new SendTask("Disconnect", this.clientSocket).execute();
        }
    }

    public class SendTask extends AsyncTask<Void, Void, Void> {
        private String message;
        private SSLSocket clientSocket;

        public SendTask(String message, SSLSocket clientSocket) {
            this.message = message;
            this.clientSocket = clientSocket;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (null != clientSocket) {
                    PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())));
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

    public void disconnect() {
        sendMesseage("Disconnect");
        if (clientThread != null) {
            clientThread.disconnect();
            clientThread = null;
        }
    }

    private void setSwitch() {
        chatEngine.setSwitch();
    }
}
