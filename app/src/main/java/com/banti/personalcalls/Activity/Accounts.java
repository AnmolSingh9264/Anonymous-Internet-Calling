package com.banti.personalcalls.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.banti.personalcalls.Fragments.Signup;
import com.banti.personalcalls.R;

public class Accounts extends AppCompatActivity {
   int exit=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_accounts);
        getWindow().setStatusBarColor(Color.rgb(255,255,255));
        Fragment_transtion(new Signup());
    }
    private void Fragment_transtion(Fragment fragment){
        FragmentManager manager=getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.accounts,fragment).commit();
    }

    @Override
    public void onBackPressed() {
        exit+=1;
       if (exit==1){
           Toast.makeText(this, "Press again to exit!", Toast.LENGTH_SHORT).show();
       }else if(exit==2){
           finish();
       }
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                exit=0;
            }
        },1500);
    }
}