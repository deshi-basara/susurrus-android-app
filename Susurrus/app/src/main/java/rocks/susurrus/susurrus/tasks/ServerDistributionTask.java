package rocks.susurrus.susurrus.tasks;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
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
import rocks.susurrus.susurrus.threads.ServerAuthenticationThread;

/**
 * Created by simon on 06.06.15.
 */
public class ServerDistributionTask extends AsyncTask<MessageModel, Integer, Boolean> {
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
    protected Boolean doInBackground(MessageModel... messageModels) {
        Log.d(LOG_TAG, "Starting the server-distribution-task ...");

        // send message to all authenticated clients
        try {
            ArrayList<InetAddress> authenticatedClients = ServerAuthenticationThread.
                    authenticatedClients;

            InetAddress ownerAddress = messageModels[0].getOwnerAddress();
            // loop through all authenticated clients
            Log.d(LOG_TAG, "availableClients: " + authenticatedClients.size());
            for(int i = 0; i < authenticatedClients.size(); i++) {
                InetAddress currentAddress = authenticatedClients.get(i);

                // check if the currentAddress matches the Address of the message owner, skip
                // the client
                if(currentAddress.equals(ownerAddress)) {
                    // message owner, skip
                    break;
                }

                // create a socket-client and send the message to all clients
                Socket socket = new Socket();
                socket.setReuseAddress(true);
                socket.bind(null);
                socket.connect(new InetSocketAddress(currentAddress, WifiDirectService.
                        SERVICE_PORT));

                Log.d(LOG_TAG, "Connected to client: " + currentAddress);

                // get the output-Stream of the socket and send the message
                OutputStream outputStream = socket.getOutputStream();
                new ObjectOutputStream(outputStream).writeObject(messageModels[0]);

                Log.v(LOG_TAG, "doInBackground: write to "+ currentAddress +" succeeded");
                socket.close();
            }
        } catch(IOException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "Error sending a message");
        }

        return true;
    }

}
