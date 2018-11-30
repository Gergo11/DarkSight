package com.gergo.darksight.Encryption;

import android.util.Log;

import com.gergo.darksight.Logic.Message;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;

import java.security.Key;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.SecretKey;

public class Encryptor {

    private static  Encryptor encryptor = null;
    private LevelOneRSA levelOneRSA = null;
    private LevelThreeAES levelThreeAES = null;
    private PublicKey rsaKey = null;
    private PublicKey eccKey = null;
    private SecretKey aesKey = null;

    public Encryptor() {
        init();
    }

    private void init() {
        levelOneRSA = LevelOneRSA.getLevelOneRSA();
        levelThreeAES = LevelThreeAES.getLevelThreeAES();
    }

    public String advancedEncrypt(Message message) {
        String msg = "Error";
        try {
            Log.e("TAG","RSA " + levelOneRSA.encrypt(message.getMessageData().toString(),rsaKey));
            Log.e("TAG","RSA + EC " +levelOneRSA.encrypt(message.getMessageData().toString(),rsaKey));
            msg = levelThreeAES.encrypt(levelOneRSA.encrypt(message.getMessageData().toString(),rsaKey),aesKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e("TAG","encrapted messeage "+msg);
        return msg;
    }

    public String encrypt(Message message) {
        String msg = "Error";
        try {
            msg = levelOneRSA.encrypt(message.getMessageData().toString(),rsaKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return msg;
    }


    public static Encryptor getEncryptor(){
        if(encryptor == null){
            encryptor = new Encryptor();
        }
        return encryptor;
    }


    public void setRsaKey(PublicKey rsaKey) {
        this.rsaKey = rsaKey;
    }

    public void setEccKey(PublicKey eccKey) {
        this.eccKey = eccKey;
    }

    public void setAesKey(SecretKey key) {
        this.aesKey =  key;
    }
}
