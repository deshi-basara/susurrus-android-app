package rocks.susurrus.susurrus.tasks;

import android.os.AsyncTask;
import android.os.Message;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import rocks.susurrus.susurrus.ChatActivity;
import rocks.susurrus.susurrus.chat.models.MessageModel;
import rocks.susurrus.susurrus.services.WifiDirectService;

/**
 * Created by simon on 06.06.15.
 */
public class ServerDistributionTask extends AsyncTask<Void, MessageModel, Void> {
    public static final String LOG_TAG = "ServerDistributionTask";

    /**
     * Constants
     */
    public static final int MESSAGE_SENDING = 1;
    public static final int MESSAGE_SENT = 2;
    public static final int MESSAGE_ERROR = 3;

    /**
     * Attributes
     */
    private ChatActivity chatActivity;

    /**
     * Networking
     */
    private ServerSocket socket;

    /**
     * Task constructor.
     * @param chatActivity Needed for progress feedback calls.
     */
    public ServerDistributionTask(ChatActivity chatActivity) {
        this.chatActivity = chatActivity;
    }

    @Override
    /**
     * Executed when the AsyncTask is started.
     */
    protected Void doInBackground(Void... params) {
        Log.d(LOG_TAG, "Starting the messaging-distribution-server ...");

        //Display le message on the sender before sending it
        //publishProgress(msg);

        //Send the message to clients
        /*try {
            ArrayList<InetAddress> listClients = ServerInit.clients;
            for(InetAddress addr : listClients){

                if(msg[0].getSenderAddress()!=null && addr.getHostAddress().equals(msg[0].getSenderAddress().getHostAddress())){
                    return msg[0];
                }

                Socket socket = new Socket();
                socket.setReuseAddress(true);
                socket.bind(null);
                Log.v(TAG,"Connect to client: " + addr.getHostAddress());
                socket.connect(new InetSocketAddress(addr, SERVER_PORT));
                Log.v(TAG, "doInBackground: connect to "+ addr.getHostAddress() +" succeeded");

                OutputStream outputStream = socket.getOutputStream();

                new ObjectOutputStream(outputStream).writeObject(msg[0]);

                Log.v(TAG, "doInBackground: write to "+ addr.getHostAddress() +" succeeded");
                socket.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Erreur d'envoie du message");
        }*/

        return null;
    }

}
