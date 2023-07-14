package com.banti.personalcalls.request;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.banti.personalcalls.R;
import com.banti.personalcalls.models.requestmodel;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class request_adapter extends FirebaseRecyclerAdapter<requestmodel,request_adapter.Viewholder> {
    DatabaseReference reference;
    public request_adapter(@NonNull FirebaseRecyclerOptions<requestmodel> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull request_adapter.Viewholder holder, int position, @NonNull requestmodel model) {
            reference= FirebaseDatabase.getInstance().getReference("user");
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    holder.name.setText(snapshot.child(model.getUid()).child("name").getValue().toString());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            holder.remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                  DatabaseReference  delete=FirebaseDatabase.getInstance().getReference("user/".concat(model.getUid2()).concat("/request/"+model.getUid()));
                    delete.child("uid").removeValue();
                    delete.child("uid2").removeValue();
                }
            });
            holder.accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    reference.child(model.getUid()).child("friend").child(model.getUid2()).child("uid").setValue(model.getUid2());
                    reference.child(model.getUid2()).child("friend")
                            .child(model.getUid()).child("uid").setValue(model.getUid()).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            DatabaseReference  delete=FirebaseDatabase.getInstance().getReference("user/".concat(model.getUid2()).concat("/request/"+model.getUid()));
                            delete.child("uid").removeValue();
                            delete.child("uid2").removeValue();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(holder.itemView.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
    }
    @NonNull
    @Override
    public request_adapter.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Viewholder(LayoutInflater.from(parent.getContext()).inflate(R.layout.request_raw,parent,false));
    }

    public class Viewholder extends RecyclerView.ViewHolder {
        TextView name;
        ImageView remove,accept;
        public Viewholder(@NonNull View itemView) {
            super(itemView);
            name=itemView.findViewById(R.id.textView);
            remove=itemView.findViewById(R.id.remove);
            accept=itemView.findViewById(R.id.accept);
        }
    }
}
