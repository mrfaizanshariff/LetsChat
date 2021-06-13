package com.aelinstudios.sjcetalks.Activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.aelinstudios.sjcetalks.databinding.ActivityOTPBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.mukesh.OnOtpCompletionListener;

import java.util.concurrent.TimeUnit;

public class OTPActivity extends AppCompatActivity {
    //setting up the binding
    ActivityOTPBinding binding;
    //Setting up FireBase Auth
    FirebaseAuth firebaseAuth;

    ProgressDialog dialog;

    String verficationID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOTPBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //setting progress dialog
        dialog = new ProgressDialog(this);
        dialog.setMessage("Sending OTP..");
        dialog.setCancelable(false);
        dialog.show();

        //hiding the actionBar
//        getSupportActionBar().hide();


        //getting instance of firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

        //recieving the data from the previous intent
        String phoneNumber = getIntent().getStringExtra("PhoneNumber");
        binding.otpverifytextview.setText("Verify: "+phoneNumber);

        //Creating Phone auth configurations
        PhoneAuthOptions options =  PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(OTPActivity.this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {



                    }

                    @Override
                    public void onCodeSent(@NonNull String verifyID, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(verifyID, forceResendingToken);

                        dialog.dismiss();
                        verficationID=verifyID;
                        //setting up keyboard programatically
                        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
                        binding.otpView.requestFocus();

                    }
                }).build();
        //now we have to settup verification
        PhoneAuthProvider.verifyPhoneNumber(options);

        //now handling the OTPView
        binding.otpView.setOtpCompletionListener(new OnOtpCompletionListener() {
            @Override
            public void onOtpCompleted(String otp) {
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verficationID,otp);

                firebaseAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        InputMethodManager imm =(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(binding.otpView.getWindowToken(), 0);

                        if(task.isSuccessful()){
                            Intent intent= new Intent(OTPActivity.this,SetupProfileActivity.class);
                            startActivity(intent);
                            finishAffinity();//this will finish all the activities opened before
                        }else {
                            Toast.makeText(OTPActivity.this,"Verification Failed",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
}