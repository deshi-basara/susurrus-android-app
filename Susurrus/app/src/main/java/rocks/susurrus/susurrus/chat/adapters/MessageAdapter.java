package rocks.susurrus.susurrus.chat.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import rocks.susurrus.susurrus.R;
import rocks.susurrus.susurrus.chat.models.MessageModel;

/**
 * Adapter between messageModel and messageView.
 */
public class MessageAdapter extends ArrayAdapter<MessageModel> {
    private TextView chatText;

    private List<MessageModel> messageList = new ArrayList<>();


    private LinearLayout singleMessageContainer;

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
        View row = convertView;
        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.activity_chat_message, parent, false);
        }
        singleMessageContainer = (LinearLayout) row.findViewById(R.id.single_message_container);
        MessageModel chatMessageObj = getItem(position);
        chatText = (TextView) row.findViewById(R.id.single_message_content);
        chatText.setText(chatMessageObj.message);
        //chatText.setBackgroundResource(chatMessageObj.left ? R.drawable.bubble_a : R.drawable.bubble_b);
        singleMessageContainer.setGravity(chatMessageObj.owner ? Gravity.LEFT : Gravity.RIGHT);
        return row;
    }

    public Bitmap decodeToBitmap(byte[] decodedByte) {
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
    }

}
