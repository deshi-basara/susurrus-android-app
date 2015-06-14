package rocks.susurrus.susurrus.threads;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import rocks.susurrus.susurrus.chat.models.MessageModel;
import rocks.susurrus.susurrus.models.AuthModel;
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
    public static ArrayList<InetAddress> authenticatedClients;
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
                    // not connected yet, get client's input-stream, buffer it and read
                    // buffered messages
                    InputStream inputStream = authClient.getInputStream();
                    ObjectInputStream objectIS = new ObjectInputStream(inputStream);
                    AuthModel authRequest = (AuthModel) objectIS.readObject();

                    Log.d(LOG_TAG, "Authentication: " + authRequest.getRoomPassword());
                    Log.d(LOG_TAG, "hasPassword: "  + administratedRoom.hasEncryption());

                    // valid auth-request?

                    // check if the started Room has a password
                    boolean validPassword = false;
                    if(administratedRoom.hasEncryption()) {
                        // did client send correct password
                        validPassword = isValidPassword(authRequest.getRoomPassword());
                    }

                    // check if password is valid, if needed
                    if(administratedRoom.hasEncryption() && !validPassword) {
                        // not valid, set response to "not authenticated"
                        authRequest.setAuthenticationStatus(false);
                    }
                    else {
                        // no password needed or correct, add client to the authenticated-
                        // Clients-list and set response to "authenticated"
                        authenticatedClients.add(authClientAddress);
                        authRequest.setAuthenticationStatus(true);
                    }

                    // get the output-Stream of the client and send the response
                    OutputStream outputStream = authClient.getOutputStream();
                    new ObjectOutputStream(outputStream).writeObject(authRequest);

                    Log.d(LOG_TAG, "Authentication: " + authRequest.getAuthenticationStatus());
                    Log.d(LOG_TAG, "New Authentication: " + authClientAddress);
                }

                authClient.close();
            }
        } catch(IOException e) {
            e.printStackTrace();
        } catch(ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Invoked if thread is stopped.
     */
    public void onStop() {
        // try to close the authentication socket.
        try {
            // make sure you close the socket upon exiting
            authSocket.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }


    private boolean isValidRequest(AuthModel request) {
        try {
            String passwordString = request.getRoomPassword();
            String publicString = request.getPublicString();
        } catch(NoSuchMethodError e) {
            e.printStackTrace();

            return false;
        }

        //@todo isempty?

        return true;
    }

    private boolean isValidPassword(String password) {
        if(password.equals(this.administratedRoom.getRoomPassword())) {
            return true;
        }
        else {
            return false;
        }
    }
}
