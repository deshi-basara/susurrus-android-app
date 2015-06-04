package rocks.susurrus.susurrus.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import rocks.susurrus.susurrus.R;
import rocks.susurrus.susurrus.chat.models.MessageModel;
import rocks.susurrus.susurrus.models.RoomModel;

/**
 * Created by simon on 04.06.15.
 */
public class RoomAdapter extends ArrayAdapter<RoomModel> {
    private static final String LOG_TAG = "RoomAdapter";

    private ArrayList<RoomModel> roomList = new ArrayList<RoomModel>();

    /**
     * Class constructor.
     * Sets a reference to the activity context calling the adapter and the desired layout-xml of a
     * single room item.
     * @param context Activity context calling the adapter.
     * @param singleRoomResourceId ResourceId of a single message's layout.
     */
    public RoomAdapter(Context context, int singleRoomResourceId) {
        super(context, singleRoomResourceId);
    }

    /**
     * Adds a new room item to the roomList.
     * @param room One room model
     */
    public void add(RoomModel room) {
        Log.d(LOG_TAG, "Adding room");
        roomList.add(room);
        super.add(room);
    }

    /**
     * Returns a specific roomModel identified by its id in the roomList.
     * @param index
     * @return
     */
    public RoomModel getItem(int index) {
        return this.roomList.get(index);
    }

    @Override
    /**
     * Describes the translation between the data item and the View to display.
     * Returns the actual view used as a row within the ListView at a particular position.
     */
    public View getView(int position, View convertView, ViewGroup parent) {
        // which message should be inserted
        RoomModel room = getItem(position);

        // check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.activity_main_room,
                    parent, false);
        }

        // lookup views for data population
        TextView roomName = (TextView) convertView.findViewById(R.id.main_room_name);
        TextView roomOwner = (TextView) convertView.findViewById(R.id.main_room_owner);
        TextView roomCategory = (TextView) convertView.findViewById(R.id.main_room_category);
        ImageView roomEncrypted = (ImageView) convertView.findViewById(R.id.main_room_lock_img);
        TextView userCount = (TextView) convertView.findViewById(R.id.main_room_users_count);

        // populate the data into the template view using the data object
        roomName.setText(room.getRoomName());
        roomOwner.setText(room.getOwnerName());
        roomCategory.setText(room.getRoomCategory());
        userCount.setText("4");

        // no password needed, hide lock
        if(!room.isRoomEncrypted()) {
            roomEncrypted.setVisibility(View.INVISIBLE);
        }

        // return the completed view to render on screen
        return convertView;
    }
}
