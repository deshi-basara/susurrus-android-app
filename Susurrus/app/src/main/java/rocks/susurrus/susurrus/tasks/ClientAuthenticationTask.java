package rocks.susurrus.susurrus.tasks;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import rocks.susurrus.susurrus.MainActivity;
import rocks.susurrus.susurrus.chat.models.MessageModel;
import rocks.susurrus.susurrus.models.RoomModel;
import rocks.susurrus.susurrus.services.WifiDirectService;

/**
 * Created by simon on 07.06.15.
 */
public class ClientAuthenticationTask extends AsyncTask<Void, Void, Boolean> {
    public static final String LOG_TAG = "ClientAuthentication";

    /**
     * Constans
     */
    public static final int SOCKET_CONNECTED = 1;
    public static final int SOCKET_PASSWORD_NEEDED = 2;
    public static final int SOCKET_PASSWORD_WRONG = 3;
    public static final int SOCKET_AUTHENTICATED = 4;
    public static final int SOCKET_EXCEPTION = 5;

    /**
     * Handler
     */
    private Handler handler;

    /**
     * Data
     */
    private MainActivity mainActivity;
    private InetAddress authServerAddress;

    /**
     * Task constructor.
     * @param authServerAddress
     */
    public ClientAuthenticationTask(Handler handler, MainActivity mainActivity,
                                    InetAddress authServerAddress) {
        this.handler = handler;
        this.mainActivity = mainActivity;
        this.authServerAddress = authServerAddress;
    }

    @Override
    /**
     * Executed when the AsyncTask is started.
     */
    protected Boolean doInBackground(Void... voids) {
        Log.d(LOG_TAG, "Authentication-Client starting ...");

        Socket authClient = null;
        try {
            // create a new authentication client
            authClient = new Socket();

            authClient.bind(null);
            authClient.connect(new InetSocketAddress(this.authServerAddress,
                    WifiDirectService.SERVICE_AUTH_PORT));

            publishMessage(this.SOCKET_CONNECTED);
            //mainActivity.showRoomJoinFeedbackUpdate(this.SOCKET_CONNECTED);

            // connect with the authentication socket
            //@todo send authentication data if needed
            //@todo handle authentication states ERROR/PASSWORD_NEEDED

            publishMessage(this.SOCKET_AUTHENTICATED);
            //mainActivity.showRoomJoinFeedbackUpdate(this.SOCKET_AUTHENTICATED);

        } catch(IOException e) {
            e.printStackTrace();

            publishMessage(this.SOCKET_EXCEPTION);
            //mainActivity.showRoomJoinFeedbackUpdate(this.SOCKET_EXCEPTION);
        } finally {
            // try to disconnect if client is still connected
            if(authClient != null && authClient.isConnected()) {
                try {
                    authClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return true;
    }

    /**
     * Broadcasts the receivedMessage to the handler in the ui-thread.
     * @param authStatus Authentication Status.
     */
    private void publishMessage(int authStatus) {
        Log.d(LOG_TAG, "PublishMessage: " + authStatus);

        // and set the Message.what-field as authStatus
        Message outputMessage = this.handler.obtainMessage(authStatus);
        outputMessage.sendToTarget();
    }
}
