package com.banti.personalcalls;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.banti.personalcalls.Fragments.Calling;
import com.banti.personalcalls.models.model;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class adapter extends FirebaseRecyclerAdapter<model,adapter.Viewholder> {
    DatabaseReference reference;
    private static final int PERMISSION_REQ_ID = 22;
    private static final String[] REQUESTED_PERMISSIONS =
            {
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.BLUETOOTH
            };
    public adapter(@NonNull FirebaseRecyclerOptions<model> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull adapter.Viewholder holder, int position, @NonNull model model) {
        reference= FirebaseDatabase.getInstance().getReference("user");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                holder.textView.setText(snapshot.child(model.getUid()).child("name").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        holder.call.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                FragmentActivity Activity = (FragmentActivity) holder.itemView.getContext();
                if (ContextCompat.checkSelfPermission(holder.itemView.getContext(), REQUESTED_PERMISSIONS[0]) != PackageManager.PERMISSION_GRANTED){
                   Activity.requestPermissions(REQUESTED_PERMISSIONS,PERMISSION_REQ_ID);
                }else {
                    FragmentManager manager = Activity.getSupportFragmentManager();
                    Bundle bundle = new Bundle();
                    bundle.putString("uid", model.getUid());
                    Calling calling = new Calling();
                    calling.setArguments(bundle);
                    manager.beginTransaction().replace(R.id.home, calling).addToBackStack(null).commit();
                }
            }
        });
    }

    @NonNull
    @Override
    public adapter.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Viewholder(LayoutInflater.from(parent.getContext()).inflate(R.layout.users,parent,false));
    }


    public class Viewholder extends RecyclerView.ViewHolder {
        TextView textView;
        ImageView call;
        View view;
        public Viewholder(@NonNull View itemView) {
            super(itemView);
            this.view=itemView;
            textView=itemView.findViewById(R.id.textView);
            call=itemView.findViewById(R.id.call);
        }
    }
}
