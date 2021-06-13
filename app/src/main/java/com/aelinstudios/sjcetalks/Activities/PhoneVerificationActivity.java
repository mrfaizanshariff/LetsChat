package com.aelinstudios.sjcetalks.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.aelinstudios.sjcetalks.databinding.ActivityPhoneVerificationBinding;
import com.google.firebase.auth.FirebaseAuth;

public class PhoneVerificationActivity extends AppCompatActivity {
    //enabling view binding
    ActivityPhoneVerificationBinding binding;
    FirebaseAuth auth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //using the binding object to bind it to the layout
        binding = ActivityPhoneVerificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        if(auth.getCurrentUser() != null){
            Intent intent = new Intent(PhoneVerificationActivity.this,MainActivity.class);
            startActivity(intent);
            finish();
        }

//        getSupportActionBar().hide();

        // setting on clickListener to the button and taking the user to next intent
        binding.setupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PhoneVerificationActivity.this,OTPActivity.class);
                //we are passing value between activities so we have to use intent.putExtra()
                intent.putExtra("PhoneNumber",binding.nameBox.getText().toString());
                startActivity(intent);
            }
        });
    }
}