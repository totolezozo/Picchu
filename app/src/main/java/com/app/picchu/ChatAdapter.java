package com.app.picchu;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<Message> messages;

    // Initializes the adapter with a list of messages to display in the chat
    public ChatAdapter(List<Message> messages) {
        this.messages = messages;
    }


    // Inflates the layout for individual chat items and creates a view holder for it
    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item, parent, false);
        return new ChatViewHolder(view);
    }

    // Binds the message data to the view holder, displaying the message text in the chat item
    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Message message = messages.get(position);
        holder.messageTextView.setText(message.getMessage());
    }

    // Returns the total number of messages in the chat
    @Override
    public int getItemCount() {
        return messages.size();
    }

    // Adds a new message to the chat and notifies the adapter to update the view
    public void addMessage(Message newMessage) {
        messages.add(newMessage);
        notifyItemInserted(messages.size() - 1);
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        public TextView messageTextView;

        // Holds references to the views within each chat item (message text in this case)
        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);

            messageTextView = itemView.findViewById(R.id.messageTextView);
        }
    }
}

