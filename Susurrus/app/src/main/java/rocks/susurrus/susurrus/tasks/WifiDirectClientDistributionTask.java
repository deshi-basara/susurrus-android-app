package rocks.susurrus.susurrus.tasks;

import android.app.ActivityManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;

import rocks.susurrus.susurrus.ChatActivity;
import rocks.susurrus.susurrus.MainActivity;
import rocks.susurrus.susurrus.chat.models.MessageModel;
import rocks.susurrus.susurrus.network.WifiDirectLocalService;

/**
 * Created by simon on 06.06.15.
 */
public class WifiDirectClientDistributionTask extends AsyncTask<MessageModel, Integer, Boolean> {
    public static final String LOG_TAG = "ClientDistributionTask";

    public static final int MESSAGE_PREPARING = 1;
    public static final int MESSAGE_SENDING = 2;
    public static final int MESSAGE_SENT = 3;

    private Context context;
    private InetAddress serverAddress;


    public WifiDirectClientDistributionTask(Context c, InetAddress a) {
        this.context = c;
        this.serverAddress = a;
    }

    @Override
    /**
     * Executed when the AsyncTask is started.
     */
    protected Boolean doInBackground(MessageModel... messageModel) {
        Log.d(LOG_TAG, "Starting the messaging-distribution via server ...");

        Log.d(LOG_TAG, "Message: " + messageModel[0].getMessage());

        publishProgress(this.MESSAGE_PREPARING);

        try {
            // create a new socket client
            Socket client = new Socket();

            client.setReuseAddress(true);
            client.bind(null);

            // connect client to the server
            client.connect(new InetSocketAddress(this.serverAddress,
                    WifiDirectLocalService.SERVICE_PORT));

            publishProgress(this.MESSAGE_SENDING);

            Log.d(LOG_TAG, "Client successfully connected to " + this.serverAddress);

            // get the client's output stream and write the message to it
            OutputStream outputStream = client.getOutputStream();
            // we only pass one argument at max, always use the first element [0] from the 'varargs'
            new ObjectOutputStream(outputStream).writeObject(messageModel[0]);

            Log.d(LOG_TAG, "Client successfully sent his message to " + this.serverAddress);
            publishProgress(this.MESSAGE_SENT);
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

        Log.d(LOG_TAG, "Message progress: " + messageStatus[0]);

        /*if(isActivityRunning(MainActivity.class)){
            ChatActivity.refreshList(msg[0], true);
        }*/
    }

    @Override
    protected void onPostExecute(Boolean hasErrors) {
        Log.d(LOG_TAG, "onPostExecute");
        super.onPostExecute(hasErrors);
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
