package rocks.susurrus.susurrus;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.ActionProvider;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.EditText;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.*;

import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.SealedObject;

import static org.junit.Assert.*;

import rocks.susurrus.susurrus.models.MessageModel;
import rocks.susurrus.susurrus.utils.Crypto;
import rocks.susurrus.susurrus.utils.Settings;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class CryptoTest {
    static final String LOG_TAG = "CryptoTest";

    private final String settingsPassword = "userpassword";
    private final String settingsPasswordInvalid = "invalidpassword";

    @Test
    public void invalidSettingsUnlock() throws Exception {
        boolean unlocked = Settings.unlockSettings(RuntimeEnvironment.application,
                settingsPasswordInvalid);

        assertEquals("Settings were encrypted with an invalid password", unlocked, true);
    }

    @Test
    public void validSettingsUnlock() throws Exception {
        boolean unlocked = Settings.unlockSettings(RuntimeEnvironment.application,
                settingsPassword);

        assertEquals("Settings were not encrypted with a valid password", unlocked, false);
    }

    @Test
    public void encryptAndDecryptMessage() throws Exception {
        MessageModel newMessage = new MessageModel(false, "OWNER", 1);


        PublicKey pubTest = Settings.getInstance().getPublicKey();
        PrivateKey privTest = Settings.getInstance().getPrivateKey();

        Log.d(LOG_TAG, "plain: " + newMessage.getOwnerName());

        SealedObject encrypted = Crypto.encryptBytes(newMessage, pubTest);
        assertNotNull("Encrypted SealedObject is null", encrypted);

        Object decrypted = Crypto.decryptBytes(encrypted, privTest);
        assertNotNull("Decrypted Object is null", decrypted);

        MessageModel oldMessage = (MessageModel) decrypted;
        assertEquals("Values before/after encryption don't match", oldMessage.getOwnerName(),
                "OWNER");
    }

}
