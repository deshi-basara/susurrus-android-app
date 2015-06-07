package rocks.susurrus.susurrus;

import android.app.Notification;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
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

import rocks.susurrus.susurrus.chat.adapters.MessageAdapter;
import rocks.susurrus.susurrus.chat.models.MessageModel;
import rocks.susurrus.susurrus.models.RoomModel;
import rocks.susurrus.susurrus.network.WifiDirectBroadcastReceiver;
import rocks.susurrus.susurrus.tasks.ClientDistributionTask;


public class ChatActivity extends ActionBarActivity {
    private static final String LOG_TAG = "ChatActivity";

    /**
     * Data
     */
    private MessageAdapter messageAdapter;
    private RoomModel currentRoom;

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
    }

    private void setupData() {
        // get needed data from intent
        this.currentRoom = (RoomModel) getIntent().getSerializableExtra("ROOM_MODEL");
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
        if (id == R.id.action_list) {

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

        return super.onOptionsItemSelected(item);
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
        actionBar.setSubtitle("4 Teilnehmer");

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
     * Adds a new message to the messageAdapter and resets the messageInputText.
     * @return True, if message was added successfully.
     */
    private Boolean addNewMessage() {
        // reset input text field
        String messageText = messageInputText.getText().toString();
        messageInputText.setText("");

        Log.d(LOG_TAG, "New message: " + messageText);

        // add message to the adapter
        MessageModel newMessage = new MessageModel(true, "Cooler Benutzer", messageText);
        messageAdapter.add(newMessage);

        // send/broadcast message
        distributeNewMessage(newMessage);

        return true;
    }

    /**
     * Starts the distribution of a message inside the wifiDirect Network.
     * If the user is a normal client:  Send message to the socket-server (owner), which distributes
     *                                  the message.
     * If the user is the room owner:   Send message directly to all connected peers.
     * @param message
     */
    private void distributeNewMessage(MessageModel message) {
        Log.d(LOG_TAG, "Distributing message ...");

        WifiDirectBroadcastReceiver wifiDirectReceiver = WifiDirectBroadcastReceiver.getInstance();

        // is the current user the server owner or just a client?
        if(wifiDirectReceiver.isMaster()) {
            // server owner, create a server-task for distributing the message directly to all
            // connected clients.
            Log.d(LOG_TAG, "... with a server-distributing-task ...");
        }
        else {
            // not server owner, create a client-task for receiving messages
            Log.d(LOG_TAG, "... with a client-distributing-task ...");

            new ClientDistributionTask(ChatActivity.this, wifiDirectReceiver.getMasterAddress())
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
                Log.d(LOG_TAG, "messageInputTextListener");
                return addNewMessage();
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
            Log.d(LOG_TAG, "messageSendButtonListener");
            addNewMessage();
        }
    };
}
