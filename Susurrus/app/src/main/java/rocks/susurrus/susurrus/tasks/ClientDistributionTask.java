package rocks.susurrus.susurrus.tasks;

import android.os.AsyncTask;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import rocks.susurrus.susurrus.ChatActivity;
import rocks.susurrus.susurrus.R;
import rocks.susurrus.susurrus.chat.models.MessageModel;
import rocks.susurrus.susurrus.services.WifiDirectService;

/**
 * Created by simon on 06.06.15.
 */
public class ClientDistributionTask extends AsyncTask<MessageModel, Integer, Boolean> {
    public static final String LOG_TAG = "ClientDistributionTask";

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
    private InetAddress serverAddress;
    private ListView messageList;

    /**
     * Task constructor.
     * @param chatActivity Needed for progress feedback calls.
     * @param serverAddress Destination address (socket-server) of the message.
     */
    public ClientDistributionTask(ChatActivity chatActivity, InetAddress serverAddress,
                                  ListView messageList) {
        this.chatActivity = chatActivity;
        this.serverAddress = serverAddress;
        this.messageList = messageList;
    }

    @Override
    /**
     * Executed when the AsyncTask is started.
     */
    protected Boolean doInBackground(MessageModel... messageModel) {
        Log.d(LOG_TAG, "Starting the messaging-distribution via server ...");

        Log.d(LOG_TAG, "Message: " + messageModel[0].getMessage());

        publishProgress(this.MESSAGE_SENDING);

        Socket client = null;
        try {
            // create a new socket client
            client = new Socket();

            client.setReuseAddress(true);
            client.bind(null);

            // connect client to the server
            client.connect(new InetSocketAddress(this.serverAddress,
                    WifiDirectService.SERVICE_PORT));

            Log.d(LOG_TAG, "Client successfully connected to " + this.serverAddress);
            Log.d(LOG_TAG, messageModel[0].toString());

            // get the client's output stream and write the message to it
            OutputStream outputStream = client.getOutputStream();
            // we only pass one argument at max, always use the first element [0] from the 'varargs'
            new ObjectOutputStream(outputStream).writeObject(messageModel[0]);

            Log.d(LOG_TAG, "Client successfully sent his message to " + this.serverAddress);
            publishProgress(this.MESSAGE_SENT);
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            if(client != null && client.isConnected()) {
                try {
                    client.close();
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return false;
    }

    @Override
    /**
     * Is called via publishProgress(int...).
     * This method is used to display any form of progress in the user interface while the
     * background computation is still executing.
     */
    protected void onProgressUpdate(Integer... messageStatus) {
        // we only pass one argument at max, always use the first element [0] from the 'varargs'
        super.onProgressUpdate(messageStatus[0]);

        // get last element in our ListView
        /*int lastIndex = this.messageList.getCount() - 1;
        LinearLayout listItem = (LinearLayout) this.messageList.getChildAt(lastIndex);

        if(listItem != null) {
            RelativeLayout listRelative = (RelativeLayout) listItem.getChildAt(0);
            ImageView statusIndicator = (ImageView) listRelative.getChildAt(1);

            // update the view element according to the messageStatus
            switch(messageStatus[0]) {
                case MESSAGE_SENT:
                    // change drawable to the sent-indicator
                    statusIndicator.setImageResource(R.drawable.checkmark_24);

                    break;
                case MESSAGE_ERROR:
                    // change drawable to the error-indicator
                    statusIndicator.setImageResource(R.drawable.cancel_24);

                    break;
            }
        }*/

        Log.d(LOG_TAG, "Message progress: " + messageStatus[0]);
    }

    /*@SuppressWarnings("rawtypes")
    public Boolean isActivityRunning(Class activityClass) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(Integer.MAX_VALUE);

        for (ActivityManager.RunningTaskInfo task : tasks) {
            if (activityClass.getCanonicalName().equalsIgnoreCase(task.baseActivity.getClassName()))
                return true;
        }

        return false;
    }*/

}
