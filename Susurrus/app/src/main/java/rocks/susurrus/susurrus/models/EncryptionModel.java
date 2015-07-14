package rocks.susurrus.susurrus.models;

import java.io.Serializable;

import javax.crypto.SealedObject;

/**
 * Created by simon on 14.07.15.
 */
public class EncryptionModel implements Serializable {

    /**
     * Data
     */
    private SealedObject sealedObject;
    private byte[] wrappedKey;

    public EncryptionModel(SealedObject _sealedObject, byte[] _wrappedKey) {
        this.sealedObject = _sealedObject;
        this.wrappedKey = _wrappedKey;
    }

    public SealedObject getSealedObject() {
        return this.sealedObject;
    }
    public byte[] getWrappedKey() {
        return this.wrappedKey;
    }

}
