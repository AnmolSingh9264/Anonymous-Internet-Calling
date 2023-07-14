package com.banti.personalcalls.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import com.banti.personalcalls.Activity.Accounts;
import com.banti.personalcalls.Activity.Home;
import com.banti.personalcalls.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.onesignal.OneSignal;

public class MainActivity extends AppCompatActivity {
    FirebaseAuth auth;
    DatabaseReference reference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);
        OneSignal.initWithContext(this);
        OneSignal.setAppId("a2caa25b-9dec-4b8a-a683-6ef33ba19e1c");
        OneSignal.promptForPushNotifications();
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        getWindow().setStatusBarColor(Color.rgb(255,255,255));
        auth=FirebaseAuth.getInstance();
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (auth.getCurrentUser()!=null){
                    reference= FirebaseDatabase.getInstance().getReference("user/".concat(auth.getCurrentUser().getUid()));
                    reference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.child("is_login").exists()&&snapshot.child("name").exists()&&snapshot.child("email").exists()&&snapshot.child("phone").exists()&&snapshot.child("password").exists()){
                                    transition(Home.class);
                                }else{
                                    transition(Accounts.class);
                                }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
                }else{
                    transition(Accounts.class);
                }
            }
        },6500);
    }
    private void transition(Class<?> activity){
        startActivity(new Intent(this, activity));
        finish();
    }
}