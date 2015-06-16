package rocks.susurrus.susurrus;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.afollestad.materialdialogs.MaterialDialog;

import rocks.susurrus.susurrus.chat.adapters.MessageAdapter;
import rocks.susurrus.susurrus.chat.models.MessageModel;
import rocks.susurrus.susurrus.models.RoomModel;
import rocks.susurrus.susurrus.network.WifiDirectBroadcastReceiver;
import rocks.susurrus.susurrus.services.MasterService;
import rocks.susurrus.susurrus.services.WifiDirectService;
import rocks.susurrus.susurrus.tasks.ClientDistributionTask;
import rocks.susurrus.susurrus.tasks.ServerDistributionTask;
import rocks.susurrus.susurrus.utils.Settings;


public class ChatActivity extends ActionBarActivity {
    private static final String LOG_TAG = "ChatActivity";

    /**
     * Threads/Handler/Services
     */
    WifiDirectService wifiDirectService;
    Intent wifiDirectIntent;
    boolean isWifiDirectServiceBound = false;
    MasterService masterService;
    Intent chatService;
    boolean isChatServiceBound = false;

    /**
     * Data
     */
    private MessageAdapter messageAdapter;
    private RoomModel currentRoom;
    private boolean isMasterNode;
    private String userName;

    /**
     * Views
     */
    private ListView messageListView;
    private EditText messageInputText;
    private ImageButton messageSendButton;
    private DrawerLayout chatDrawerLayout;
    private ActionBarDrawerToggle chatDrawerToggle;
    private boolean chatDrawerOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        setView();
        setAction();

        // start service for receiving messages
        //startService(new Intent(this, ChatService.class));

        setupData();
        setupServices();

