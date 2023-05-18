package com.example.doclocker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.doclocker.databinding.ActivityPdfCategoryBinding;
import com.example.doclocker.databinding.AddPdfDialogCategoryBinding;
import com.example.doclocker.databinding.CheckPasswordDialogBinding;
import com.example.doclocker.databinding.PdfCardviewBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Executor;

public class PdfCategoryActivity extends AppCompatActivity implements PdfRetrieveAdapter.DialogListner {
   ActivityPdfCategoryBinding binding;
   String category,userName,actualPassword,url;
   FirebaseUser user;
   DatabaseReference dbRefUser,dbRefPass;
   StorageReference storageReference;
   Dialog checkPassDialog,pdfUploadDialog,addPdfDialog;
   ArrayList<ModalPdfRetrieverClass> pdfRetrieverList;
   CheckPasswordDialogBinding passwordDialogBinding;
   Executor executor;
   DatabaseReference dbRefDocs,dbRef;
    PdfCardviewBinding pdfCardviewBinding;
    AddPdfDialogCategoryBinding addPdfDialogCategoryBinding;

    AlertDialog alertDialog;

   PdfRetrieveAdapter adapter;
    TextWatcher textWatcher;
    BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    int width;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfCategoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        width = metrics.widthPixels;
        int height = metrics.heightPixels;
        executor = ContextCompat.getMainExecutor(this);
        category = getIntent().getStringExtra("CATEGORY_NAME");
        user = FirebaseAuth.getInstance().getCurrentUser();
        String email = user.getEmail();
        storageReference = FirebaseStorage.getInstance().getReference();
        pdfCardviewBinding = PdfCardviewBinding.inflate(getLayoutInflater());
        passwordDialogBinding = CheckPasswordDialogBinding.inflate(getLayoutInflater());
        checkPassDialog = new Dialog(PdfCategoryActivity.this);
        checkPassDialog.setCancelable(true);
        checkPassDialog.setContentView(passwordDialogBinding.getRoot());
        pdfRetrieverList = new ArrayList<>();
        adapter = new PdfRetrieveAdapter(pdfRetrieverList,PdfCategoryActivity.this,this);

        addPdfDialogCategoryBinding = AddPdfDialogCategoryBinding.inflate(getLayoutInflater());
        pdfUploadDialog = new Dialog(PdfCategoryActivity.this);
        pdfUploadDialog.setCancelable(true);
        pdfUploadDialog.setContentView(addPdfDialogCategoryBinding.getRoot());

        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.setNestedScrollingEnabled(false);
        binding.categoryTv.setText(category);

