package com.banti.personalcalls.Fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.banti.personalcalls.Activity.Home;
import com.banti.personalcalls.Activity.Incomming;
import com.banti.personalcalls.R;
import com.banti.personalcalls.databinding.FragmentCallingBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

import io.agora.rtc2.ChannelMediaOptions;
import io.agora.rtc2.Constants;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;
import com.banti.personalcalls.Generator.io.agora.media.RtcTokenBuilder;
import com.banti.personalcalls.Generator.io.agora.media.RtcTokenBuilder.Role;

import org.json.JSONException;
import org.json.JSONObject;

public class Calling extends Fragment {
    FragmentCallingBinding binding;
    Boolean micoff=true;
    Boolean start=false;
    ProgressDialog dialog;
  //  private final String appId = "442818a1041240ceb64cecf35f7a3c2f";
  //  private String token = "007eJxTYOBcKDF7Qq2sqsG53RplOhtSD3Z96a1VDYlsdF/iPfMk2y8FBhMTIwtDi0RDAxNDIxOD5NQkM5Pk1OQ0Y9M080TjZKO0ueXhyY2BjAx+Va6sjAwQCOKzMCRnJJYwMAAAwIEdmA==";
    // Track the status of your connection
    private boolean isJoined = false;
    private RtcEngine agoraEngine;
    DatabaseReference required,reference;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Calling() {
        // Required empty public constructor
    }
    public static Calling newInstance(String param1, String param2) {
        Calling fragment = new Calling();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                startActivity(new Intent(requireContext(), Home.class));
                if (agoraEngine!=null){
                    agoraEngine.leaveChannel();
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        String uid=getArguments().getString("uid");
        binding=FragmentCallingBinding.inflate(getLayoutInflater(),container,false);
      reference= FirebaseDatabase.getInstance().getReference("user/".concat(uid).concat("/calling"));
        DatabaseReference caller_name= FirebaseDatabase.getInstance().getReference("user/"+uid);
        required=FirebaseDatabase.getInstance().getReference("required");
        dialog=new ProgressDialog(getContext(),R.style.progress_dialog);
        dialog.setMessage("connecting...");
        dialog.setCancelable(false);
        dialog.create();
        caller_name.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
              binding.textView.setText(snapshot.child("name").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        IRtcEngineEventHandler mRtcEventHandler =new IRtcEngineEventHandler() {
            @Override
            public void onJoinChannelSuccess(String channel, int uid2, int elapsed) {
                super.onJoinChannelSuccess(channel, uid2, elapsed);
               new Handler(Looper.getMainLooper()).post(new Runnable() {
                   @Override
                   public void run() {
                       reference.child("caller").setValue(FirebaseAuth.getInstance().getCurrentUser().getUid()).addOnSuccessListener(new OnSuccessListener<Void>() {
                           @Override
                           public void onSuccess(Void unused) {
                               Toast.makeText(getContext(), "Call sent", Toast.LENGTH_SHORT).show();
                               notificaiton();
                           }
                       }).addOnFailureListener(new OnFailureListener() {
                           @Override
                           public void onFailure(@NonNull Exception e) {
                               Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                           }
                       });
                       dialog.dismiss();
                       Toast.makeText(getContext(), "connected to channel", Toast.LENGTH_SHORT).show();
                   }
               });
               // joinChannel();
            }

            @Override
            public void onLeaveChannel(RtcStats stats) {
                super.onLeaveChannel(stats);
                Log.d("msg","leved");
            }
            @Override
            public void onUserJoined(int uid, int elapsed) {
                super.onUserJoined(uid, elapsed);
               sendmsg("Call received");
            }

            @Override
            public void onUserOffline(int uid, int reason) {
                super.onUserOffline(uid, reason);
                sendmsg("Receiver disconnected");
            }

            @Override
            public void onConnectionInterrupted() {
                super.onConnectionInterrupted();
                sendmsg("Connection Interrupted");
            }

            @Override
            public void onConnectionLost() {
                super.onConnectionLost();
                sendmsg("Connection Lost");
            }
        };

       // joinChannel();

        binding.recive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                start=true;
                dialog.show();
               // setupVoiceSDKEngine(mRtcEventHandler);
                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            if(start) {
                                Toast.makeText(requireContext(), "Busy on another call", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                                start=false;
                            }
                        }else{
                            required.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (start) {
                                        start=false;
                                        String appid = snapshot.child("appid").getValue().toString();
                                        String certificate = snapshot.child("certificate").getValue().toString();
                                        String time = snapshot.child("time").getValue().toString();
                                        reference.child("appid").setValue(appid);
                                        reference.child("certificate").setValue(certificate);
                                        reference.child("time").setValue(time);
                                        Random rnd = new Random();
                                        int ruid = rnd.nextInt(999);
                                        String channel = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                        String token = create_server(appid, certificate, time, ruid, channel);
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                setupVoiceSDKEngine(mRtcEventHandler, appid);
                                                reference.child("token").setValue(token);
                                                if (agoraEngine != null) {
                                                    joinChannel(token, ruid);
                                                }
                                            }
                                        }, 2500);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                  dialog.dismiss();
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                          dialog.dismiss();
                    }
                });
            }
        });
       binding.mic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (agoraEngine!=null){
                    if(micoff){
                        agoraEngine.muteLocalAudioStream(true);
                        Toast.makeText(getContext(), "Speaker off", Toast.LENGTH_SHORT).show();
                        micoff=false;
                    }else{
                        agoraEngine.muteLocalAudioStream(false);
                        Toast.makeText(getContext(), "Speaker on", Toast.LENGTH_SHORT).show();
                        micoff=true;
                    }
                }

            }
        });
        binding.end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                agoraEngine.leaveChannel();
                agoraEngine=null;
                reference.removeValue();
                        FragmentManager manager=requireActivity().getSupportFragmentManager();
                        manager.beginTransaction().replace(R.id.home, new Freinds()).addToBackStack(null).commit();

            }
        });
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
    }
    private void setupVoiceSDKEngine(IRtcEngineEventHandler mRtcEventHandler,String appid) {
        try {
            RtcEngineConfig config = new RtcEngineConfig();
            config.mContext =requireContext();
            config.mAppId = appid;
            config.mEventHandler = mRtcEventHandler;
            agoraEngine = RtcEngine.create(config);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    private void joinChannel(String token,int uid) {
        ChannelMediaOptions options = new ChannelMediaOptions();
        options.autoSubscribeAudio = true;
        options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER;
        options.channelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING;
        agoraEngine.joinChannel(token, FirebaseAuth.getInstance().getCurrentUser().getUid().toString(),uid, options);
    }
    private String create_server(String appid,String certificate,String time,int uid,String channel){
        RtcTokenBuilder tokenBuilder = new RtcTokenBuilder();
        int timestamp = (int)(System.currentTimeMillis() / 1000 + Integer.parseInt(time));
        String result = tokenBuilder.buildTokenWithUid(appid, certificate,
                FirebaseAuth.getInstance().getCurrentUser().getUid().toString(), uid, Role.Role_Publisher, timestamp);
        return result;
    }
    private void notificaiton(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String jsonResponse;

                    URL url = new URL("https://onesignal.com/api/v1/notifications");
                    HttpURLConnection con = (HttpURLConnection)url.openConnection();
                    con.setUseCaches(false);
                    con.setDoOutput(true);
                    con.setDoInput(true);
                    con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    con.setRequestProperty("Authorization", "Basic ODExMTkyYTctMTQyYS00MzMzLTg4Y2ItMWU3N2I2Nzk5NWI4");
                    con.setRequestMethod("POST");
                    String strJsonBody = "{"
                            +   "\"app_id\": \"a2caa25b-9dec-4b8a-a683-6ef33ba19e1c\","
                            +   "\"filters\": [{\"field\": \"tag\", \"key\": \"tags\", \"relation\": \"=\", \"value\": \""+getArguments().getString("uid")+"\"}],"
                            +   "\"included_segments\": [\"Subscribed Users\"],"
                            +   "\"headings\": {\"en\": \""+binding.textView.getText().toString()+"\"},"
                            +   "\"contents\": {\"en\": \"Incoming Call\"}"
                            + "}";


                    System.out.println("strJsonBody:\n" + strJsonBody);

                    byte[] sendBytes = strJsonBody.getBytes("UTF-8");
                    con.setFixedLengthStreamingMode(sendBytes.length);

                    OutputStream outputStream = con.getOutputStream();
                    outputStream.write(sendBytes);

                    int httpResponse = con.getResponseCode();
                    Log.d("data",""+httpResponse);

                    if (  httpResponse >= HttpURLConnection.HTTP_OK
                            && httpResponse < HttpURLConnection.HTTP_BAD_REQUEST) {
                        Scanner scanner = new Scanner(con.getInputStream(), "UTF-8");
                        jsonResponse = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
                        scanner.close();
                    }
                    else {
                        Scanner scanner = new Scanner(con.getErrorStream(), "UTF-8");
                        jsonResponse = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
                        scanner.close();
                    }
                    Log.d("data",jsonResponse.toString());

                } catch(Throwable t) {
                    t.printStackTrace();
                    Log.d("data",t.toString());
                }

            }
        }).start();
    }
    private void sendmsg(String msg){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }
}