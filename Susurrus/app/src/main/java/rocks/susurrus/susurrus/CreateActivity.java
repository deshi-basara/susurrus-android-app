package rocks.susurrus.susurrus;

import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.HashMap;
import java.util.Map;

import rocks.susurrus.susurrus.network.WiFiDirectBroadcastReceiver;


public class CreateActivity extends ActionBarActivity {
    private static final String LOG_TAG = "CreateActivity";

    private final int SERVER_PORT = 4440;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        registerWifiRoom();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_create, menu);
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

    private void registerWifiRoom() {
        Log.d(LOG_TAG, "Start registering new wifi room ...");

        //  Create a string map containing information about the room.
        Map record = new HashMap();
        record.put("listenport", String.valueOf(SERVER_PORT));
        record.put("buddyname", "John Doe" + (int) (Math.random() * 1000));
        record.put("available", "visible");

        WiFiDirectBroadcastReceiver mReceiver = WiFiDirectBroadcastReceiver.getInstance();
        mReceiver.createNewRoom(record);

        Log.d(LOG_TAG, "... registered.");
    }
}
