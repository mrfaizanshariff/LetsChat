package com.aelinstudios.sjcetalks.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import com.aelinstudios.sjcetalks.Models.User;
import com.aelinstudios.sjcetalks.databinding.ActivitySetupProfileBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class SetupProfileActivity extends AppCompatActivity {

    protected ActivitySetupProfileBinding binding;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    FirebaseStorage firebaseStorage;
    Uri selectedImage;
    ProgressDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivitySetupProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        dialog = new ProgressDialog(this);
        dialog.setMessage("Setting Up Profile..");
        dialog.setCancelable(false);

        //setting up firebase auth,database,storage
        firebaseAuth=FirebaseAuth.getInstance();
        firebaseDatabase=FirebaseDatabase.getInstance();
        firebaseStorage=FirebaseStorage.getInstance();
        //setting up onClick Listener for image selection
        binding.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,100);
            }
        });

        binding.profilebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = binding.profilebox.getText().toString();
                if(name.isEmpty()){
                    binding.profilebox.setError("Please Enter The Name");
                    return;
                }
                dialog.show();

                if(selectedImage!=null){
                    StorageReference reference= firebaseStorage.getReference().child("Profiles").child(firebaseAuth.getUid());
                    reference.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if(task.isSuccessful()){
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String imageUrl = uri.toString();

                                        String uid = firebaseAuth.getUid();
                                        String phone = firebaseAuth.getCurrentUser().getPhoneNumber();
                                        String name = binding.profilebox.getText().toString();

                                        User user = new User(uid,name,phone,imageUrl);

                                        firebaseDatabase.getReference().child("Users")
                                                .child(uid)
                                                .setValue(user)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        dialog.dismiss();
                                                        Intent intent = new Intent(SetupProfileActivity.this,MainActivity.class);
                                                        startActivity(intent);
                                                        finish();

                                                    }
                                                });

                                    }
                                });
                            }

                                            }
                                        });
                            }else {
                    String uid = firebaseAuth.getUid();
                    String phone = firebaseAuth.getCurrentUser().getPhoneNumber();


                    User user = new User(uid,name,phone,"No image ");

                    firebaseDatabase.getReference().child("Users")
                            .child(uid)
                            .setValue(user)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    dialog.dismiss();
                                    Intent intent = new Intent(SetupProfileActivity.this,MainActivity.class);
                                    startActivity(intent);
                                    finish();
                        }
                    });
                }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(data!=null){
            if(data.getData() != null){
                binding.imageView.setImageURI(data.getData());
                selectedImage = data.getData();
            }

        }
    }
}