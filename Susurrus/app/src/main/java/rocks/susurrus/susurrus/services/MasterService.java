package rocks.susurrus.susurrus.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import java.security.PublicKey;

import rocks.susurrus.susurrus.ChatActivity;
import rocks.susurrus.susurrus.MainActivity;
import rocks.susurrus.susurrus.R;
import rocks.susurrus.susurrus.models.RoomModel;
import rocks.susurrus.susurrus.threads.ServerAuthenticationThread;
import rocks.susurrus.susurrus.threads.ServerReceiveThread;
import rocks.susurrus.susurrus.utils.Settings;

public class MasterService extends Service {
    final static String LOG_TAG = "MessengerService";

    /**
     * Constants
     */
    public static final int NOTIFICATION_ID = 100;

    /**
     * Networking
     */
    private ServerAuthenticationThread authRunnable;
    private ServerReceiveThread receiveRunnable;
    private Thread authThread;
    private Thread chatThread;
    private boolean hasAuth = false;
    private boolean hasChat = false;
    private boolean hasIcon = false;

    /**
     * Handler
     */
    private Handler chatHandler;

    /**
     * Binder
     */
    private final IBinder binder = new InstanceBinder();
    public class InstanceBinder extends Binder {
        public MasterService getService() {
            return MasterService.this;
        }
    }

    /**
     * Data
     */
    private RoomModel roomData;

    @Override
    /**
     * Binder-Interface that clients use to communicate with the service.
     */
    public IBinder onBind(Intent intent) {
        return this.binder;
    }

    @Override
    /**
     * Is executed the first time the Service is started.
     */
    public int onStartCommand(Intent intent, int flags, int startId) {
        // get roomData via intent extras
        if(intent == null || intent.getAction() == null) {
            Log.e(LOG_TAG, "Empty intent");
        }
        else {
        }

        roomData = (RoomModel) intent.getSerializableExtra("ROOM_MODEL");

        Log.d(LOG_TAG, "MasterService created.");

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(this.hasAuth) {
            Log.d(LOG_TAG, "Stopping: authThread [Thread]");

            this.authRunnable.terminate();
            this.hasAuth = false;
        }
        if(this.hasChat) {
            Log.d(LOG_TAG, "Stopping: chatThread [Thread]");

            this.receiveRunnable.terminate();
            this.hasChat = false;
        }
        if(this.hasIcon) {
            Log.d(LOG_TAG, "Canceling: chatIcon [Notification]");

            NotificationManager notificationManager = (NotificationManager)
                    getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(MasterService.NOTIFICATION_ID);
        }

        Log.d(LOG_TAG, "MasterService destroyed.");
    }

    /**
     * Starts thread that runs the authentication socket.
     */
    private void initiateAuthentication() {

        if(!hasAuth) {
            // create a thread, for authenticating with the service/room
            this.authRunnable = new ServerAuthenticationThread(
                    this.roomData,
                    Settings.getInstance().getPublicKey()
            );

            this.authRunnable.start();

            //this.authThread = new Thread(this.authRunnable);
            //this.authThread.start();

            this.hasAuth = true;
        }

    }

    /**
     * Starts thread that runs the messaging socket.
     */
    private void initiateChat() {

        if(!this.hasChat) {
            // create a thread, for messaging with the service/room
            this.receiveRunnable = new ServerReceiveThread(this.chatHandler);

            this.receiveRunnable.start();

            //this.chatThread = new Thread(this.receiveRunnable);
            //this.chatThread.start();

            this.hasChat = true;
        }
    }

    /**
     * Opens a status-indicator-notification when the service is started.
     */
    private void initiateNotificationIcon() {

        if(!this.hasIcon) {

            // set MainActivity as notification action, which redirects to the last opened Activity
            Intent notificationIntent = new Intent(this, MainActivity.class);
            notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            notificationIntent.setAction(Intent.ACTION_MAIN);
            PendingIntent resultPendingIntent =
                    PendingIntent.getActivity(
                            this,
                            0,
                            notificationIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );

            // set a "closing-broadcast" as notification close action, which signals the broadcast-
            // listener to close the whole app
            Intent closingIntent = new Intent(this, MainActivity.class);
            closingIntent.putExtra("EXIT", true);
            closingIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent closePendingIntent =
                    PendingIntent.getActivity(
                            this,
                            0,
                            closingIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );

            // set notification content and open it
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            Notification notification  = new Notification.Builder(this)
                    .setContentTitle(getString(R.string.notify_running_title))
                    .setContentText(getString(R.string.notify_running_content))
                    .setSmallIcon(R.drawable.entering_heaven_alive_24)
                    .addAction(R.drawable.cancel_24, getString(R.string.notify_running_stop),
                            closePendingIntent)
                    .setContentIntent(resultPendingIntent)
                    .setOngoing(true)
                    .build();
            notificationManager.notify(this.NOTIFICATION_ID, notification);

            this.hasIcon = true;
        }

    }


    /**
     * Getter/Setter
     */
    public void setChatHandler(Handler handler) {
        this.chatHandler = handler;
    }

    public void startChatThread() {
        this.initiateChat();
    }

    public void startAuthThread() {
        this.initiateAuthentication();
    }
    public void startNotificationIcon() {
        this.initiateNotificationIcon();
    }
}
