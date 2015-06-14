package rocks.susurrus.susurrus.chat.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import rocks.susurrus.susurrus.R;
import rocks.susurrus.susurrus.chat.models.MessageModel;

/**
 * Adapter between messageModel and messageView.
 */
public class MessageAdapter extends ArrayAdapter<MessageModel> {
    private static final String LOG_TAG = "MessageAdapter";

    private List<MessageModel> messageList = new ArrayList<MessageModel>();
    private List<ImageView> rowsList = new ArrayList<>();

    /**
     * Class constructor.
     * Sets a reference to the activity context calling the adapter and the desired layout-xml of a
     * single message.
     * @param context Activity context calling the adapter.
     * @param singleMessageResourceId ResourceId of a single message's layout.
     */
    public MessageAdapter(Context context, int singleMessageResourceId) {
        super(context, singleMessageResourceId);
    }

    /**
     * Adds a new message item to the messageList.
     * @param message
     */
    public void add(MessageModel message) {
        Log.d(LOG_TAG, "Adding message: " + message.isOwner());
        messageList.add(message);
        super.add(message);
    }

    /**
     * Returns the current message count overall.
     * @return
     */
    public int getMessageCount() {
        return this.messageList.size();
    }

    /**
     * Returns a specific messageModel identified by its id in the messageList.
     * @param index
     * @return
     */
    public MessageModel getItem(int index) {
        return this.messageList.get(index);
    }

    @Override
    /**
     * Describes the translation between the data item and the View to display.
     * Returns the actual view used as a row within the ListView at a particular position.
     */
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.d(LOG_TAG, "MessagePosition: " + position);

        // which message should be inserted
        MessageModel chatMessage = getItem(position);

        Log.d(LOG_TAG, "MessageContent: " + chatMessage.getMessage());


        // get all available layouts
        LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);

        // message of the owner?
        Log.d(LOG_TAG, "MessageOwner: " + chatMessage.isOwner());
        View row = null;
        if(chatMessage.isOwner()) {
            // position message on the right
            row = inflater.inflate(R.layout.activity_chat_message_right, parent, false);
        }
        else {
            // not the owner, position message on the left
            row = inflater.inflate(R.layout.activity_chat_message_left, parent, false);

            // set username
            TextView usernameText = (TextView) row.findViewById(R.id.single_message_username);
            usernameText.setText(chatMessage.getOwnerName());
        }

        // lookup view and populate it
        TextView chatText = (TextView) row.findViewById(R.id.single_message_content);
        chatText.setText(chatMessage.getMessage());

        // return the completed view to render on screen
        Log.d(LOG_TAG, "ListView item added");
        return row;
    }

    public Bitmap decodeToBitmap(byte[] decodedByte) {
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
    }
}
