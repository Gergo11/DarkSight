package com.gergo.darksight.Encryption;

import org.bouncycastle.crypto.InvalidCipherTextException;

public class Decryptor {

    private static  Decryptor decryptor = null;
    private LevelOneRSA levelOneRSA = null;
    private LevelTwoElliptic levelTwoElliptic = null;
    private LevelThreeAES levelThreeAES = null;

    public Decryptor() {
        init();
    }

    private void init() {
        levelOneRSA = LevelOneRSA.getLevelOneRSA();
        levelTwoElliptic = LevelTwoElliptic.getLevelTwoElliptic();
        levelThreeAES = LevelThreeAES.getLevelThreeAES();
    }

    public String advancedDecrypt(String message) {
        String decryptedString = "Error";
        try {
            decryptedString = levelThreeAES.decrypt(levelTwoElliptic.decrypt(levelOneRSA.decrypt(message)));
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
