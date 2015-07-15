package rocks.susurrus.susurrus.utils;

import android.util.Base64;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.security.InvalidKeyException;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;

import rocks.susurrus.susurrus.models.EncryptionModel;

/**
 * Created by simon on 14.06.15.
 */
public class Crypto {

    private static final String CRYPTO_ALGO = "RSA";

    /**
     * Generates a private- and public-RSA-Key with 2048 bits.
     *
     * @return Arraylist with privateKey [0] and publicKey [1].
     * @throws RuntimeException
     */
    public static ArrayList generateKeys() throws RuntimeException {
        ArrayList keys = new ArrayList();

        // initiate the key-generator (rsa)
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance(CRYPTO_ALGO);
            SecureRandom randomNumbers = new SecureRandom();
            generator.initialize(2048, randomNumbers);

            // generate key-pair
            KeyPair pair = generator.generateKeyPair();
            PrivateKey privateKey = pair.getPrivate();
            PublicKey publicKey = pair.getPublic();

            // preparing keys
            keys.add(privateKey);
            keys.add(publicKey);
        } catch(NoSuchAlgorithmException e) {
            e.printStackTrace();

            throw new RuntimeException("Keys could not be generated");
        }

        return keys;
    }

    /**
     * Encodes a given public-/private-Key as base64 string.
     * @param key
     * @return
     */
    public static String keyToString(Key key) {
        byte[] keyBytes = key.getEncoded();
        String key64 = Base64.encodeToString(keyBytes, Base64.DEFAULT);

        return key64;
    }

    /**
     * Converts a given base64-String into an usable publicKey.
     *
     * @param keyString Base64-String of a publicKey.
     * @return PublicKey.
     */
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

    /**
     * Converts a given base64-String into an usable privateKey.
     *
     * @param keyString
     * @return
     */
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

    /**
     * Encrypts an unencrypted serializable Object with AES.
     *
     * At first an AES-Session-Key is generated for encrypting the handed SealedObject. The AES-
     * Session-Key is wrapped with the user's publicKey. The wrapped-key can be used to decrypt
     * the SealedObject later.
     *
     * @param _unencryptedObj Serializable object.
     * @param _publicKey The user's publicKey.
     * @return An EncryptionModel with SealedObject and wrapped-Key.
     */
    public static EncryptionModel encryptBytes(Serializable _unencryptedObj,
                                                             PublicKey _publicKey) {

        // generate a random AES-key for encrypting the object's data
        SecretKey aesSessionKey;
        try {
            // generate key
            KeyGenerator generator = KeyGenerator.getInstance("AES");
            SecureRandom randomNumbers = new SecureRandom();
            generator.init(256, randomNumbers);
            aesSessionKey = generator.generateKey();

        } catch(NoSuchAlgorithmException e) {
            e.printStackTrace();

            return null;
        }

        // initiate the encryption cipher
        Cipher cipher;
        byte[] wrappedKey;
        try {
            // get a cipher object that implements the RSA transformation
            cipher = Cipher.getInstance(CRYPTO_ALGO);

            // initialize cipher with the public key. Use WRAP_MODE for wrapping the encrypted
            // AES-data with the handed public key.
            cipher.init(Cipher.WRAP_MODE, _publicKey);

            // wrap AES-key
            wrappedKey = cipher.wrap(aesSessionKey);

            // get a new cipher and use the wrapped AES-key for encryption
            cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, aesSessionKey);

        } catch(NoSuchAlgorithmException e) {
            e.printStackTrace();

            return null;
        } catch(NoSuchPaddingException e) {
            e.printStackTrace();

            return null;
        } catch(InvalidKeyException e) {
            e.printStackTrace();

            return null;
        } catch(IllegalBlockSizeException e) {
            e.printStackTrace();

            return null;
        }

        // use the cipher for encryption
        SealedObject encryptedObj;
        try {
            // creates a new SealedObject instance wrapping the specified object and sealing it
            // using the specified cipher.
            encryptedObj = new SealedObject(_unencryptedObj, cipher);
        } catch(IllegalBlockSizeException e) {
            e.printStackTrace();

            return null;
        } catch(NullPointerException e) {
            e.printStackTrace();

            return null;
        } catch(IOException e) {
            e.printStackTrace();

            return null;
        }

        return new EncryptionModel(encryptedObj, wrappedKey);
    }

    /**
     * Decrypts an AES encrypted serializable Object with the handed wrapped- and private-Key.
     *
     * At first the AES-Session-Key is restored from the user's privateKey which unwraps it from
     * the wrappedKey. The restored AES-Session-Key is used to decrypt the handed SealedObject.
     *
     * @param _encryptedObj RSA-encrypted sealedObject.
     * @param _privateKey The user's privateKey.
     * @return An unencrypted Object.
     */
    public static Object decryptBytes(SealedObject _encryptedObj, PrivateKey _privateKey,
                                      byte[] _wrappedKey) {

        // initiate the decryption cipher
        Cipher cipher;
        try {
            // get a Cipher object that implements the RSA transformation
            cipher = Cipher.getInstance(CRYPTO_ALGO);

            // initialize this cipher with the private key from the given certificate and use it
            // for decryption
            cipher.init(Cipher.UNWRAP_MODE, _privateKey);

            Key aesSessionKey = cipher.unwrap(_wrappedKey, "AES", Cipher.SECRET_KEY);

            cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, aesSessionKey);

        } catch(NoSuchAlgorithmException e) {
            e.printStackTrace();

            return null;
        } catch(NoSuchPaddingException e) {
            e.printStackTrace();

            return null;
        } catch(InvalidKeyException e) {
            e.printStackTrace();

            return null;
        }

        // use the cipher for decryption
        Object decryptedObj;
        try {
            // returns the wrapped object, decrypting it using the specified key
            decryptedObj = _encryptedObj.getObject(cipher);
        } catch(IOException e) {
            e.printStackTrace();

            return null;
        } catch(ClassNotFoundException e) {
            e.printStackTrace();

            return null;
        } catch(IllegalBlockSizeException e) {
            e.printStackTrace();

            return null;
        } catch(BadPaddingException e) {
            e.printStackTrace();

            return null;
        }

        return decryptedObj;
    }

}