        Log.d(LOG_TAG, "ChatActivity created.");
    }

    private void setupData() {
        // get needed data from intent
        this.currentRoom = (RoomModel) getIntent().getSerializableExtra("ROOM_MODEL");

        // get if the current client is the network's MasterNode
        WifiDirectBroadcastReceiver wifiDirectReceiver = WifiDirectBroadcastReceiver.getInstance();
        this.isMasterNode = wifiDirectReceiver.isMaster();

        // get username
        SharedPreferences settings = getSharedPreferences(Settings.PREF_ID, 0);
        userName = settings.getString(Settings.PREF_USER_NAME, "Anonymous");
    }

    private void setupServices() {
        // start and bind the wifiDirectService
        this.wifiDirectIntent = new Intent(this, WifiDirectService.class);
        startService(this.wifiDirectIntent);
        bindService(this.wifiDirectIntent, wifiDirectConnection, Context.BIND_AUTO_CREATE);

        // is our user the MasterNode of the whole network?
        if(this.isMasterNode) {
            // start auth- and receive-thread
            setupMaster();
        }
        else {
            // start only the receive-thread
            setupSlave();
        }
    }

    private void setupMaster() {
        Log.d(LOG_TAG, "Setup Master");

        this.chatService = new Intent(this, MasterService.class);
        this.chatService.putExtra("ROOM_MODEL", this.currentRoom);
        startService(this.chatService);
        bindService(this.chatService, masterConnection, Context.BIND_AUTO_CREATE);
    }

    private void setupSlave() {
        Log.d(LOG_TAG, "Setup Slave");

        this.chatService = new Intent(this, MasterService.class);
        this.chatService.putExtra("ROOM_MODEL", this.currentRoom);
        startService(this.chatService);
        bindService(this.chatService, slaveConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat, menu);

        /*menu.findItem(R.id.action_search).setIcon(
                new IconDrawable(this, Iconify.IconValue.fa_share)
                        .colorRes(R.color.abc_primary_text_disable_only_material_dark)
                        .actionBarSize());*/
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // drawer toggle requested
        if(id == R.id.action_list) {

            // drawer not open?
            if(!chatDrawerOpen) {
                // open it
                chatDrawerLayout.openDrawer(Gravity.RIGHT);
                chatDrawerOpen = true;
            }
            else {
                // close it
                chatDrawerLayout.closeDrawer(Gravity.RIGHT);
                chatDrawerOpen = false;
            }

            return true;
        }
        // back button
        else if(id == android.R.id.home) {
            showExitWarning();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    /**
     * Is executed, when the Activity is destroyed
     */
    protected void onDestroy() {
        super.onDestroy();

        // destroy the local room-service, if masterNode
        if(isMasterNode) {
            wifiDirectService.removeLocalService();
        }

        // unbind & destroy the chatService
        if(this.chatService != null && this.isChatServiceBound) {
            Log.d(LOG_TAG, "Unbinding: chatService [ServiceConnection]");
            if(isMasterNode) {
                unbindService(this.masterConnection);
            }
            else {
                unbindService(this.slaveConnection);
            }

            Log.d(LOG_TAG, "Stopping: chatService [Service]");
            stopService(this.chatService);
        }

        // unbind the wifiDirectService
        if(this.wifiDirectService != null && this.isWifiDirectServiceBound) {
            Log.d(LOG_TAG, "Unbinding: wifiDirectService [ServiceConnection]");
            unbindService(this.wifiDirectConnection);
        }

        Log.d(LOG_TAG, "ChatActivity destroyed.");
    }

    @Override
    /**
     * Is executed whenever the back-button on an android-keyboard was touched.
     */
    public void onBackPressed() {
        Log.d(LOG_TAG, "OnBackPressed");

        showExitWarning();

        //super.onBackPressed();
    }

    /**
     * Sets the actionBar's title accordingy to the handed chatRoom name.
     */
    private void setAction() {
        // get intent and title value
        Intent intent = getIntent();
        String actionTitle = intent.getStringExtra("ROOM_NAME");

        // setup actionBar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(actionTitle);
        actionBar.setSubtitle("1 Teilnehmer");
        //@todo R.string

        // setup backButton
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

    }

    /**
     * Gets all needed layout views, sets adapters and adds events if needed.
     */
    private void setView() {
        // get views
        messageListView = (ListView) findViewById(R.id.message_list_view);
        messageSendButton = (ImageButton) findViewById(R.id.message_send_button);
        messageInputText = (EditText) findViewById(R.id.message_input_text);
        chatDrawerLayout = (DrawerLayout) findViewById(R.id.chat_drawer_layout);

        // set adapters
        messageAdapter = new MessageAdapter(getApplicationContext(),
                R.layout.activity_chat_message_left);
        messageListView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        messageListView.setAdapter(messageAdapter);

        // initiate drawer
        chatDrawerToggle = new ActionBarDrawerToggle(this, chatDrawerLayout,
                null, R.string.chat_drawer_open, R.string.chat_drawer_close) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);

                chatDrawerOpen = false;
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

                chatDrawerOpen = true;
            }
        };

        // set events
        messageInputText.setOnKeyListener(messageInputTextListener);
        messageSendButton.setOnClickListener(messageSendButtonListener);
        chatDrawerLayout.setDrawerListener(chatDrawerToggle);
    }

    /**
     * Shows a warning-dialog on the screen, when a back-button was touched.
     */
    private void showExitWarning() {

        // warn masterNode from killing the chat room
        if(isMasterNode) {
            new MaterialDialog.Builder(this)
                    .title(R.string.chat_warn_exit_title)
                    .content(R.string.chat_warn_exit_content_master)
                    .positiveText(R.string.chat_warn_exit_proceed)
                    .negativeText(R.string.chat_warn_exit_cancel)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            NavUtils.navigateUpFromSameTask(ChatActivity.this);
                        }
                    })
                    .show();

        }
        // warn clientNode from closing the chat room
        else {
            new MaterialDialog.Builder(this)
                    .title(R.string.chat_warn_exit_title)
                    .content(R.string.chat_warn_exit_content_slave)
                    .positiveText(R.string.chat_warn_exit_proceed)
                    .negativeText(R.string.chat_warn_exit_cancel)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            NavUtils.navigateUpFromSameTask(ChatActivity.this);
                        }
                    })
                    .show();
        }
    }

    /**
     * Fetches a new message from the chat-input, resets the chat-input and creates a new
     * MessageModel that is returned.
     * @return MessageModel
     */
    private MessageModel getMessageFromInput() {
        // reset input text field
        String messageText = messageInputText.getText().toString();
        messageInputText.setText("");

        MessageModel newMessage = new MessageModel(true, this.userName, messageText);
        return newMessage;
    }

    /**
     * Adds a new message to the messageAdapter.
     * @param newMessage MessageModel that should be added.
     */
    private void addMessage(MessageModel newMessage) {
        // add message to the adapter
        messageAdapter.add(newMessage);
    }

    /**
     * Starts the distribution of a message inside the wifiDirect Network.
     * If the user is a normal client:  Send message to the socket-server (owner), which distributes
     *                                  the message.
     * If the user is the room owner:   Send message directly to all connected peers.
     * @param message
     */
    private void distributeMessage(MessageModel message) {
        Log.d(LOG_TAG, "Distributing message ...");

        // get an instance of the wifiDirectReceiver
        WifiDirectBroadcastReceiver wifiDirectReceiver = WifiDirectBroadcastReceiver.getInstance();

        // is the current user the server owner or just a client?
        if(this.isMasterNode) {
            // server owner, create a server-task for distributing the message directly to all
            // connected clients.
            Log.d(LOG_TAG, "... with a server-distributing-task ...");

            new ServerDistributionTask(ChatActivity.this)
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, message);
        }
        else {
            // not server owner, create a client-task for receiving messages
            Log.d(LOG_TAG, "... with a client-distributing-task ...");

            if(this.messageListView == null) {
                Log.e(LOG_TAG, "DAVOR SCHON LEER");
            }

            new ClientDistributionTask(ChatActivity.this, wifiDirectReceiver.getMasterAddress(),
                    this.messageListView)
                        .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, message);
        }
    }

    /**
     * OnKeyListener for the messageInputText-View.
     */
    private View.OnKeyListener messageInputTextListener = new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            // send message if the user tabs the enter button
            if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                    (keyCode == KeyEvent.KEYCODE_ENTER)) {

                MessageModel inputMessage = getMessageFromInput();
                addMessage(inputMessage);
                distributeMessage(inputMessage);

                return true;
            }
            return false;
        }
    };

    /**
     * OnClickListener for the messageSendButton-View.
     */
    private View.OnClickListener messageSendButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            MessageModel inputMessage = getMessageFromInput();
            addMessage(inputMessage);
            distributeMessage(inputMessage);
        }
    };

    /**
     * Connection to the external MessengerService-process for MasterNode.
     */
    private ServiceConnection masterConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MasterService.InstanceBinder localBinder = (MasterService.InstanceBinder) service;
            masterService = localBinder.getService();
            masterService.setChatHandler(chatHandler);
            masterService.startChatThread();
            masterService.startAuthThread();
            isChatServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isChatServiceBound = false;
        }
    };

    /**
     * Connection to the external MessengerService-process for SlaveNodes.
     */
    private ServiceConnection slaveConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MasterService.InstanceBinder localBinder = (MasterService.InstanceBinder) service;
            masterService = localBinder.getService();
            masterService.setChatHandler(chatHandler);
            masterService.startChatThread();

            isChatServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) { isChatServiceBound = false;
        }
    };

    /**
     * Connection to the external wifiDirectService-process.
     */
    private ServiceConnection wifiDirectConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            WifiDirectService.InstanceBinder localBinder = (WifiDirectService.InstanceBinder) service;
            wifiDirectService = localBinder.getService();
            isWifiDirectServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isWifiDirectServiceBound = false;
        }
    };

    /**
     * Attach handler object to the ui-thread for communication between the Receiver-thread
     * and the ChatActivity.
     */
    private Handler chatHandler = new Handler(Looper.getMainLooper()) {
        @Override
        /**
         * Is executed whenever the handler receives a new message from our chatService-
         * thread.
         */
        public void handleMessage(Message inputMessage) {
            MessageModel receivedMessage = (MessageModel) inputMessage.obj;

            // what kind of message did we receive?
            //@todo implement switch case

            addMessage(receivedMessage);

            // new message received, is the current user the master then he needs to distribute
            // the message inside the whole network
            if(isMasterNode) {
                Log.d(LOG_TAG, "Redistributing message");
                distributeMessage(receivedMessage);
            }
        }
    };
}
