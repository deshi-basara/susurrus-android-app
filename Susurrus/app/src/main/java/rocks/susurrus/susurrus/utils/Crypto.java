package rocks.susurrus.susurrus.utils;

import android.util.Base64;

import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

/**
 * Created by simon on 14.06.15.
 */
public class Crypto {

    private static final String CRYPTO_ALGO = "RSA";

    public static ArrayList generateKeys() {
        ArrayList keys = new ArrayList();

        // initiate the key-generator (rsa)
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance(CRYPTO_ALGO);
            generator.initialize(1024);

            // generate key-pair
            KeyPair pair = generator.generateKeyPair();
            PrivateKey privateKey = pair.getPrivate();
            PublicKey publicKey = pair.getPublic();

            // preparing keys
            keys.add(privateKey);
            keys.add(publicKey);
        } catch(NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return keys;
    }

    public static String keyToString(Key key) {
        byte[] keyBytes = key.getEncoded();
        String key64 = Base64.encodeToString(keyBytes, Base64.DEFAULT);

        return key64;
    }

    public static PublicKey publicStringToKey(String keyString) {
        // convert back to bytes and construct an X509EncodedKeySpec from it
        byte[] keyBytes = Base64.decode(keyString, Base64.DEFAULT);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);

        // obtain the publicKey
        PublicKey publicKey = null;
        try {
            KeyFactory generator = KeyFactory.getInstance(CRYPTO_ALGO);
            publicKey = generator.generatePublic(keySpec);
        } catch(NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch(InvalidKeySpecException e) {
            e.printStackTrace();
        }

        return publicKey;
    }

    public static PrivateKey privateStringToKey(String keyString) {
        // convert back to bytes and construct an PKCS8EncodedKeySpec from it
        byte[] keyBytes = Base64.decode(keyString, Base64.DEFAULT);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);

        // obtain the publicKey
        PrivateKey privateKey = null;
        try {
            KeyFactory generator = KeyFactory.getInstance(CRYPTO_ALGO);
            privateKey = generator.generatePrivate(keySpec);
        } catch(NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch(InvalidKeySpecException e) {
            e.printStackTrace();
        }

        return privateKey;
    }

}
