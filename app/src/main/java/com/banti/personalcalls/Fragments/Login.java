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
import com.banti.personalcalls.R;
import com.banti.personalcalls.databinding.FragmentLoginBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Login extends Fragment {
     FragmentLoginBinding binding;
     DatabaseReference reference;
     ProgressDialog dialog;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    public Login() {
        // Required empty public constructor
    }
    public static Login newInstance(String param1, String param2) {
        Login fragment = new Login();
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
        binding=FragmentLoginBinding.inflate(getLayoutInflater(),container,false);
        dialog=new ProgressDialog(getContext(),R.style.progress_dialog);
        dialog.setMessage("Logging in...");
        dialog.setCancelable(false);
        dialog.create();
        binding.register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager manager=requireActivity().getSupportFragmentManager();
                manager.beginTransaction().replace(R.id.accounts,new Signup() ).commit();
            }
        });
        binding.forgetpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(binding.email.getText().toString())){
                    show_snack("Email is empty");
                }else{
                    FirebaseAuth.getInstance().sendPasswordResetEmail(binding.email.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Snackbar.make(binding.getRoot(),"Password reset link is sent to your email", BaseTransientBottomBar.LENGTH_SHORT)
                                            .show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    show_snack(e.getMessage());
                                }
                            });
                }
            }
        });
        binding.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validate()){
                    dialog.show();
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(binding.email.getText().toString(),binding.password.getText().toString())
                            .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                @Override
                                public void onSuccess(AuthResult authResult) {
                                    reference= FirebaseDatabase.getInstance().getReference("user/".concat(FirebaseAuth.getInstance().getCurrentUser().getUid().concat("/is_login")));
                                    reference.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            dialog.dismiss();
                                            if (snapshot.exists()){
                                                show_snack("Already login in another device");
                                            }else{
                                                Snackbar.make(binding.getRoot(),"Succesfully Logged in!", BaseTransientBottomBar.LENGTH_SHORT)
                                                        .show();
                                                new Handler().postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        startActivity(new Intent(requireActivity(), Home.class));
                                                        requireActivity().finish();
                                                    }
                                                },1500);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                   show_snack(e.getMessage());
                                }
                            });
                }
            }
        });
        return binding.getRoot();
    }
    private Boolean validate() {
        if (TextUtils.isEmpty(binding.email.getText())) {
            show_snack("Email is empty!");
            return false;
        } else if (TextUtils.isEmpty(binding.password.getText())) {
            show_snack("Password is empty!");
            return false;
        } else {
            return true;
        }
    }
    private void show_snack(String  message){
        Snackbar.make(binding.getRoot(),message, BaseTransientBottomBar.LENGTH_SHORT)
                .setBackgroundTint(Color.RED).show();
    }
}