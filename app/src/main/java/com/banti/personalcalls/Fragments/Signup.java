package com.banti.personalcalls.Fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.banti.personalcalls.Activity.Home;
import com.banti.personalcalls.Database;
import com.banti.personalcalls.R;
import com.banti.personalcalls.databinding.FragmentSignupBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.nio.ShortBuffer;

public class Signup extends Fragment {
    FragmentSignupBinding binding;
    FirebaseAuth auth;
    DatabaseReference reference;
    ProgressDialog dialog;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Signup() {
        // Required empty public constructor
    }
    public static Signup newInstance(String param1, String param2) {
        Signup fragment = new Signup();
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
        binding=FragmentSignupBinding.inflate(getLayoutInflater(),container,false);
        auth=FirebaseAuth.getInstance();
        dialog=new ProgressDialog(getContext(),R.style.progress_dialog);
        dialog.setMessage("Creating account...");
        dialog.setCancelable(false);
        dialog.create();
        binding.signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager manager=requireActivity().getSupportFragmentManager();
                manager.beginTransaction().replace(R.id.accounts,new Login()).commit();
            }
        });
        binding.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validate()){
                    dialog.show();
                    if (auth.getCurrentUser()!=null){
                     account_creation();
                    }else {
                        auth.createUserWithEmailAndPassword(binding.email.getText().toString(), binding.password.getText().toString())
                                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                    @Override
                                    public void onSuccess(AuthResult authResult) {
                                      account_creation();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        dialog.dismiss();
                                        show_snack(e.getMessage());
                                    }
                                });
                    }
                }
            }
        });
        return binding.getRoot() ;
    }
    private Boolean validate(){
        if (TextUtils.isEmpty(binding.name.getText())){
            show_snack("Name is empty!");
            return false;
        }else if (TextUtils.isEmpty(binding.email.getText())){
            show_snack("Email is empty!");
            return false;
        }else if (TextUtils.isEmpty(binding.phone.getText())){
            show_snack("Phone is empty!");
           return false;
        }else if (TextUtils.isEmpty(binding.password.getText())){
            show_snack("Password is empty!");
            return false;
        }else if (binding.phone.length()!=10){
            show_snack("Enter valid number");
            return false;
        }
        else {
            return true;
        }
    }
    private void show_snack(String  message){
        Snackbar.make(binding.getRoot(),message, BaseTransientBottomBar.LENGTH_SHORT)
                .setBackgroundTint(Color.RED).show();
    }
    private void account_creation(){
        reference = FirebaseDatabase.getInstance().getReference("user/".concat(auth.getCurrentUser().getUid().toString()));
        reference.child("name").setValue(binding.name.getText().toString());
        reference.child("email").setValue(binding.email.getText().toString());
        reference.child("phone").setValue(binding.phone.getText().toString());
        reference.child("password").setValue(binding.password.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                dialog.dismiss();
                Snackbar.make(binding.getRoot(), "Succesfully Registered!", BaseTransientBottomBar.LENGTH_SHORT)
                        .show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(requireActivity(), Home.class));
                        requireActivity().finish();
                    }
                }, 1500);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                show_snack(e.getMessage());
                dialog.dismiss();
            }
        });
    }
}