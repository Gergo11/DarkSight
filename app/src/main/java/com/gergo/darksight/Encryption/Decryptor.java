package com.gergo.darksight.Encryption;

public class Decryptor {

    private static  Decryptor decryptor = null;
    private LevelOneRSA levelOneRSA = null;
    private LevelTwoAES levelTwoAES = null;

    public Decryptor() {
        init();
    }

    private void init() {
        levelOneRSA = LevelOneRSA.getLevelOneRSA();
        levelTwoAES = LevelTwoAES.getLevelTwoAES();
    }

    public String advancedDecrypt(String message) {
        String decryptedString = "Error";
        try {
            decryptedString = levelOneRSA.decrypt(levelTwoAES.decrypt(message));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return decryptedString;
    }

    public String decrypt(String message) {
        String decryptedString = "Error";
        decryptedString = levelOneRSA.decrypt(message);
        return decryptedString;
    }
    public static Decryptor getDecryptor(){
        if(decryptor == null){
            decryptor = new Decryptor();
        }
        return decryptor;
    }
}
