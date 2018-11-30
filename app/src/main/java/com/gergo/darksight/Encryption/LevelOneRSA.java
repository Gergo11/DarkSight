package com.gergo.darksight.Encryption;

import android.util.Base64;
import android.util.Log;

import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.provider.JCERSAPublicKey;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class LevelOneRSA {

    private static LevelOneRSA levelOneRSA =null;
    private KeyPair keyPair = null;

    public LevelOneRSA() {
        init();
    }

    public void init(){
        try {
            keyPair = generateKeys();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public KeyPair generateKeys() throws NoSuchAlgorithmException{
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        return kpg.generateKeyPair();
    }

    public String encrypt(String message, PublicKey publicKey) throws Exception{
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return Base64.encodeToString(cipher.doFinal(message.getBytes()),Base64.DEFAULT);
    }

    public String decrypt(String encrypted) throws InvalidCipherTextException {
        Cipher cipher = null;
        byte[] stringBytes = Base64.decode(encrypted.getBytes(),Base64.DEFAULT);
        try {
            cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
            return new String(cipher.doFinal(stringBytes));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return "Error decrypt!";
    }

    public static LevelOneRSA getLevelOneRSA(){
        if(levelOneRSA == null){
            levelOneRSA = new LevelOneRSA();
        }
        return levelOneRSA;
    }

    public String getPublicKey(){
       String pub = Base64.encodeToString(keyPair.getPublic().getEncoded(),Base64.DEFAULT);
        if(keyPair.getPublic()==null){
            init();
           return pub;
        }
        return pub;
    }
}
