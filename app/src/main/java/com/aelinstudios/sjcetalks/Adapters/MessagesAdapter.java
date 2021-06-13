package com.aelinstudios.sjcetalks.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aelinstudios.sjcetalks.Models.Message;
import com.aelinstudios.sjcetalks.R;
import com.aelinstudios.sjcetalks.databinding.DeleteDialogBinding;
import com.aelinstudios.sjcetalks.databinding.ItemRecieveBinding;
import com.aelinstudios.sjcetalks.databinding.ItemSentBinding;
import com.bumptech.glide.Glide;
import com.github.pgreze.reactions.ReactionPopup;
import com.github.pgreze.reactions.ReactionsConfig;
import com.github.pgreze.reactions.ReactionsConfigBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class MessagesAdapter  extends RecyclerView.Adapter{

    Context context;
    ArrayList<Message> messages;

    final int ITEM_SENT =1;
    final int ITEM_RECEIVE = 2;

    FirebaseDatabase database;
    String senderRoom;
    String receiverRoom;


    public MessagesAdapter(Context context,ArrayList<Message> messages, String senderRoom,String receiverRoom){
        this.context=context;
        this.messages = messages;
        this.senderRoom =senderRoom;
        this.receiverRoom = receiverRoom;

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if(viewType==ITEM_SENT){
            View view = LayoutInflater.from(context).inflate(R.layout.item_sent,parent,false);
            return  new SentViewHolder(view);
        } else {
            View view=LayoutInflater.from(context).inflate(R.layout.item_recieve,parent,false);
            return new ReceiverViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        if(FirebaseAuth.getInstance().getUid().equals(message.getSenderId())){
            return ITEM_SENT;
        } else {
            return ITEM_RECEIVE;
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        Message message = messages.get(position);

        int reactions[]=new int[]{
                R.drawable.ic_fb_like,
                R.drawable.ic_fb_love,
                R.drawable.ic_fb_laugh,
                R.drawable.ic_fb_wow,
                R.drawable.ic_fb_sad,
                R.drawable.ic_fb_angry
        };

        ReactionsConfig config = new ReactionsConfigBuilder(context)
                .withReactions(reactions)
                .build();

        ReactionPopup popup = new ReactionPopup(context, config, (pos) -> {
            if(holder.getClass() == SentViewHolder.class){
                SentViewHolder sentViewHolder = (SentViewHolder) holder;
                sentViewHolder.binding.feelingsent.setImageResource(reactions[pos]);
                sentViewHolder.binding.feelingsent.setVisibility(View.VISIBLE);
            } else {
                ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;
                viewHolder.binding.feelingsreceive.setImageResource(reactions[pos]);
                viewHolder.binding.feelingsreceive.setVisibility(View.VISIBLE);
            }

            message.setFeeling(pos);

            database=FirebaseDatabase.getInstance();

            database.getReference()
                    .child("chats")
                    .child(senderRoom)
                    .child("messages")
                    .child(message.getMessageId()).setValue(message);

            database.getReference()
                    .child("chats")
                    .child(receiverRoom)
                    .child("messages")
                    .child(message.getMessageId()).setValue(message);

            return true; // true is closing popup, false is requesting a new selection
        });




        if(holder.getClass() == SentViewHolder.class){
            SentViewHolder sentViewHolder = (SentViewHolder) holder;

            if(message.getMessage().equals("photo")){
                sentViewHolder.binding.imagesend.setVisibility(View.VISIBLE);
                sentViewHolder.binding.msgSent.setVisibility(View.GONE);
                Glide.with(context).load(message.getImageUrl())
                        .placeholder(R.drawable.placeholder)
                        .into(sentViewHolder.binding.imagesend);
            }
            sentViewHolder.binding.msgSent.setText(message.getMessage());


            if(message.getFeeling() >= 0){
                sentViewHolder.binding.feelingsent.setImageResource(reactions[message.getFeeling()]);
                sentViewHolder.binding.feelingsent.setVisibility(View.VISIBLE);
            }else {
                sentViewHolder.binding.feelingsent.setVisibility(View.GONE);
            }

            sentViewHolder.binding.msgSent.setOnTouchListener(new View.OnTouchListener() {

                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {

                    popup.onTouch(view,motionEvent);
                    return false;
                }
            });
            sentViewHolder.binding.imagesend.setOnTouchListener(new View.OnTouchListener() {

                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {

                        popup.onTouch(view,motionEvent);

                    return false;
                }
            });


            sentViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    View view = LayoutInflater.from(context).inflate(R.layout.delete_dialog, null);
                    DeleteDialogBinding binding = DeleteDialogBinding.bind(view);
                    AlertDialog dialog = new AlertDialog.Builder(context)
                            .setTitle("Delete Message")
                            .setView(binding.getRoot())
                            .create();

                    binding.everyone.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            message.setMessage("This message was removed.");
                            message.setFeeling(-1);
                            FirebaseDatabase.getInstance().getReference()
                                    .child("chats")
                                    .child(senderRoom)
                                    .child("messages")
                                    .child(message.getMessageId()).setValue(message);

                            FirebaseDatabase.getInstance().getReference()
                                    .child("chats")
                                    .child(receiverRoom)
                                    .child("messages")
                                    .child(message.getMessageId()).setValue(message);
                            dialog.dismiss();
                        }
                    });

                    binding.delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            FirebaseDatabase.getInstance().getReference()
                                    .child("chats")
                                    .child(senderRoom)
                                    .child("messages")
                                    .child(message.getMessageId()).setValue(null);
                            dialog.dismiss();
                        }
                    });

                    binding.cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

                    dialog.show();

                    return false;
                }
            });

        }
        else {
            ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;
            if(message.getMessage().equals("photo")){
                viewHolder.binding.imagereceive.setVisibility(View.VISIBLE);
                viewHolder.binding.msgReciver.setVisibility(View.GONE);
                Glide.with(context).load(message.getImageUrl())
                        .placeholder(R.drawable.placeholder)
                        .into(viewHolder.binding.imagereceive);
            }
            viewHolder.binding.msgReciver.setText(message.getMessage());

            if(message.getFeeling() >= 0){
                viewHolder.binding.feelingsreceive.setImageResource(reactions[message.getFeeling()]);
                viewHolder.binding.feelingsreceive.setVisibility(View.VISIBLE);
            }else {
                viewHolder.binding.feelingsreceive.setVisibility(View.GONE);
            }

            viewHolder.binding.msgReciver.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    popup.onTouch(view,motionEvent);
                    return false;
                }
            });

            viewHolder.binding.imagereceive.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    popup.onTouch(view,motionEvent);
                    return false;
                }
            });

            viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    View view = LayoutInflater.from(context).inflate(R.layout.delete_dialog, null);
                    DeleteDialogBinding binding = DeleteDialogBinding.bind(view);
                    AlertDialog dialog = new AlertDialog.Builder(context)
                            .setTitle("Delete Message")
                            .setView(binding.getRoot())
                            .create();

                    binding.everyone.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            message.setMessage("This message was removed.");
                            message.setFeeling(-1);
                            FirebaseDatabase.getInstance().getReference()
                                    .child("chats")
                                    .child(senderRoom)
                                    .child("messages")
                                    .child(message.getMessageId()).setValue(message);

                            FirebaseDatabase.getInstance().getReference()
                                    .child("chats")
                                    .child(receiverRoom)
                                    .child("messages")
                                    .child(message.getMessageId()).setValue(message);
                            dialog.dismiss();
                        }
                    });

                    binding.delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            FirebaseDatabase.getInstance().getReference()
                                    .child("chats")
                                    .child(senderRoom)
                                    .child("messages")
                                    .child(message.getMessageId()).setValue(null);
                            dialog.dismiss();
                        }
                    });

                    binding.cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

                    dialog.show();

                    return false;
                }
            });




        }

    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    //View Holder For sender recyclerView
    public class SentViewHolder extends RecyclerView.ViewHolder{

        ItemSentBinding binding;

        public SentViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemSentBinding.bind(itemView);

        }
    }
    //View Holder For receiver

    public class ReceiverViewHolder extends RecyclerView.ViewHolder{

        ItemRecieveBinding binding;

        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemRecieveBinding.bind(itemView);
        }
    }


}
