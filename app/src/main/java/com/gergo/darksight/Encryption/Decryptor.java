package com.gergo.darksight.Encryption;

import android.util.Log;

import org.bouncycastle.crypto.InvalidCipherTextException;

public class Decryptor {

    private static  Decryptor decryptor = null;
    private LevelOneRSA levelOneRSA = null;
    private LevelThreeAES levelThreeAES = null;

    public Decryptor() {
        init();
    }

    private void init() {
        levelOneRSA = LevelOneRSA.getLevelOneRSA();
        levelThreeAES = LevelThreeAES.getLevelThreeAES();
    }

    public String advancedDecrypt(String message) {
        String decryptedString = "Error";
        try {
            Log.e("TAG","AES "+levelThreeAES.decrypt(message));
            decryptedString = levelOneRSA.decrypt(levelThreeAES.decrypt(message));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return decryptedString;
    }

    public String decrypt(String message) {
        String decryptedString = "Error";
        try {
            decryptedString = levelOneRSA.decrypt(message);
        } catch (InvalidCipherTextException e) {
            e.printStackTrace();
        }

        return decryptedString;
    }
    public static Decryptor getDecryptor(){
        if(decryptor == null){
            decryptor = new Decryptor();
        }
        return decryptor;
    }
}
