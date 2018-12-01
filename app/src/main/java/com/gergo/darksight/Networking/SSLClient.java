package com.gergo.darksight.Networking;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;

import com.gergo.darksight.Encryption.Encryptor;
import com.gergo.darksight.Encryption.LevelOneRSA;
import com.gergo.darksight.Encryption.LevelTwoAES;
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
                this.clientSocket = (SSLSocket) socketFactory.createSocket(serverAddr, serverPort);
                this.clientSocket.startHandshake();
                this.input = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
                Log.e("TAG", "cliensocket: " + this.clientSocket.toString());
                Log.e("TAG", "input:" + input.toString());
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    if (input != null) {
                        String tempLine = input.readLine();
                        if (tempLine != null) {
                            if (!tempLine.equals("Done")) {
                                message = message + tempLine + "\n";
                                Log.e("TAG", "messeage arrived raw: " + message);
                            } else {
                                if (null == message || "Disconnect\n".contentEquals(message)) {
                                    Log.e("TAG", "interrupted");
                                    message = "";
                                    clientSocket.close();
                                    chatEngine.reloadCommon();
                                    chatEngine.clearMesseageList();
                                    Thread.currentThread().interrupt();
                                    break;
                                }
                                if (message.equals(Common.connectionCode + "\n")) {
                                    Common.isConsent = true;
                                    message = "";
                                }
                                if (message.equals(Common.advancedEncryptionCode + "\n")) {
                                    Common.ADVANCED_ENCRYPTION = true;
                                    message = "";
                                } else {
                                    if (Common.ADVANCED_ENCRYPTION && !alreadyRan) {
                                        sendMesseage(Common.advancedEncryptionCode);
                                        alreadyRan = true;
                                    }
                                }
                                if (Common.isConsent) {
                                    if (Common.secretConnectionInProgress) {
                                        Log.e("TAG", "secretconnection");
                                        Log.e("TAG", "recieving messeage " + message);
                                        Encryptor encryptor = Encryptor.getEncryptor();
                                        if (Common.ADVANCED_ENCRYPTION) {
                                            if (Common.SEND_KEYS) {
                                                Log.e("TAG", "secretconnection sending keys advanced");
                                                LevelOneRSA levelOneRSA = LevelOneRSA.getLevelOneRSA();
                                                LevelTwoAES levelTwoAES = LevelTwoAES.getLevelTwoAES();
                                                sendMesseage(levelOneRSA.getPublicKey());
                                                sendMesseage(levelTwoAES.getKey());
                                                Common.SEND_KEYS = false;
                                            }
                                            if (Common.RECIEVE_KEYS) {
                                                if (!message.equals("") && Common.isAdvRSARec) {
                                                    Log.e("TAG", "secretconnection recieve rsa");
                                                    byte[] keyBytes = Base64.decode(message.getBytes(), Base64.DEFAULT);
                                                    X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(keyBytes);
                                                    message = "";
                                                    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                                                    PublicKey clientPub = keyFactory.generatePublic(pubKeySpec);
                                                    encryptor.setRsaKey(clientPub);
                                                    Common.isAdvRSARec = false;
                                                }
                                                if (!message.equals("") && Common.isAdvAESRec) {
                                                    Log.e("TAG", "secretconnection recieve aes");
                                                    byte[] keyBytes = Base64.decode(message.getBytes(), Base64.DEFAULT);
                                                    SecretKey aesKey = new SecretKeySpec(keyBytes, 0, keyBytes.length, "AES");
                                                    encryptor.setAesKey(aesKey);
                                                    Common.secretConnectionInProgress = false;
                                                    Common.RECIEVE_KEYS = false;
                                                    Common.isAdvAESRec = false;
                                                    message = "";

                                                }
                                            }
                                        } else {
                                            if (Common.SEND_KEYS) {
                                                Log.e("TAG", "secretconnection normal");
                                                LevelOneRSA levelOneRSA = LevelOneRSA.getLevelOneRSA();
                                                sendMesseage(levelOneRSA.getPublicKey());
                                                Common.SEND_KEYS = false;
                                            }
                                            if (Common.RECIEVE_KEYS && message != null) {
                                                if (!message.equals("")) {
                                                    Log.e("TAG", "secretconnection normal");
                                                    byte[] keyBytes = Base64.decode(message.getBytes(), Base64.DEFAULT);
                                                    X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(keyBytes);
                                                    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                                                    PublicKey clientPub = keyFactory.generatePublic(pubKeySpec);
                                                    encryptor.setRsaKey(clientPub);
                                                    Common.RECIEVE_KEYS = false;
                                                    Common.secretConnectionInProgress = false;
                                                    message = "";
                                                }
                                            }
                                        }
                                    } else {
                                        Handler mainHandler = new Handler(Looper.getMainLooper());
                                        if (message != null && !message.equals("")) {
                                            Log.e("TAG", "recieving messeage");
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
            new SendTask(message, clientSocket).execute();
        }

        public void disconnect() {
            new SendTask("Disconnect", clientSocket).execute();
        }

        public void interupt() {
            Thread.currentThread().interrupt();
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
                Log.e("TAG2","Sending + socket: "+ clientSocket.toString());
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
        if (clientThread != null) {
            clientThread.disconnect();
            clientThread.interupt();
            clientThread = null;
        }
    }

}
