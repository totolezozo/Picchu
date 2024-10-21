package com.app.picchu;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<String> userList;      // List of user IDs
    private List<String> usernameList;  // List of usernames


    // Initializes the adapter with a list of user IDs and corresponding usernames
    public UserAdapter(List<String> userList, List<String> usernameList) {
        this.userList = userList;
        this.usernameList = usernameList;
    }


    // Inflates the layout for individual user items and creates a view holder for it
    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false);
        return new UserViewHolder(view);
    }


    // Binds the username and user ID to the view holder and sets up an onClick listener
    // to open the profile of the selected user
    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        String userId = userList.get(position);
        String username = usernameList.get(position);

        holder.userNameTextView.setText(username);

        holder.userNameTextView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ProfileActivity.class);
            intent.putExtra("profileUserId", userId);
            v.getContext().startActivity(intent);
        });
    }


    // Returns the total number of users in the list
    @Override
    public int getItemCount() {
        return userList.size();
    }


    // Holds references to the views within each user item (username text view)
    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView userNameTextView;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userNameTextView = itemView.findViewById(R.id.user_name);
        }
    }
}
