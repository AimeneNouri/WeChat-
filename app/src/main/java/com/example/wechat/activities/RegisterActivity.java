package com.example.wechat.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wechat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class RegisterActivity extends AppCompatActivity {

    private Button SignUpButton;
    private EditText UserEmail, UserPassword;
    private TextView AlreadyAccount, TogglePassword;

    private FirebaseAuth mAuth;
    private DatabaseReference Rootref;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        Rootref = FirebaseDatabase.getInstance().getReference();

        SignUpButton = (Button) findViewById(R.id.button_Register);
        UserEmail = (EditText) findViewById(R.id.RegisterEmail);
        UserPassword = (EditText) findViewById(R.id.pass_register);
        AlreadyAccount = (TextView) findViewById(R.id.already_have_account_link);
        loadingBar = new ProgressDialog(this) ;
        TogglePassword = findViewById(R.id.togglePassword);

        TogglePassword.setVisibility(View.GONE);
        UserPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        UserPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (UserPassword.getText().length() > 0)
                {
                    TogglePassword.setVisibility(View.VISIBLE);
                }
                else
                {
                    TogglePassword.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        TogglePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TogglePassword.getText() == "SHOW")
                {
                    TogglePassword.setText("HIDE");
                    UserPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    UserPassword.setSelection(UserPassword.length());
                }
                else
                {
                    TogglePassword.setText("SHOW");
                    UserPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    UserPassword.setSelection(UserPassword.length());
                }
            }
        });

        AlreadyAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               sendUserToLoginActivity();
            }
        });

        SignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateNewAccount();
            }
        });
    }

    private void sendUserToLoginActivity() {
        Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    private void CreateNewAccount() {
        String email = UserEmail.getText().toString();
        String password = UserPassword.getText().toString();

        if(TextUtils.isEmpty(email))
        {
            Toast.makeText(this, "Please Enter your email!!", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(password))
        {
            Toast.makeText(this, "Please Enter your password!!", Toast.LENGTH_SHORT).show();
        }
        else{
            loadingBar.setTitle("Creating New Account");
            loadingBar.setMessage("Please wait, while we were creating new account for you");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        String deviceToken = FirebaseInstanceId.getInstance().getToken();
                        String currentUserID = mAuth.getCurrentUser().getUid();
                        Rootref.child("Users").child(currentUserID).setValue("");

                        Rootref.child("Users").child(currentUserID).child("device_token").setValue(deviceToken);

                        sendUserToMainActivity();
                        Toast.makeText(RegisterActivity.this, "Account Created Successfuly!", Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                    else{
                        String message = task.getException().toString();
                        Toast.makeText(RegisterActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }
            });
        }
    }



    private void sendUserToMainActivity() {
        Intent homeIntent = new Intent(RegisterActivity.this, MainActivity.class);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(homeIntent);
        finish();
    }
}
