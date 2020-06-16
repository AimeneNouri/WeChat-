package com.example.wechat.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wechat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {

    private Button SendVerification_btn, VerifyBtn;
    private EditText InputPhoneNumber, InputVerificationCode, countryCode;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callBacks;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    private FirebaseAuth mAuth;
    private TextView mLoginFeedback;
    private ProgressBar mProgressBar, progressBarVerify;

    private TextView your_phone_number;
    private ImageView logoPhone_activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        mAuth = FirebaseAuth.getInstance();

        SendVerification_btn = findViewById(R.id.send_verification_btn);
        VerifyBtn = findViewById(R.id.verify_button);
        InputPhoneNumber = findViewById(R.id.phone_number_input);
        InputVerificationCode = findViewById(R.id.verification_code_input);
        mLoginFeedback = findViewById(R.id.login_form_feedback);
        mProgressBar = findViewById(R.id.progressBar);
        progressBarVerify = findViewById(R.id.progressBarVerify);
        your_phone_number = findViewById(R.id.your_phone_number);
        logoPhone_activity = findViewById(R.id.logoPhone_activity);
        countryCode = findViewById(R.id.country_code);

        SendVerification_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String country_code = countryCode.getText().toString();
                String phoneNumber = InputPhoneNumber.getText().toString();

                String complete_phone_number = "+" + country_code + phoneNumber;

                if (phoneNumber.isEmpty()){
                    InputPhoneNumber.setError("Phone number is required");
                    InputPhoneNumber.requestFocus();
                    return;
                }
                else if (country_code.isEmpty())
                {
                    countryCode.setError("Country code is required");
                    countryCode.requestFocus();
                    return;
                }
                else {
                    mProgressBar.setVisibility(View.VISIBLE);
                    SendVerification_btn.setEnabled(false);

                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            complete_phone_number,
                            60,
                            TimeUnit.SECONDS,
                            PhoneLoginActivity.this,
                            callBacks
                    );
                }
            }
        });

        VerifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendVerification_btn.setVisibility(View.GONE);
                InputPhoneNumber.setVisibility(View.GONE);
                countryCode.setVisibility(View.GONE);

                String verificationCode = InputVerificationCode.getText().toString();

                if (TextUtils.isEmpty(verificationCode))
                {
                    InputVerificationCode.setError("Verification Code is required");
                    InputVerificationCode.requestFocus();
                    return;
                }
                else
                {
                    progressBarVerify.setVisibility(View.VISIBLE);
                    VerifyBtn.setEnabled(false);

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });

        callBacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                mVerificationId = verificationId;
                mResendToken = token;

                your_phone_number.setText("Enter Code Verification");
                logoPhone_activity.setImageResource(R.drawable.confirmed_otp);

                Toast.makeText(PhoneLoginActivity.this, "Code has been sent, please check and verify...", Toast.LENGTH_SHORT).show();

                mProgressBar.setVisibility(View.GONE);

                SendVerification_btn.setVisibility(View.GONE);
                VerifyBtn.setVisibility(View.VISIBLE);

                InputPhoneNumber.setVisibility(View.GONE);
                countryCode.setVisibility(View.GONE);
                InputVerificationCode.setVisibility(View.VISIBLE);
            }

            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                mProgressBar.setVisibility(View.GONE);
                SendVerification_btn.setEnabled(true);

                Toast.makeText(PhoneLoginActivity.this, "Invalid Phone Number, Please enter a valid number!!!", Toast.LENGTH_SHORT).show();

                SendVerification_btn.setVisibility(View.VISIBLE);
                VerifyBtn.setVisibility(View.GONE);

                InputPhoneNumber.setVisibility(View.VISIBLE);
                countryCode.setVisibility(View.VISIBLE);
                InputVerificationCode.setVisibility(View.GONE);
            }
        };
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        progressBarVerify.setVisibility(View.GONE);
                        sendUserToMainActivity();
                    }
                    else {
                        String message = task.getException().toString();
                        Toast.makeText(PhoneLoginActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                }
            });
    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(PhoneLoginActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
}
