package com.gergo.darksight.Encryption;

import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;

public class LevelOneRSA {

    private static LevelOneRSA levelOneRSA =null;
    private AsymmetricCipherKeyPair keyPair = null;

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

    public static String getHexString(byte[] b) throws Exception {
        String result = "";
        for (int i=0; i < b.length; i++) {
            result +=
                    Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
        }
        return result;
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length()-1;
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }


    public AsymmetricCipherKeyPair generateKeys() throws NoSuchAlgorithmException{

        RSAKeyPairGenerator generator = new RSAKeyPairGenerator();
        generator.init(new RSAKeyGenerationParameters
                (
                        new BigInteger("10001", 16),//publicExponent
                        SecureRandom.getInstance("SHA1PRNG"),//pseudorandom number generator
                        4096,//strength
                        80//certainty
                ));

        return generator.generateKeyPair();
    }

    public String encrypt(String messeage, PublicKey publicKey) throws Exception{

        byte[] data = messeage.getBytes();
        Security.addProvider(new BouncyCastleProvider());
        RSAEngine engine = new RSAEngine();
        engine.init(true, (CipherParameters) publicKey); //true if encrypt
        byte[] hexEncodedCipher = engine.processBlock(data, 0, data.length);
        return getHexString(hexEncodedCipher);
    }

    public String decrypt(String encrypted) throws InvalidCipherTextException {

        Security.addProvider(new BouncyCastleProvider());
        AsymmetricBlockCipher engine = new RSAEngine();
        engine.init(false, keyPair.getPrivate()); //false for decryption
        byte[] encryptedBytes = hexStringToByteArray(encrypted);
        byte[] hexEncodedCipher = engine.processBlock(encryptedBytes, 0, encryptedBytes.length);
        return new String (hexEncodedCipher);
    }



    public static LevelOneRSA getLevelOneRSA(){
        if(levelOneRSA == null){
            levelOneRSA = new LevelOneRSA();
        }
        return levelOneRSA;
    }

    public CipherParameters getPublicKey(){
        if(keyPair.getPublic()==null){
            init();
           return keyPair.getPublic();
        }
        return keyPair.getPublic();
    }

        //	AsymmetricCipherKeypair includes both private and public keys, but AsymmetricKeyPair includes only public

       // AsymmetricCipherKeyPair keyPair = GenerateKeys();
        //String encryptedMessage = Encrypt(plainMessage.getBytes("UTF-8"), keyPair.getPublic());
        //String decryptedMessage = Decrypt(encryptedMessage, (AsymmetricKeyParameter) keyPair.getPrivate());


}