        dbRefUser = FirebaseDatabase.getInstance().getReference();
        dbRefUser.child("User Details").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot snapshot1 : snapshot.getChildren()){
                        userName = snapshot1.child("userName").getValue(String.class);
                        String emailDataBase = snapshot1.child("email").getValue(String.class);
                        if (Objects.equals(email, emailDataBase)){
                             makeListDocuments(userName,category);

                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.addDocument.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               pdfUploadDialog.show();
               pdfUploadDialog.getWindow().setLayout((6 * width)/7, WindowManager.LayoutParams.WRAP_CONTENT);
               addPdfDialogCategoryBinding.add.setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View v) {
                       validateField();

                   }
               });
            }
        });


    }

    private void validateField() {
        String text = addPdfDialogCategoryBinding.name.getEditText().getText().toString();
        String pass = addPdfDialogCategoryBinding.password.getEditText().getText().toString();

        if (text.isEmpty()){
            Toast.makeText(this, "Please give a name to document", Toast.LENGTH_SHORT).show();
        }
        else if (pass.isEmpty()){
            Toast.makeText(this, "Please add a password to the document", Toast.LENGTH_SHORT).show();
        }
        else {
            selectFiles();
        }
    }

    private void selectFiles() {
        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Selecting files...."),1);

    }
    @SuppressLint("Range")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==1&& resultCode==RESULT_OK&&data!=null&&data.getData()!=null){
            Uri uri = data.getData();
            String uriString = uri.toString();
            File myFile = new File(uriString);
            String path = myFile.getAbsolutePath();
            String displayName = null;
            if(uriString.startsWith("content://")){
                Cursor cursor = null;
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        cursor = this.getContentResolver().query(uri,null,null,null);
                    }
                    if (cursor!=null&&cursor.moveToFirst()){
                        displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));


                    }

                } finally {
                    cursor.close();
                }
            }
            else if (uriString.startsWith("file://")){
                displayName = myFile.getName();
            }


            UploadFiles(data.getData());
        }
    }

    private void UploadFiles(Uri data) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading the file....");
        progressDialog.show();
        dbRef = FirebaseDatabase.getInstance().getReference(userName+" Uploads");
        StorageReference reference = storageReference.child("Uploads/"+addPdfDialogCategoryBinding.name.getEditText().getText().toString()+".pdf");
        reference.putFile(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isComplete());
                Uri url = uriTask.getResult();
                ModalPdfClass pdfClass = new ModalPdfClass(addPdfDialogCategoryBinding.name.getEditText().getText().toString(),url.toString(),addPdfDialogCategoryBinding.password.getEditText().getText().toString());
                dbRef.child(category).child(addPdfDialogCategoryBinding.name.getEditText().getText().toString()).setValue(pdfClass);
                Toast.makeText(PdfCategoryActivity.this, "File uploaded", Toast.LENGTH_SHORT).show();
                pdfUploadDialog.dismiss();
                progressDialog.dismiss();
                pdfRetrieverList.clear();
                makeListDocuments(userName,category);

            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                double progress = (100*snapshot.getBytesTransferred())/snapshot.getTotalByteCount();
                progressDialog.setMessage("Uploaded"+(int)progress+"%");
                pdfUploadDialog.cancel();

            }
        });
    }

    private void makeListDocuments(String userName, String category) {
        dbRefDocs = FirebaseDatabase.getInstance().getReference(userName+" Uploads");
        dbRefDocs.child(category).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                         ModalPdfRetrieverClass m = dataSnapshot.getValue(ModalPdfRetrieverClass.class);
                         pdfRetrieverList.add(m);
                    }
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public void showDialog(View view) {
        checkPassDialog.show();
        checkPassDialog.getWindow().setLayout((6 * width)/7, WindowManager.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void checkPassword(String nameOfPdf) {
        dbRefPass = FirebaseDatabase.getInstance().getReference(userName+" Uploads");
        dbRefPass.child(category).child(nameOfPdf).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot snapshot1 : snapshot.getChildren()){
                        if (Objects.equals(snapshot1.getKey(), "password")){
                            actualPassword = snapshot1.getValue().toString();
                            Log.d("Passss", ""+actualPassword);
                        }
                        if (Objects.equals(snapshot1.getKey(), "url")){
                            url =  snapshot1.getValue().toString();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                passwordDialogBinding.password.setError(null);
            }
        };

        passwordDialogBinding.password.getEditText().addTextChangedListener(textWatcher);
        passwordDialogBinding.add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String enteredPassword = Objects.requireNonNull(passwordDialogBinding.password.getEditText()).getText().toString();
                Log.d("Passs", ""+actualPassword);
                if (enteredPassword.equals(actualPassword)){
                    Intent intent = new Intent(pdfCardviewBinding.viewPdf.getContext(), ViewPdfActivity.class);
                    intent.putExtra("filename",nameOfPdf);
                    Log.d("Urlllll", ""+url);
                    intent.putExtra("fileurl",url);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    pdfCardviewBinding.viewPdf.getContext().startActivity(intent);
                    Toast.makeText(PdfCategoryActivity.this, "Access granted", Toast.LENGTH_SHORT).show();
                    checkPassDialog.dismiss();
                }
                else {
                    Toast.makeText(PdfCategoryActivity.this, "Access denied", Toast.LENGTH_SHORT).show();
                }
            }
        });
        passwordDialogBinding.forgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                biometricPrompt = new BiometricPrompt(PdfCategoryActivity.this, executor, new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        Toast.makeText(PdfCategoryActivity.this, "Authentication error "+errString, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        Toast.makeText(PdfCategoryActivity.this, "Authentication succeeded", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(pdfCardviewBinding.viewPdf.getContext(),ViewPdfActivity.class);
                        intent.putExtra("filename",nameOfPdf);
                        intent.putExtra("fileurl",url);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        pdfCardviewBinding.viewPdf.getContext().startActivity(intent);
                        Toast.makeText(PdfCategoryActivity.this, "Access granted", Toast.LENGTH_SHORT).show();
                        checkPassDialog.dismiss();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        Toast.makeText(PdfCategoryActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                    }
                });
                promptInfo = new BiometricPrompt.PromptInfo.Builder().setTitle("Biometric login for my app")
                        .setSubtitle("Access the pdf file using the biometric")
                        .setNegativeButtonText("Use account password")
                        .build();
                biometricPrompt.authenticate(promptInfo);
            }
        });
    }

    @Override
    public void deleteDocument(String name) {

        AlertDialog.Builder builder = new AlertDialog.Builder(PdfCategoryActivity.this);
        builder.setIcon(R.drawable.warning);
        builder.setMessage("Are you sure you want to delete this document?");
        builder.setTitle("Alert !");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", (DialogInterface.OnClickListener) (dialog, which) -> {
            DatabaseReference dbref= FirebaseDatabase.getInstance().getReference(userName+" Uploads").child(category);
            Query query=dbref.child(name);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    // remove the value at reference
                    dataSnapshot.getRef().removeValue();
                    Toast.makeText(PdfCategoryActivity.this, "Document deleted successfully", Toast.LENGTH_SHORT).show();
                    pdfRetrieverList.clear();
                    makeListDocuments(userName,category);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        });

        builder.setNegativeButton("No", (DialogInterface.OnClickListener) (dialog, which) -> {
            dialog.cancel();
        });

        alertDialog = builder.create();
        alertDialog.show();
    }
}