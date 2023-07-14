package com.banti.personalcalls;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;

public class Database {
    Boolean flag=false;
    DatabaseReference reference;
    public Database(DatabaseReference reference){
        this.reference=reference;
    }
    public Boolean insert(String value,String child){
          reference.child(child).setValue(value).addOnCompleteListener(new OnCompleteListener<Void>() {
              @Override
              public void onComplete(@NonNull Task<Void> task) {
                  setFlag(true);
              }
          }).addOnFailureListener(new OnFailureListener() {
              @Override
              public void onFailure(@NonNull Exception e) {
                  setFlag(false);
                  Log.d("datasignin",e.getMessage());
              }
          });
          return getFlag();
    }
    public void setFlag(Boolean flag) {
        this.flag = flag;
    }
    public Boolean getFlag() {
        return flag;
    }
}
