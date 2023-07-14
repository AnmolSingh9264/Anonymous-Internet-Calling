package com.banti.personalcalls.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.banti.personalcalls.Generator.io.agora.media.RtcTokenBuilder;
import com.banti.personalcalls.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;

import io.agora.rtc2.ChannelMediaOptions;
import io.agora.rtc2.Constants;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;

public class Incomming extends AppCompatActivity {
    private RtcEngine agoraEngine;
    Boolean micoff=true;
    String appid,name,token,time,certificate;
    ProgressDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_incomming);
        getWindow().setStatusBarColor(Color.rgb(255,255,255));
        dialog=new ProgressDialog(Incomming.this,R.style.progress_dialog);
        dialog.setMessage("Connecting...");
        dialog.setCancelable(false);
        dialog.create();
        IRtcEngineEventHandler mRtcEventHandler =new IRtcEngineEventHandler() {
            @Override
            public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
                super.onJoinChannelSuccess(channel, uid, elapsed);
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                        Toast.makeText(Incomming.this, "Connected to channel", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onLeaveChannel(RtcStats stats) {
                super.onLeaveChannel(stats);
            }

            @Override
            public void onUserJoined(int uid, int elapsed) {
                super.onUserJoined(uid, elapsed);
                sendmsg("Connected to caller");
            }

            @Override
            public void onUserOffline(int uid, int reason) {
                super.onUserOffline(uid, reason);
                sendmsg("Caller disconnected");
            }

            @Override
            public void onConnectionInterrupted() {
                super.onConnectionInterrupted();
                sendmsg("Connection interrupted");
            }

            @Override
            public void onConnectionLost() {
                super.onConnectionLost();
                sendmsg("Connection lost");
            }
        };
        DatabaseReference  reference=FirebaseDatabase.getInstance().getReference("user/".concat(FirebaseAuth.getInstance().getCurrentUser().getUid()).concat("/calling"));
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                 appid=snapshot.child("appid").getValue().toString();
                 name=snapshot.child("caller").getValue().toString();
                certificate=snapshot.child("certificate").getValue().toString();
                time=snapshot.child("time").getValue().toString();
                DatabaseReference caller_name=FirebaseDatabase.getInstance().getReference("user/".concat(name));
                caller_name.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        TextView name=findViewById(R.id.textView);
                        name.setText(snapshot.child("name").getValue().toString());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        ImageView mute=findViewById(R.id.mic);
        mute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (agoraEngine!=null){
                    if(micoff){
                        agoraEngine.muteLocalAudioStream(true);
                        Toast.makeText(Incomming.this, "Speaker off", Toast.LENGTH_SHORT).show();
                        micoff=false;
                    }else{
                        agoraEngine.muteLocalAudioStream(false);
                        Toast.makeText(Incomming.this, "Speaker on", Toast.LENGTH_SHORT).show();
                        micoff=true;
                    }
                }

            }
        });
        ImageView call=findViewById(R.id.imageView);
        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
                Random random=new Random();
                int uuid=random.nextInt(999);
                setupVoiceSDKEngine(mRtcEventHandler,appid);
                token=create_server(appid,certificate,time,uuid,name);
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                if (agoraEngine!=null){
                   joinChannel(token,name,uuid);
                }else{
                    Toast.makeText(Incomming.this, "NUll", Toast.LENGTH_SHORT).show();
                }
                    }
                },2500);
               // setupVoiceSDKEngine(mRtcEventHandler);
            }
        });
        ImageView end=findViewById(R.id.end);
        end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               leave();
            }
        });
    }
    @Override
    public void onBackPressed() {
       // super.onBackPressed();
        leave();
    }
    private void setupVoiceSDKEngine(IRtcEngineEventHandler mRtcEventHandler,String appId) {
        try {
            RtcEngineConfig config = new RtcEngineConfig();
            config.mContext =getBaseContext();
            config.mAppId = appId;
            config.mEventHandler = mRtcEventHandler;
            agoraEngine = RtcEngine.create(config);
        } catch (Exception e) {
            throw new RuntimeException("Check the error.");
        }
    }
    private void joinChannel(String token,String channel,int uuid) {
        ChannelMediaOptions options = new ChannelMediaOptions();
        options.autoSubscribeAudio = true;
        options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER;
        options.channelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING;
        agoraEngine.joinChannel(token,channel,uuid,options);
    }
    private String create_server(String appid,String certificate,String time,int uid,String channel){
        RtcTokenBuilder tokenBuilder = new RtcTokenBuilder();
        int timestamp = (int)(System.currentTimeMillis() / 1000 + Integer.parseInt(time));
        String result = tokenBuilder.buildTokenWithUid(appid, certificate,
                channel, uid, RtcTokenBuilder.Role.Role_Publisher, timestamp);
        return result;
    }
    private void leave(){
        if (agoraEngine!=null){
            agoraEngine.leaveChannel();
        }
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("user/".concat(FirebaseAuth.getInstance().getCurrentUser().getUid()).concat("/calling"));
        reference.removeValue();
        startActivity(new Intent(this,Home.class));
        finish();
    }
    private void sendmsg(String msg){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(Incomming.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

}