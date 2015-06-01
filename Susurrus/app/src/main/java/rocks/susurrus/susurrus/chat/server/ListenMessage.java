package rocks.susurrus.susurrus.chat.server;

import android.os.AsyncTask;
import android.os.Message;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import rocks.susurrus.susurrus.chat.models.MessageModel;

/**
 * AsyncTask
 */
public class ListenMessage extends AsyncTask<Void, Void, Void> {
    private static final String LOG_TAG = "Client.ReceiveMessage";
    private static final int SERVER_PORT = 4445;

    private ServerSocket socket;

    @Override
    /**
     * Executed when the AsyncTask execution is cancled.
     */
    protected void onCancelled() {
        // close a potentially opened socket connection on cancellation
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onCancelled();
    }

    @Override
    /**
     * Executed when the AsyncTask is started.
     */
    protected Void doInBackground(Void... params) {
        Log.d(LOG_TAG, "SERVER: Creating a socket server ...");

        try {
            // establish a socket connection to server
            socket = new ServerSocket(SERVER_PORT);
            Log.d(LOG_TAG, "... accepting connections, start listening for messages.");

            while(true){

                // only accept on connection to server at the time
                Socket clientSocket = socket.accept();

                // get server's input-stream, buffer it and read buffered messages
                InputStream inputStream = clientSocket.getInputStream();
                ObjectInputStream objectIS = new ObjectInputStream(inputStream);
                MessageModel message = (MessageModel) objectIS.readObject();

                // get the ip of the client and add it to the message
                InetAddress senderAddr = clientSocket.getInetAddress();

                Log.d(LOG_TAG, "Sender IP: " + senderAddr);
                //message.setOwnerAddress(senderAddr);

                clientSocket.close();
                //publishProgress(message);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onProgressUpdate(Void... progress) {
    }

    @Override
    protected void onPostExecute(Void result) {
    }
}
