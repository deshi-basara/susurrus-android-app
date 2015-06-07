package rocks.susurrus.susurrus.tasks;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import rocks.susurrus.susurrus.models.RoomModel;
import rocks.susurrus.susurrus.services.WifiDirectService;

/**
 * Created by simon on 07.06.15.
 */
public class ClientAuthenticationTask extends AsyncTask<Void, Void, Boolean> {
    public static final String LOG_TAG = "ClientAuthentication";

    /**
     * Data
     */
    private String authServerAddress;
    private RoomModel clickedRoom;

    /**
     * Task constructor.
     * @param authServerAddress
     * @param clickedRoom
     */
    public ClientAuthenticationTask(String authServerAddress, RoomModel clickedRoom) {
        this.authServerAddress = authServerAddress;
        this.clickedRoom = clickedRoom;
    }

    @Override
    /**
     * Executed when the AsyncTask is started.
     */
    protected Boolean doInBackground(Void... voids) {
        Log.d(LOG_TAG, "Authentication-Client starting ...");

        try {
            // create a new authentication client
            Socket authClient = new Socket();

            // connect with the authentication socket
            //@todo send authentication data if needed
            authClient.bind(null);
            authClient.connect(new InetSocketAddress(this.authServerAddress, WifiDirectService.
                    SERVICE_AUTH_PORT));

            Log.d(LOG_TAG, "... Authenticated!");

            // close socket
            authClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        } /*finally{
            if (client != null) {
                if (socket.isConnected()) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }*/

        return true;
    }
}
