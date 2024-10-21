package com.app.picchu;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder> {

    private List<Conversation> conversations;
    private String currentUserEmail;
    private Context context;


    // Initializes the adapter with a list of conversations, the current user's email, and the context for starting new activities
    public ConversationAdapter(List<Conversation> conversations, String currentUserEmail, Context context) {
        this.conversations = conversations;
        this.currentUserEmail = currentUserEmail;
        this.context = context;
    }


    // Inflates the layout for individual conversation items and creates a view holder for it
    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.conversation_item, parent, false);
        return new ConversationViewHolder(view);
    }


    // Binds the conversation data (friend name and last message time) to the view holder and sets an onClick listener to open the conversation
    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        Conversation conversation = conversations.get(position);

        holder.friendNameTextView.setText(conversation.getSenderOrReceiver(currentUserEmail));

        Timestamp timestamp = conversation.getTimestamp();
        if (timestamp != null) {
            String formattedTimestamp = formatTimestamp(timestamp);
            holder.lastMessageTime.setText(formattedTimestamp);
        } else {
            holder.lastMessageTime.setText("Unknown time");
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ConversationActivity.class);
            intent.putExtra("friendName", conversation.getSenderOrReceiver(currentUserEmail));
            intent.putExtra("friendEmail", conversation.getSenderOrReceiver(currentUserEmail));

            context.startActivity(intent);
        });
    }


    // Formats the timestamp into a readable date and time format
    private String formatTimestamp(Timestamp timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
        return sdf.format(timestamp.toDate());
    }

    // Returns the total number of conversations to display
    @Override
    public int getItemCount() {
        return conversations.size();
    }


    // Holds references to the views within each conversation item (friend name and last message time)
    public static class ConversationViewHolder extends RecyclerView.ViewHolder {
        public TextView friendNameTextView;
        public TextView lastMessageTime;

        public ConversationViewHolder(@NonNull View itemView) {
            super(itemView);
            friendNameTextView = itemView.findViewById(R.id.friendUsername);
            lastMessageTime = itemView.findViewById(R.id.lastMessageTime);
        }
    }

}

