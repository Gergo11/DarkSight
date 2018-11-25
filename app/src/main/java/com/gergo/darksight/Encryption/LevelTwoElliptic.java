package com.gergo.darksight.Encryption;

import android.util.Log;

import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.interfaces.ECPrivateKey;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.provider.JDKKeyPairGenerator;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPrivateKeySpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.ECGenParameterSpec;


import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyAgreement;
import javax.crypto.NoSuchPaddingException;

public class LevelTwoElliptic {

    private static LevelTwoElliptic levelTwoElliptic = null;
    KeyPairGenerator kpgen = null;
    private KeyPair keyPair = null;


    public LevelTwoElliptic() {
        init();
    }

    private void init (){
        if(kpgen == null) {
            try {
                Security.addProvider(new BouncyCastleProvider());
                kpgen = KeyPairGenerator.getInstance("EC", "BC");
                kpgen.initialize(new ECGenParameterSpec("prime192v1"), new SecureRandom());
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (NoSuchProviderException e) {
                e.printStackTrace();
            } catch (InvalidAlgorithmParameterException e) {
                e.printStackTrace();
            }
        }
        //Log.d("keygen","+++++++++++++"+kpgen.toString());
        keyPair = kpgen.generateKeyPair();
    }

    final protected static char[] hexArray = "0123456789abcdef".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte [] savePublicKey (PublicKey key) throws Exception
    {
        //return key.getEncoded();
        //.getEncoded(true)
        ECPublicKey eckey = (ECPublicKey)key;
        return eckey.getQ().getEncoded();
    }

    public static PublicKey loadPublicKey (byte [] data) throws Exception
    {
		/*KeyFactory kf = KeyFactory.getInstance("ECDH", "BC");
		return kf.generatePublic(new X509EncodedKeySpec(data));*/

        ECParameterSpec params = ECNamedCurveTable.getParameterSpec("prime192v1");
        ECPublicKeySpec pubKey = new ECPublicKeySpec(params.getCurve().decodePoint(data), params);
        KeyFactory kf = KeyFactory.getInstance("EC", "BC");
        return kf.generatePublic(pubKey);
    }

    public static byte [] savePrivateKey (PrivateKey key) throws Exception
    {
        //return key.getEncoded();

        ECPrivateKey eckey = (ECPrivateKey)key;
        return eckey.getD().toByteArray();
    }

    public static PrivateKey loadPrivateKey (byte [] data) throws Exception
    {
        //KeyFactory kf = KeyFactory.getInstance("ECDH", "BC");
        //return kf.generatePrivate(new PKCS8EncodedKeySpec(data));

        ECParameterSpec params = ECNamedCurveTable.getParameterSpec("prime192v1");
        ECPrivateKeySpec prvkey = new ECPrivateKeySpec(new BigInteger(data), params);
        KeyFactory kf = KeyFactory.getInstance("EC", "BC");
        return kf.generatePrivate(prvkey);
    }

    public static void doECDH (String name, byte[] dataPrv, byte[] dataPub) throws Exception
    {
        KeyAgreement ka = KeyAgreement.getInstance("EC", "BC");
        ka.init(loadPrivateKey(dataPrv));
        ka.doPhase(loadPublicKey(dataPub), true);
        byte [] secret = ka.generateSecret();
        System.out.println(name + bytesToHex(secret));
    }

   // public static void main (String [] args) throws Exception
   // {
    //    Security.addProvider(new BouncyCastleProvider());
//
     //   KeyPairGenerator kpgen = KeyPairGenerator.getInstance("ECDH", "BC");
     //   kpgen.initialize(new ECGenParameterSpec("prime192v1"), new SecureRandom());
   //    KeyPair pairA = kpgen.generateKeyPair();
   //    KeyPair pairB = kpgen.generateKeyPair();
   //    System.out.println("Alice: " + pairA.getPrivate());
   //    System.out.println("Alice: " + pairA.getPublic());
   //    System.out.println("Bob:   " + pairB.getPrivate());
   //    System.out.println("Bob:   " + pairB.getPublic());
   //    byte [] dataPrvA = savePrivateKey(pairA.getPrivate());
   //    byte [] dataPubA = savePublicKey(pairA.getPublic());
   //    byte [] dataPrvB = savePrivateKey(pairB.getPrivate());
   //    byte [] dataPubB = savePublicKey(pairB.getPublic());
   //    System.out.println("Alice Prv: " + bytesToHex(dataPrvA));
   //    System.out.println("Alice Pub: " + bytesToHex(dataPubA));
   //    System.out.println("Bob Prv:   " + bytesToHex(dataPrvB));
   //    System.out.println("Bob Pub:   " + bytesToHex(dataPubB));
//
   //    doECDH("Alice's secret: ", dataPrvA, dataPubB);
   //    doECDH("Bob's secret:   ", dataPrvB, dataPubA);
   //}


    public String encrypt(Key key, String messeage){
        Cipher ec_cipher=null;
        byte[] clearText=messeage.getBytes();
        byte[] cipherText = null;
        try {
            ec_cipher = Cipher.getInstance("EC");
            ec_cipher.init(Cipher.ENCRYPT_MODE, key);
            cipherText=ec_cipher.doFinal(clearText);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

        return new String(cipherText);
    }

    public String decrypt(String messeage){

        Cipher ec_cipher=null;
        byte[] entcryptedText=messeage.getBytes();
        byte[] decipherText = null;

        try {
            ec_cipher = Cipher.getInstance("EC");
            ec_cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
            decipherText=ec_cipher.doFinal(entcryptedText);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

        return new String(decipherText);
    }

    public Key getPublicKey (){
        if(keyPair!=null){
            return keyPair.getPublic();
        }else {
            init();
            return keyPair.getPublic();
        }
    }

    public static LevelTwoElliptic getLevelTwoElliptic(){
        if(levelTwoElliptic == null){
            levelTwoElliptic = new LevelTwoElliptic();
        }
        return levelTwoElliptic;
    }
}
