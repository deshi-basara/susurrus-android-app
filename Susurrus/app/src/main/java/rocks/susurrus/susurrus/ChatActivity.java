package rocks.susurrus.susurrus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import rocks.susurrus.susurrus.chat.ReceiverService;
import rocks.susurrus.susurrus.chat.adapters.MessageAdapter;
import rocks.susurrus.susurrus.chat.models.MessageModel;
import rocks.susurrus.susurrus.network.WiFiDirectBroadcastReceiver;


public class ChatActivity extends ActionBarActivity {
    private static final String LOG_TAG = "ChatActivity";

    // intent-filter for reacting on network changes
    private final IntentFilter mIntentFilter = new IntentFilter();
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private WiFiDirectBroadcastReceiver mReceiver;

    // chat
    private MessageAdapter messageAdapter;

    // view
    private ListView messageListView;
    private EditText messageInputText;
    private Button messageSendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        setView();

        // set filter-actions, that will later be handled by the WiFiDirectBroadcastReceiver
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        // get an instance of the WifiP2PManager
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        // register application with the WifiP2PManager
        mChannel = mManager.initialize(this, getMainLooper(), null);
        // get an instance of the broadcast receiver and set needed data
        mReceiver = WiFiDirectBroadcastReceiver.getInstance();
        mReceiver.setWifiManager(mManager);
        mReceiver.setWifiChannel(mChannel);
        mReceiver.setActivity(this);

        // start service for receiving messages
        startService(new Intent(this, ReceiverService.class));
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /* register the broadcast receiver with the intent values to be matched */
    @Override
    protected void onResume() {
        super.onResume();
        // register our broadcast receiver, which is called when an intentFilter matches
        registerReceiver(mReceiver, mIntentFilter);
    }
    /* unregister the broadcast receiver */
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    /**
     * Gets all needed layout views, sets adapters and adds events if needed.
     */
    private void setView() {
        // get views
        messageListView = (ListView) findViewById(R.id.message_list_view);
        messageSendButton = (Button) findViewById(R.id.message_send_button);
        messageInputText = (EditText) findViewById(R.id.message_input_text);

        // set adapters
        messageAdapter = new MessageAdapter(getApplicationContext(),
                R.layout.activity_chat_message_left);
        messageListView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        messageListView.setAdapter(messageAdapter);

        // set events
        messageInputText.setOnKeyListener(messageInputTextListener);
        messageSendButton.setOnClickListener(messageSendButtonListener);

        /*discoverPeers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(LOG_TAG, "Finding peers ...");

                mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(LOG_TAG, "... peers found.");
                    }

                    @Override
                    public void onFailure(int reasonCode) {
                        Log.d(LOG_TAG, "peers error: " + reasonCode + ".");
                    }
                });
            }
        });*/
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
        MessageModel newMessage = new MessageModel(true, messageText);
        messageAdapter.add(newMessage);

        return true;
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
