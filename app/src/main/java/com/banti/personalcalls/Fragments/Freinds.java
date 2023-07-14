package com.banti.personalcalls.Fragments;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.banti.personalcalls.Activity.Incomming;
import com.banti.personalcalls.R;
import com.banti.personalcalls.adapter;
import com.banti.personalcalls.databinding.FragmentFreindsBinding;
import com.banti.personalcalls.models.model;
import com.banti.personalcalls.request.request_adapter;
import com.banti.personalcalls.models.requestmodel;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Freinds extends Fragment {
    FragmentFreindsBinding binding;
    adapter Adapter;
    DatabaseReference reference,uid_exist,sendrequest;
    BottomSheetDialog dialog,request;
    request_adapter requestAdapter;
    Boolean stop_request_send=false;
    private static final int PERMISSION_REQ_ID = 22;
    private static final String[] REQUESTED_PERMISSIONS =
            {
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.BLUETOOTH
            };
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Freinds() {
        // Required empty public constructor
    }
    public static Freinds newInstance(String param1, String param2) {
        Freinds fragment = new Freinds();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
       binding=FragmentFreindsBinding.inflate(getLayoutInflater(),container,false);
       dialog=new BottomSheetDialog(requireContext());
       dialog.setContentView(R.layout.add_user_dialog);
       dialog.create();
       request=new BottomSheetDialog(requireContext());
       request.setContentView(R.layout.request_list);
       request.create();
       reference=FirebaseDatabase.getInstance().getReference("user/".concat(FirebaseAuth.getInstance().getCurrentUser().getUid()).concat("/request"));
       uid_exist=FirebaseDatabase.getInstance().getReference("user");
        EditText phone=dialog.findViewById(R.id.name);
        Button send_request=dialog.findViewById(R.id.button);
        Button copyphone=dialog.findViewById(R.id.userid);
        String path="user/".concat(FirebaseAuth.getInstance().getCurrentUser().getUid()).concat("/friend");
        FirebaseRecyclerOptions<model> options=new FirebaseRecyclerOptions.Builder<model>()
                .setQuery(FirebaseDatabase.getInstance().getReference(path), model.class).build();
        Adapter=new adapter(options);
        binding.recyclerview.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerview.setItemAnimator(null);
        binding.recyclerview.setHasFixedSize(true);
        binding.recyclerview.setAdapter(Adapter);
        binding.add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
            }
        });
        send_request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stop_request_send=true;
                if (!TextUtils.isEmpty(phone.getText().toString())){
                    uid_exist.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (stop_request_send) {
                                if (snapshot.child(phone.getText().toString()).exists()) {
                                    stop_request_send=false;
                                    if (snapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("friend").child(phone.getText().toString()).exists()) {
                                        Toast.makeText(getContext(), "already in friend list!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        sendrequest = FirebaseDatabase.getInstance().getReference("user/".concat(phone.getText().toString()).concat("/request"));
                                        sendrequest.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .child("uid2").setValue(phone.getText().toString());
                                        sendrequest.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .child("uid").setValue(FirebaseAuth.getInstance().getCurrentUser().getUid()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Toast.makeText(getContext(), "Requested successfully", Toast.LENGTH_SHORT).show();
                                                phone.setText("");
                                                dialog.dismiss();
                                            }
                                        })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }
                                } else {
                                    Toast.makeText(getContext(), "User not exist", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }
        });
        //runtime incomming  call checker
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("user/".concat(FirebaseAuth.getInstance().getCurrentUser().getUid()).concat("/calling"));
        reference.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("caller").exists()){
                    if (!checkpermission()){
                        requireActivity().requestPermissions(REQUESTED_PERMISSIONS,PERMISSION_REQ_ID);
                    }else {
                        Intent intent = new Intent(getContext(), Incomming.class);
                        intent.putExtra("data", snapshot.child("caller").getValue().toString());
                        startActivity(intent);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        copyphone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboard = (ClipboardManager) requireActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Phone", FirebaseAuth.getInstance().getCurrentUser().getUid());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getContext(), "Your phone number copied to clipboard", Toast.LENGTH_SHORT).show();
            }
        });
        FirebaseRecyclerOptions<requestmodel> recyclerOptions=new FirebaseRecyclerOptions.Builder<requestmodel>()
                .setQuery(FirebaseDatabase.getInstance().getReference("user/"+FirebaseAuth.getInstance().getCurrentUser().getUid()+"/request"), requestmodel.class).build();
        RecyclerView request_list=request.findViewById(R.id.request_list);
        requestAdapter=new request_adapter(recyclerOptions);
        request_list.setLayoutManager(new LinearLayoutManager(getContext()));
        request_list.setItemAnimator(null);
        request_list.setHasFixedSize(true);
        request_list.setAdapter(requestAdapter);
        binding.request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                request.show();
            }
        });
        return binding.getRoot();
    }
    @Override
    public void onStart() {
        super.onStart();
        Adapter.startListening();
        requestAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        Adapter.stopListening();
        requestAdapter.stopListening();
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    public Boolean checkpermission(){
        return ContextCompat.checkSelfPermission(getContext(), REQUESTED_PERMISSIONS[0]) == PackageManager.PERMISSION_GRANTED;
    }
}