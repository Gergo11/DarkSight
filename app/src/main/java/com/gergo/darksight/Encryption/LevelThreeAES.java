package com.gergo.darksight.Encryption;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.NoSuchAlgorithmException;
import java.security.Security;

import javax.crypto.KeyGenerator;

public class LevelThreeAES {

    private static LevelThreeAES levelThreeAES = null;
    private CipherParameters ivAndKey = null;

    public LevelThreeAES() {
        init();
    }

    private void init() {

        Security.addProvider(new BouncyCastleProvider());

        KeyGenerator keyGen = null;
        KeyGenerator ivGen = null;
        byte[] password = null;
        byte[] iv = null;
        try {
            keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256); //key is 256 bits
            password = keyGen.generateKey().getEncoded();

            ivGen = KeyGenerator.getInstance("AES");
            ivGen.init(128); //iv is 128 bits
            iv = ivGen.generateKey().getEncoded();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        ivAndKey = new ParametersWithIV(new KeyParameter(password), iv);
        //byte[] encryptedMessage = encrypt(plainText, ivAndKey);
        //byte[] decryptedMessage = decrypt(encryptedMessage, ivAndKey);
        //System.out.println(new String(decryptedMessage));
    }

    public static byte[] cipherData(PaddedBufferedBlockCipher cipher, byte[] data) throws Exception {
        byte[] outputBuffer = new byte[cipher.getOutputSize(data.length)];
        int length1 = cipher.processBytes(data, 0, data.length, outputBuffer, 0);
        int length2 = cipher.doFinal(outputBuffer, length1);
        byte[] result = new byte[length1 + length2];
        System.arraycopy(outputBuffer, 0, result, 0, result.length);
        return result;
    }

    public String encrypt(String messeage, CipherParameters ivAndKey) throws Exception {
        byte[] plain = messeage.getBytes();
        PaddedBufferedBlockCipher aes = new PaddedBufferedBlockCipher(
                new CBCBlockCipher(
                        new AESEngine()
                )
        );
        aes.init(true, ivAndKey);
        return new String(cipherData(aes, plain));

    }

    public String decrypt(String messeage) throws Exception {
        byte[] cipher = messeage.getBytes();
        PaddedBufferedBlockCipher aes = new PaddedBufferedBlockCipher(
                new CBCBlockCipher(
                        new AESEngine()
                )
        );
        aes.init(false, ivAndKey);
        return new String(cipherData(aes, cipher));
    }

    public CipherParameters getIvAndKey() {
        return ivAndKey;
    }

    public static LevelThreeAES getLevelThreeAES() {
        if (levelThreeAES == null) {
            levelThreeAES = new LevelThreeAES();
        }
        return levelThreeAES;
    }
}
