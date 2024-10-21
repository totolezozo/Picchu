package com.app.picchu;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Message> messages;
    private String currentUserEmail;


    // Initializes the adapter with a list of messages and the current user's email
    public MessageAdapter(List<Message> messages, String currentUserEmail) {
        this.messages = messages;
        this.currentUserEmail = currentUserEmail;
    }


    // Inflates the layout for individual message items and creates a view holder for it
    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_item, parent, false);
        return new MessageViewHolder(view);
    }


    // Binds the message data to the view holder and adjusts the alignment and appearance
    // depending on whether the message was sent by the current user or received
    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messages.get(position);

        if (message.getSender().equals(currentUserEmail)) {
            holder.messageContainer.setGravity(Gravity.END);
            //holder.messageText.setBackgroundResource(R.drawable.sent_message_background); // Customize background if needed
        } else {
            holder.messageContainer.setGravity(Gravity.START);
            //holder.messageText.setBackgroundResource(R.drawable.received_message_background); // Customize background if needed
        }

        holder.messageText.setText(message.getMessage());
    }

    // Returns the total number of messages to display
    @Override
    public int getItemCount() {
        return messages.size();
    }


    // Holds references to the views within each message item (message text and container for layout adjustments)
    public class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView messageText;
        public LinearLayout messageContainer;

        public MessageViewHolder(View itemView) {
            super(itemView);

            messageText = itemView.findViewById(R.id.messageText);
            messageContainer = itemView.findViewById(R.id.messageContainer);
        }
    }

}
