package com.aelinstudios.sjcetalks.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aelinstudios.sjcetalks.Activities.ChatActivity;
import com.aelinstudios.sjcetalks.Models.User;
import com.aelinstudios.sjcetalks.R;
import com.aelinstudios.sjcetalks.databinding.RowConversationBinding;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {

    Context context;
    ArrayList<User> users;
    public UsersAdapter(Context context,ArrayList<User> users){
        this.context = context;
        this.users = users;


    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view= LayoutInflater.from(context).inflate(R.layout.row_conversation,parent,false);

        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);

        String senderId = FirebaseAuth.getInstance().getUid();

        String senderRoom = senderId + user.getUid();

        FirebaseDatabase.getInstance().getReference()
                .child("chats")
                .child(senderRoom)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if(snapshot.exists()){
                       String lastMsg = snapshot.child("lastMsg").getValue(String.class);
                      long time = snapshot.child("lastMsgTime").getValue(Long.class);

                            SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
                            holder.binding.time.setText(dateFormat.format(new Date(time)));
                       holder.binding.lastMessage.setText(lastMsg);


                        } else{
                            holder.binding.lastMessage.setText("Tap to Chat");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        //now we do the binding
        holder.binding.userName.setText(user.getName());

        //now using the glide library to load the dp
        Glide.with(context).load(user.getProfileImage())
                .placeholder(R.drawable.avatar)
                .into(holder.binding.imageView2);

        //now setting on click listener to chat screen
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("name",user.getName());
                intent.putExtra("image",user.getProfileImage());
                intent.putExtra("uid",user.getUid());
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
       return users.size();

    }

    public class UserViewHolder extends RecyclerView.ViewHolder {

        RowConversationBinding binding;
        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = RowConversationBinding.bind(itemView);
        }
    }
}
