package rocks.susurrus.susurrus.tasks;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import rocks.susurrus.susurrus.MainActivity;
import rocks.susurrus.susurrus.chat.models.MessageModel;
import rocks.susurrus.susurrus.models.AuthModel;
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
    private AuthModel authModel;

    /**
     * Task constructor.
     * @param authServerAddress
     */
    public ClientAuthenticationTask(Handler handler, MainActivity mainActivity,
                                    InetAddress authServerAddress, AuthModel authModel) {
        this.handler = handler;
        this.mainActivity = mainActivity;
        this.authServerAddress = authServerAddress;
        this.authModel = authModel;
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

            // get streams
            OutputStream outputStream = authClient.getOutputStream();
            InputStream inputStream = authClient.getInputStream();

            // send authentication-request
            new ObjectOutputStream(outputStream).writeObject(this.authModel);

            // wait for authentication-response
            ObjectInputStream objectIS = new ObjectInputStream(inputStream);
            AuthModel authResponse = (AuthModel) objectIS.readObject();

            // client authenticated?
            boolean isAuthenticated = authResponse.getAuthenticationStatus();
            if(isAuthenticated) {
                // authenticated
                publishMessage(this.SOCKET_AUTHENTICATED);
            }
            else {
                // not authenticated
                publishMessage(this.SOCKET_PASSWORD_WRONG);
            }

        } catch(IOException e) {
            e.printStackTrace();
        } catch(ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            // try to disconnect if client is still connected
            if(authClient != null && authClient.isConnected()) {
                try {
                    Log.d(LOG_TAG, "Closing authClient ...");
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
