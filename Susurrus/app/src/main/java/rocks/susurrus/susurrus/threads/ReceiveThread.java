package rocks.susurrus.susurrus.threads;

import android.os.Message;
import android.util.Log;
import android.os.Handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.SealedObject;

import rocks.susurrus.susurrus.models.EncryptionModel;
import rocks.susurrus.susurrus.models.MessageModel;
import rocks.susurrus.susurrus.services.WifiDirectService;
import rocks.susurrus.susurrus.utils.Crypto;

/**
 * Created by simon on 07.06.15.
 */
public class ReceiveThread extends Thread {
    public static final String LOG_TAG = "ReceiveThread";

    /**
     * Handler
     */
    private Handler handler;

    /**
     * Networking
     */
    private ServerSocket socket;

    /**
     * Data
     */
    private boolean isRunning = true;
    private PrivateKey masterPrivateKey;

    public ReceiveThread(Handler _chatHandler, PrivateKey _masterPrivateKey){
        this.handler = _chatHandler;
        this.masterPrivateKey = _masterPrivateKey;
    }

    @Override
    /**
     * Is executed when thread is started.
     */
    public void run() {
        Log.d(LOG_TAG, "Receive-Thread started.");

        try {
            // create a new socket server
            socket = new ServerSocket(WifiDirectService.SERVICE_PORT);

            while(this.isRunning && !isInterrupted()){

                // only accept on connection to server at the time
                Socket connectedClient = socket.accept();

                // get server's input-stream, buffer it and read buffered messages
                InputStream inputStream = connectedClient.getInputStream();
                ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
                EncryptionModel encryptedMessage = (EncryptionModel) objectInputStream.readObject();

                SealedObject sealedMessage = encryptedMessage.getSealedObject();
                byte[] wrappedKey = encryptedMessage.getWrappedKey();

                MessageModel receivedMessage = (MessageModel)
                        Crypto.decryptBytes(sealedMessage, this.masterPrivateKey, wrappedKey);

                // get the ip of the client and add it to the message
                InetAddress clientAddress = connectedClient.getInetAddress();
                receivedMessage.setOwnerAddress(clientAddress);
                receivedMessage.setOwnership(false);

                Log.d(LOG_TAG, "New message from " + clientAddress + ": " + receivedMessage.
                        getMessage());

                // close the connection and add message to the chat
                connectedClient.close();

                publishMessage(receivedMessage);
                //publishProgress(receivedMessage);
            }

        } catch(IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            if(socket != null) {
                // close a potentially opened socket connection on cancellation
                try {
                    socket.close();

                    Log.d(LOG_TAG, "Chat-Socket closed.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        Log.d(LOG_TAG, "Chat-Thread stopped.");
    }

    /**
     * Broadcasts the receivedMessage to the handler in the ui-thread.
     * @param receivedMessage Message sent by a client to the master-socket.
     */
    private void publishMessage(MessageModel receivedMessage) {
        Message outputMessage = this.handler.obtainMessage(1, receivedMessage);
        outputMessage.sendToTarget();
    }

    /**
     * Requests the termination/interruption of the thread.
     */
    public void terminate() {
        this.isRunning = false;
        interrupt();

        if(this.socket != null) {
            // try to close the authentication socket.
            try {
                // make sure you close the socket upon exiting
                this.socket.close();

                Log.d(LOG_TAG, "Chat-Socket closed.");
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }
}
