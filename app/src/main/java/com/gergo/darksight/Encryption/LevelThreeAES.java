package com.gergo.darksight.Encryption;

import android.util.Base64;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class LevelThreeAES {

    private static LevelThreeAES levelThreeAES = null;
    private SecretKey key;


    public LevelThreeAES() {
        init();
    }

    private void init() {
        Security.addProvider(new BouncyCastleProvider());
        KeyGenerator keyGen;
        try {
            keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256); //key is 256 bits
            key = keyGen.generateKey();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public String encrypt(String message, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return Base64.encodeToString(cipher.doFinal(message.getBytes()),Base64.DEFAULT);
    }

    public String decrypt(String messeage) throws Exception {
        byte[] bytes = Base64.decode(messeage.getBytes(),Base64.DEFAULT);
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        return new String(cipher.doFinal(bytes));
    }

    public String getKey() {
        return Base64.encodeToString(key.getEncoded(),Base64.DEFAULT);
    }

    public static LevelThreeAES getLevelThreeAES() {
        if (levelThreeAES == null) {
            levelThreeAES = new LevelThreeAES();
        }
        return levelThreeAES;
    }
}
