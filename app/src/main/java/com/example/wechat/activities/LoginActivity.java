package com.example.wechat.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wechat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private Button LoginButton, PhoneLoginButton;
    private EditText UserEmail, UserPassword;
    private TextView NeedNewAccountLink, ForgotPassword, TogglePassword;
    private ProgressDialog loadingBar;

    private DatabaseReference UsersRef;

    private RelativeLayout relativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        final SharedPreferences preferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        String token = preferences.getString("FcmToken", "Undefined");
        Log.d("WECHAT", token);

        LoginButton = findViewById(R.id.button_login);
        PhoneLoginButton = findViewById(R.id.phone_login_button);
        UserEmail = findViewById(R.id.loginEmail);
        UserPassword = findViewById(R.id.pass_login);
        NeedNewAccountLink = findViewById(R.id.need_new_account);
        ForgotPassword = findViewById(R.id.forget_password);
        loadingBar = new ProgressDialog(this) ;
        TogglePassword = findViewById(R.id.togglePassword);
        relativeLayout = findViewById(R.id.login_relativeLayout);

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

        NeedNewAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToRegisterActivity();
            }
        });

        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AllowUserToLogin();
            }
        });
        PhoneLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToLoginByPhone();
            }
        });

        ForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRecoverPasswordDialog();
            }
        });

        relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = LoginActivity.this.getCurrentFocus();
                if (view != null)
                {
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    UserEmail.clearFocus();
                    UserPassword.clearFocus();
                }
            }
        });
    }

    private void showRecoverPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reset Password");

        LinearLayout linearLayout = new LinearLayout(this);
        final EditText emailEt = new EditText(this);
        emailEt.setHint("Email");
        emailEt.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        emailEt.setMinEms(16);

        linearLayout.addView(emailEt);
        linearLayout.setPadding(10, 10, 10, 10);

        builder.setView(linearLayout);

        builder.setPositiveButton("Reset", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String email = emailEt.getText().toString().trim();
                beginRecovery(email);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }

    private void beginRecovery(String email) {

        loadingBar.setMessage("Sending email...");
        loadingBar.show();

        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                loadingBar.dismiss();
                if (task.isSuccessful())
                {
                    Toast.makeText(LoginActivity.this, "Email sent, Check", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(LoginActivity.this, "Failed...", Toast.LENGTH_SHORT).show();
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                loadingBar.dismiss();
                Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void AllowUserToLogin() {
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
        else {
            loadingBar.setTitle("Sign In");
            loadingBar.setMessage("Please wait while we load sign-in");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {

                        String currentUserId = mAuth.getCurrentUser().getUid();
                        String deviceToken = FirebaseInstanceId.getInstance().getToken();

                        UsersRef.child(currentUserId).child("device_token").setValue(deviceToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful())
                                {
                                    sendUserToMainActivity();
                                    Toast.makeText(LoginActivity.this, "You are logged in", Toast.LENGTH_SHORT).show();
                                    loadingBar.dismiss();
                                }
                            }
                        });
                    }
                    else{
                        String message = task.getException().toString();
                        Toast.makeText(LoginActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }
            });
        }
    }


    private void sendUserToLoginByPhone() {
        Intent phoneIntent = new Intent(LoginActivity.this, loginByPhone.class);
        startActivity(phoneIntent);
    }
    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
    private void sendUserToRegisterActivity() {
        Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(registerIntent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            sendUserToMainActivity();
        }
        else{
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
