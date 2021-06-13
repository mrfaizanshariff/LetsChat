package com.aelinstudios.sjcetalks.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.aelinstudios.sjcetalks.Adapters.MessagesAdapter;
import com.aelinstudios.sjcetalks.Models.Message;
import com.aelinstudios.sjcetalks.R;
import com.aelinstudios.sjcetalks.databinding.ActivityChatBinding;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class ChatActivity extends AppCompatActivity {

    ActivityChatBinding binding;
    MessagesAdapter adapter;
    ArrayList<Message> messages;

    String senderRoom, receiverRoom;
    FirebaseDatabase database;
    FirebaseStorage storage;
    ProgressDialog dialog;
    String receiverUid;
    String senderUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        dialog = new ProgressDialog(this);
        dialog.setMessage("Uploading Image...");
        dialog.setCancelable(false);
        messages = new ArrayList<>();


        String name = getIntent().getStringExtra("name");
        String profile = getIntent().getStringExtra("image");


        binding.name.setText(name);
        Glide.with(ChatActivity.this).load(profile).placeholder(R.drawable.avatar).into(binding.dpimage);

        binding.imageView3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


       receiverUid = getIntent().getStringExtra("uid");
        senderUid = FirebaseAuth.getInstance().getUid();


        database.getReference().child("presence").child(receiverUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String status = snapshot.getValue(String.class);
                    if(!status.isEmpty()){
                        if(status.equals("Offline")){
                            binding.onlineStatus.setVisibility(View.GONE);
                        }else {
                            binding.onlineStatus.setText(status);
                            binding.onlineStatus.setVisibility(View.VISIBLE);
                        }
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        senderRoom = senderUid + receiverUid;
        receiverRoom =  receiverUid + senderUid;


        adapter = new MessagesAdapter(this,messages,senderRoom,receiverRoom);
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));



            database.getReference().child("chats")
                    .child(senderRoom)
                    .child("messages")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            messages.clear();
                            for(DataSnapshot snapshot1 : snapshot.getChildren()){
                                Message message = snapshot1.getValue(Message.class);
                                message.setMessageId(snapshot1.getKey());
                                messages.add(message);
                            }
                            adapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

        binding.sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msgTxt = binding.msgBox.getText().toString();

                Date date = new Date();
                Message message = new Message(msgTxt,senderUid,date.getTime());
                binding.msgBox.setText("");

                String randomKey = database.getReference().push().getKey();

                HashMap <String,Object> lastMsgobj = new HashMap<>();
                lastMsgobj.put("lastMsg", message.getMessage());
                lastMsgobj.put("lastMsgTime", date.getTime());
                //setting up last msg in the sender room and receiver room

                database.getReference().child("chats").child(senderRoom).updateChildren(lastMsgobj);
                database.getReference().child("chats").child(receiverRoom).updateChildren(lastMsgobj);

                database.getReference().child("chats")
                        .child(senderRoom)
                        .child("messages")
                        .child(randomKey)
                        .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        database.getReference().child("chats")
                                .child(receiverRoom)
                                .child("messages")
                                .child(randomKey)
                                .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                            }
                        });



                    }
                });
            }
        });


        binding.attachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,25);
            }
        });


        final Handler handler = new Handler();
        binding.msgBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                database.getReference().child("presence").child(senderUid).setValue("Typing..");
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(userStoppedTyping,1000);



            }
            Runnable userStoppedTyping = new Runnable() {
                @Override
                public void run() {
                    database.getReference().child("presence").child(senderUid).setValue("Online");
                }
            };
        });

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        //setting up name on the action bar
       //getSupportActionBar().setTitle(name);
        //setting up back button
       // getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==25){
            if(data!=null){
                if(data.getData()!=null){
                    Uri selectedImage = data.getData();
                    Calendar calendar= Calendar.getInstance();
                    StorageReference reference = storage.getReference().child("chats").child(calendar.getTimeInMillis()+"");
                    dialog.show();
                    reference.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            dialog.dismiss();
                            if(task.isSuccessful()){
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String filePath = uri.toString();


                                        String msgTxt = binding.msgBox.getText().toString();

                                        Date date = new Date();
                                        Message message = new Message(msgTxt,senderUid,date.getTime());
                                        message.setMessage("photo");
                                        message.setImageUrl(filePath);
                                        binding.msgBox.setText("");

                                        String randomKey = database.getReference().push().getKey();

                                        HashMap <String,Object> lastMsgobj = new HashMap<>();
                                        lastMsgobj.put("lastMsg", message.getMessage());
                                        lastMsgobj.put("lastMsgTime", date.getTime());
                                        //setting up last msg in the sender room and receiver room

                                        database.getReference().child("chats").child(senderRoom).updateChildren(lastMsgobj);
                                        database.getReference().child("chats").child(receiverRoom).updateChildren(lastMsgobj);

                                        database.getReference().child("chats")
                                                .child(senderRoom)
                                                .child("messages")
                                                .child(randomKey)
                                                .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                database.getReference().child("chats")
                                                        .child(receiverRoom)
                                                        .child("messages")
                                                        .child(randomKey)
                                                        .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {

                                                    }
                                                });



                                            }
                                        });

                                        Toast.makeText(ChatActivity.this,"Uploaded",Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                        }
                    });

                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        String currentId = FirebaseAuth.getInstance().getUid();
        database.getReference().child("presence").child(currentId).setValue("Online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        String currentId = FirebaseAuth.getInstance().getUid();
        database.getReference().child("presence").child(currentId).setValue("Offline");


    }

    @Override
    protected void onDestroy() {
        String currentId = FirebaseAuth.getInstance().getUid();
        database.getReference().child("presence").child(currentId).setValue("Offline");
        super.onDestroy();

    }

    // making the back button on action bar work
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}