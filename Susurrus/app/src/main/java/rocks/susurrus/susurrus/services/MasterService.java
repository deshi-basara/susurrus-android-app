package rocks.susurrus.susurrus.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import rocks.susurrus.susurrus.models.RoomModel;
import rocks.susurrus.susurrus.threads.ServerAuthenticationThread;
import rocks.susurrus.susurrus.threads.ServerReceiveThread;

public class MasterService extends Service {
    final static String LOG_TAG = "MessengerService";

    /**
     * Networking
     */
    private Thread authThread;
    private Thread chatThread;
    private boolean isAuthenticated = false;
    private boolean hasChat = false;

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
        roomData = (RoomModel) intent.getSerializableExtra("ROOM_MODEL");

        // start the authentication socket
        //this.initiateAuthentication();

        // start the messaging socket
        //this.initiateChat();

        return START_STICKY;
    }

    /**
     * Starts thread that runs the authentication socket.
     */
    private void initiateAuthentication() {

        if(!isAuthenticated) {
            // create a thread, for authenticating with the service/room
            ServerAuthenticationThread auth = new ServerAuthenticationThread(
                    this.roomData);

            this.authThread = new Thread(auth);
            this.authThread.start();

            this.isAuthenticated = true;

            Log.d(LOG_TAG, "AuthenticationThread started.");
        }

    }

    /**
     * Starts thread that runs the messaging socket.
     */
    private void initiateChat() {

        if(!this.hasChat) {
            // create a thread, for messaging with the service/room
            ServerReceiveThread receive = new ServerReceiveThread(this.chatHandler);

            this.chatThread = new Thread(receive);
            this.chatThread.start();

            this.hasChat = true;

            Log.d(LOG_TAG, "AuthenticationThread started.");
        }
    }

    /**
     * Getter/Setter
     */
    public void setChatHandler(Handler handler) {
        Log.d(LOG_TAG, "ChatHandler set");

        this.chatHandler = handler;
    }

    public void startChatThread() {
        this.initiateChat();
    }

    public void startAuthThread() {
        this.initiateAuthentication();
    }
}
