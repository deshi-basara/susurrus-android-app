package rocks.susurrus.susurrus.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import rocks.susurrus.susurrus.network.WifiDirectLocalService;

/**
 * Created by simon on 06.06.15.
 */
public class WifiDirectServerTask extends AsyncTask<Void, Void, Void> {
    public static final String LOG_TAG = "WifiDirectServerTask";

    private Context context;
    private ServerSocket socket;

    public WifiDirectServerTask(Context c){
        context = c;
    }

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
        Log.d(LOG_TAG, "Starting the messaging-server ...");

        try {
            // create a new socket server
            socket = new ServerSocket(WifiDirectLocalService.SERVICE_PORT);
            Log.d(LOG_TAG, "... accepting connections, start listening for messages.");

            while(true){

                // only accept on connection to server at the time
                Socket connectedClient = socket.accept();

                // get server's input-stream, buffer it and read buffered messages
                InputStream inputStream = connectedClient.getInputStream();
                ObjectInputStream objectIS = new ObjectInputStream(inputStream);
                Message message = (Message) objectIS.readObject();

                // get the ip of the client and add it to the message
                InetAddress clientAddress = connectedClient.getInetAddress();
                //message.setOwnerAddress(senderAddr);

                Log.d(LOG_TAG, "New message from " + clientAddress + ": " + message.toString());

                // close the connection and add message to the chat
                connectedClient.close();
                //publishProgress(message);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    /*@Override
    protected void onProgressUpdate(Message... values) {
        super.onProgressUpdate(values);
        playNotification(mContext, values[0]);

        //If the message contains a video or an audio, we saved this file to the external storage
        int type = values[0].getmType();
        if(type==Message.AUDIO_MESSAGE || type==Message.VIDEO_MESSAGE || type==Message.FILE_MESSAGE || type==Message.DRAWING_MESSAGE){
            values[0].saveByteArrayToFile(mContext);
        }

        new SendMessageServer(mContext, false).executeOnExecutor(THREAD_POOL_EXECUTOR, values);
    }*/

}
