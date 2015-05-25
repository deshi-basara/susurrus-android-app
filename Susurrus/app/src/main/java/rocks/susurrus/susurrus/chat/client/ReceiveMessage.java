package rocks.susurrus.susurrus.chat.client;

import android.os.AsyncTask;
import android.os.Message;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by simon on 25.05.15.
 */
public class ReceiveMessage extends AsyncTask<Void, Void, Void>  {
    private static final String LOG_TAG = "Client.ReceiveMessage";
    private static final int SERVER_PORT = 4446;

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
        Log.d(LOG_TAG, "Establishing a connection to the socket server ...");

        try {
            // establish a socket connection to server
            socket = new ServerSocket(SERVER_PORT);
            Log.d(LOG_TAG, "... accepting connection, start listening for messages.");

            while(true){

                // only accept on connection to server at the time
                Socket serverSocket = socket.accept();

                // get server's input-stream, buffer it and read buffered messages
                InputStream inputStream = serverSocket.getInputStream();
                BufferedInputStream buffer = new BufferedInputStream(inputStream);
                ObjectInputStream objectIS = new ObjectInputStream(buffer);
                Message message = (Message) objectIS.readObject();

                // close connection to server
                serverSocket.close();

                Log.d(LOG_TAG, "New message received");
                Log.d(LOG_TAG, message.toString());
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
