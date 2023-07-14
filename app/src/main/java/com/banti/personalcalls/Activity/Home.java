package com.banti.personalcalls.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import com.banti.personalcalls.Fragments.Freinds;
import com.banti.personalcalls.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.onesignal.OneSignal;

public class Home extends AppCompatActivity {
    Boolean flag=true;
    DatabaseReference reference;
    AlertDialog.Builder dialog;
    private static final int PERMISSION_REQ_ID = 22;
    private static final String[] REQUESTED_PERMISSIONS =
            {
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.BLUETOOTH
            };
    FragmentManager manager;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_home);
        getWindow().setStatusBarColor(Color.rgb(255,255,255));
        OneSignal.sendTag("tags",FirebaseAuth.getInstance().getCurrentUser().getUid());
        dialog=new AlertDialog.Builder(Home.this,R.style.Base_Theme_AppCompat_Dialog_Alert);
        dialog.setTitle("Permission");
        dialog.setMessage("Access Denied!");
        dialog.setCancelable(false);
        dialog.create();
        reference = FirebaseDatabase.getInstance().getReference("user/".concat(FirebaseAuth.getInstance().getCurrentUser().getUid().toString()));
        reference.child("is_login").setValue("true");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("access").exists()) {
                    if (!snapshot.child("access").getValue().toString().equals("true")) {
                        dialog.show();
                    }
                }else{
                    dialog.show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
         manager=getSupportFragmentManager();
            manager.beginTransaction().replace(R.id.home, new Freinds()).commit();
            if (!checkpermission()){
                requestPermissions(REQUESTED_PERMISSIONS,PERMISSION_REQ_ID);
            }
    }
    @Override
    public void onBackPressed() {
       // super.onBackPressed();
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    public Boolean checkpermission(){
        return ContextCompat.checkSelfPermission(this, REQUESTED_PERMISSIONS[0]) == PackageManager.PERMISSION_GRANTED;
    }
}