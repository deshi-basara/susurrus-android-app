package rocks.susurrus.susurrus.threads;

import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import rocks.susurrus.susurrus.models.RoomModel;
import rocks.susurrus.susurrus.services.WifiDirectService;

/**
 * Created by simon on 07.06.15.
 */
public class ServerAuthenticationThread implements Runnable {
    public static final String LOG_TAG = "ServerAuthentication";

    /**
     * Networking
     */
    private ArrayList<InetAddress> authenticatedClients;
    private ServerSocket authSocket;

    /**
     * Data
     */
    private RoomModel administratedRoom;

    /**
     * Thread constructor.
     * @param startedRoom
     */
    public ServerAuthenticationThread(RoomModel startedRoom) {
        this.administratedRoom = startedRoom;
        this.authenticatedClients = new ArrayList<InetAddress>();
    }

    @Override
    /**
     * Is executed when thread is started.
     */
    public void run() {
        Log.d(LOG_TAG, "Authentication-Thread starting ...");

        // clear list from potential outdated clients
        this.authenticatedClients.clear();

        try {
            // create a new socket server
            authSocket = new ServerSocket(WifiDirectService.SERVICE_AUTH_PORT);

            while(true) {

                // only allow one connection at once
                Socket authClient = authSocket.accept();

                // is newly connected client already authenticated?
                InetAddress authClientAddress = authClient.getInetAddress();
                if(!this.authenticatedClients.contains(authClientAddress)) {
                    // not connected,
                    // check if the started Room has a password
                    if(administratedRoom.hasEncryption()) {
                        //@todo check if the encryption passwords match
                    }

                    // add client to the authenticatedClients-list
                    authenticatedClients.add(authClientAddress);

                    Log.d(LOG_TAG, "New Authentication: " + authClientAddress);
                }

                authClient.close();
            }
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

    }

    /**
     * Invoked if thread is stopped.
     */
    public void onStop() {
        // try to close the authentication socket.
        try {
            // make sure you close the socket upon exiting
            authSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
