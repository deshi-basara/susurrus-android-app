package rocks.susurrus.susurrus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import rocks.susurrus.susurrus.chat.ReceiverService;
import rocks.susurrus.susurrus.network.WiFiDirectBroadcastReceiver;


public class ChatActivity extends ActionBarActivity {

    private static final String LOG_TAG = "ChatActivity";

    // intent-filter for reacting on network changes
    private final IntentFilter mIntentFilter = new IntentFilter();
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private BroadcastReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        setLayout();

        // set filter-actions, that will later be handled by the WiFiDirectBroadcastReceiver
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        // get an instance of the WifiP2PManager
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        // register application with the WifiP2PManager
        mChannel = mManager.initialize(this, getMainLooper(), null);
        // instance of the broadcast receiver
        mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);

        // start service for receiving messages
        startService(new Intent(this, ReceiverService.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat, menu);
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
     * Get's all needed layout views and adds events if needed.
     */
    private void setLayout() {
        Button discoverPeers = (Button) findViewById(R.id.testButton);

        discoverPeers.setOnClickListener(new View.OnClickListener() {
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
        });
    }
}
