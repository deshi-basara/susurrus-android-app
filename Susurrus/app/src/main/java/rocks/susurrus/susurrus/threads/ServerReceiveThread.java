package rocks.susurrus.susurrus.threads;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import rocks.susurrus.susurrus.chat.models.MessageModel;
import rocks.susurrus.susurrus.services.WifiDirectService;

/**
 * Created by simon on 07.06.15.
 */
public class ServerReceiveThread implements Runnable {
    public static final String LOG_TAG = "ServerReceiveThread";

    private ServerSocket socket;

    public ServerReceiveThread(){

    }

    @Override
    /**
     * Is executed when thread is started.
     */
    public void run() {
        Log.d(LOG_TAG, "ServerReceive-Thread starting ...");

        try {
            // create a new socket server
            socket = new ServerSocket(WifiDirectService.SERVICE_PORT);

            while(true){

                // only accept on connection to server at the time
                Socket connectedClient = socket.accept();

                // get server's input-stream, buffer it and read buffered messages
                InputStream inputStream = connectedClient.getInputStream();
                ObjectInputStream objectIS = new ObjectInputStream(inputStream);
                MessageModel receivedMessage = (MessageModel) objectIS.readObject();

                // get the ip of the client and add it to the message
                InetAddress clientAddress = connectedClient.getInetAddress();
                receivedMessage.setOwnerAddress(clientAddress);

                Log.d(LOG_TAG, "New message from " + clientAddress + ": " + receivedMessage.
                        getMessage());

                // close the connection and add message to the chat
                connectedClient.close();
                //publishProgress(receivedMessage);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Invoked if thread is stopped.
     */
    protected void onStop() {
        // close a potentially opened socket connection on cancellation
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
