package com.gergo.darksight.Encryption;

import com.gergo.darksight.Logic.Message;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;

import java.security.Key;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

public class Encryptor {

    private static  Encryptor encryptor = null;
    private LevelOneRSA levelOneRSA = null;
    private LevelTwoElliptic levelTwoElliptic = null;
    private LevelThreeAES levelThreeAES = null;
    private PublicKey rsaKey = null;
    private Key eccKey = null;
    private CipherParameters aesCipherParameters = null;

    public Encryptor() {
        init();
    }

    private void init() {
        levelOneRSA = LevelOneRSA.getLevelOneRSA();
        levelTwoElliptic = LevelTwoElliptic.getLevelTwoElliptic();
        levelThreeAES = LevelThreeAES.getLevelThreeAES();
    }

    public void fillKeys(CipherParameters aes, PublicKey rsa,Key eccKey){
        this.rsaKey = rsa;
        this.eccKey = eccKey;
        this.aesCipherParameters = aes;
    }


    public String advancedEncrypt(Message message) {
        String msg = "Error";
        try {
            msg = levelThreeAES.encrypt(levelTwoElliptic.encrypt(eccKey,levelOneRSA.encrypt(message.toString(),rsaKey)),aesCipherParameters);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return msg;
    }

    public String encrypt(Message message) {
        String msg = "Error";
        try {
            msg = levelOneRSA.encrypt(message.toString(),rsaKey);
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

    public void setEccKey(Key eccKey) {
        this.eccKey = eccKey;
    }

    public void setAesCipherParameters(Key aesCipherParameters) {
        this.aesCipherParameters = (CipherParameters) aesCipherParameters;
    }
}
