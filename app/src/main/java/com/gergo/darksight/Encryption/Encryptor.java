package com.gergo.darksight.Encryption;

import com.gergo.darksight.Logic.Message;
import java.security.PublicKey;
import javax.crypto.SecretKey;

public class Encryptor {

    private static  Encryptor encryptor = null;
    private LevelOneRSA levelOneRSA = null;
    private LevelTwoAES levelTwoAES = null;
    private PublicKey rsaKey = null;
    private SecretKey aesKey = null;

    public Encryptor() {
        init();
    }

    private void init() {
        levelOneRSA = LevelOneRSA.getLevelOneRSA();
        levelTwoAES = LevelTwoAES.getLevelTwoAES();
    }

    public String advancedEncrypt(Message message) {
        String msg = "Error";
        try {
            msg = levelTwoAES.encrypt(levelOneRSA.encrypt(message.getMessageData().toString(),rsaKey),aesKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public void setAesKey(SecretKey key) {
        this.aesKey =  key;
    }
}
