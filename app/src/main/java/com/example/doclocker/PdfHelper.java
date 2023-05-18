package com.example.doclocker;

import android.app.Application;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

public class PdfHelper extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
    }
    public static void loadPdfSize(String docUrl, String docTitle, TextView sizeTv){
        String TAG = "PDF_SIZE_TAG";
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(docUrl);
        storageReference.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {
                double bytes = storageMetadata.getSizeBytes();
                Log.d(TAG, "onSuccess: "+docTitle +" "+bytes);

                //convert bytes to KB, MB
                double kb = bytes/1024;
                double mb = kb/1024;

                if (mb >= 1){
                    sizeTv.setText(String.format("%.2f", mb)+" MB");
                }
                else if (kb >= 1){
                    sizeTv.setText(String.format("%.2f", kb)+" KB");
                }
                else {
                    sizeTv.setText(String.format("%.2f", bytes)+" bytes");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }
}


