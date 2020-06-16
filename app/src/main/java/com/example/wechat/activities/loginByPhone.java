package com.example.wechat.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wechat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class loginByPhone extends AppCompatActivity {

    private EditText mPhoneNumber, countryCode, OtpCode;
    private Button SendVerification_Btn, verifyOtp;
    private ProgressBar mProgressBar;
    private TextView mLoginFeedback;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_by_phone);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        countryCode = findViewById(R.id.country_code);
        mPhoneNumber = findViewById(R.id.phone_number_input);
        SendVerification_Btn = findViewById(R.id.send_verification_btn);
        mProgressBar = findViewById(R.id.progressBar);
        mLoginFeedback = findViewById(R.id.login_form_feedback);
        OtpCode = findViewById(R.id.otp_number_input);
        verifyOtp = findViewById(R.id.verify_button);

        SendVerification_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /*SendVerification_Btn.setVisibility(View.GONE);
                mPhoneNumber.setVisibility(View.GONE);
                countryCode.setVisibility(View.GONE);

                verifyOtp.setVisibility(View.VISIBLE);
                OtpCode.setVisibility(View.GONE);*/

                String country_code = countryCode.getText().toString();
                String phone_number = mPhoneNumber.getText().toString();

                String complete_phone_number = "+" + country_code + phone_number;

                if(phone_number.isEmpty() && country_code.isEmpty())
                {
                    mLoginFeedback.setText("Please Fill in the form to continue...");
                    mLoginFeedback.setVisibility(View.VISIBLE);

                }
                else
                {
                    mProgressBar.setVisibility(View.VISIBLE);
                    SendVerification_Btn.setEnabled(false);

                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            complete_phone_number,
                            60,
                            TimeUnit.SECONDS,
                            loginByPhone.this,
                            mCallbacks
                    );
                }
            }
        });

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                mLoginFeedback.setText("I still don't trust you, please try again");
                mLoginFeedback.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.INVISIBLE);
                SendVerification_Btn.setEnabled(true);
            }

            @Override
            public void onCodeSent(@NonNull final String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);

                new android.os.Handler().postDelayed(
                    new Runnable() {
                        public void run() {
                        Intent OTPIntent = new Intent(loginByPhone.this, PhoneVerification.class);
                        OTPIntent.putExtra("AuthCredentials", s);
                        startActivity(OTPIntent);
                        Toast.makeText(loginByPhone.this, "Code has been sent, Please check and verify", Toast.LENGTH_SHORT).show();
                    }
                },
                5000);
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(currentUser != null){
            Intent homeIntent = new Intent(loginByPhone.this, MainActivity.class);
            homeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(homeIntent);
            finish();
        }
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(loginByPhone.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {
                            sendUserToMainActivity();
                        }
                        else
                        {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {

                                mLoginFeedback.setVisibility(View.VISIBLE);
                                mLoginFeedback.setText("There was an error verifying OTP");
                            }
                        }
                        mProgressBar.setVisibility(View.INVISIBLE);
                        SendVerification_Btn.setEnabled(true);
                    }
                });
    }

    private void sendUserToMainActivity() {
        Intent homeIntent = new Intent(loginByPhone.this, MainActivity.class);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(homeIntent);
        finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
}
