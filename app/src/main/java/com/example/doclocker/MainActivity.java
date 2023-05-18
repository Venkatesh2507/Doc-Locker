package com.example.doclocker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.doclocker.databinding.ActivityMainBinding;
import com.example.doclocker.databinding.AddCategoryDialogBinding;
import com.example.doclocker.databinding.AddPdfDialogBinding;
import com.example.doclocker.databinding.AddPdfDialogCategoryBinding;
import com.example.doclocker.databinding.BottomSheetLayoutBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kerols.pdfconverter.ImageToPdf;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    ActivityMainBinding binding;
    StorageReference storageReference;
    DatabaseReference dbRefUser,dbRefCatgeory,dbRef;
    CategoryAdapter adapter;
    Dialog pdfUploadDialog,imageToPdfDialog,addCategoryDialog,addPdfDialog;
    AddPdfDialogBinding addPdfDialogBinding;
    AddPdfDialogCategoryBinding addPdfDialogCategoryBinding;


    ImageToPdf imageToPdf;
    ArrayAdapter<String> categoryListAdapter;
    BottomSheetLayoutBinding bottomSheetLayoutBinding;
    ArrayList<ModalPdfRetrieverClass> pdfRetrieverList;
    int width;

    AddCategoryDialogBinding addCategoryDialogBinding;

    private Executor executor;


    String email,userName,selectedItem,password;
    UserDetails userDetails;
    static String path;
    ArrayList<String> categoryList,categorySpinnerList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        width = metrics.widthPixels;
        int height = metrics.heightPixels;
        executor = ContextCompat.getMainExecutor(this);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        email = user.getEmail();
        addPdfDialog = new Dialog(this);

        categoryList = new ArrayList<>();
        categorySpinnerList = new ArrayList<>();


        pdfRetrieverList = new ArrayList<>();

        pdfUploadDialog = new Dialog(this);
        addPdfDialogBinding = AddPdfDialogBinding.inflate(getLayoutInflater());
        pdfUploadDialog = new Dialog(MainActivity.this);
        pdfUploadDialog.setCancelable(true);
        pdfUploadDialog.setContentView(addPdfDialogBinding.getRoot());

        addCategoryDialog = new Dialog(this);

        addCategoryDialog.setCancelable(true);
        addCategoryDialogBinding = AddCategoryDialogBinding.inflate(getLayoutInflater());
        addCategoryDialog.setContentView(addCategoryDialogBinding.getRoot());

        imageToPdfDialog = new Dialog(MainActivity.this);
        bottomSheetLayoutBinding = BottomSheetLayoutBinding.inflate(getLayoutInflater());
        
        storageReference = FirebaseStorage.getInstance().getReference();
        dbRef = FirebaseDatabase.getInstance().getReference();
        adapter = new CategoryAdapter(categoryList,MainActivity.this);
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.setNestedScrollingEnabled(false);



        binding.bottomSheetNavigation.setSelectedItemId(R.id.home);
        binding.bottomSheetNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.contacts:
                        Intent intent = new Intent(getApplicationContext(), ContactActivity.class);
                        intent.putExtra("USERNAME",userName);
                        intent.putExtra("PASSWORD",password);
                        startActivity(intent);
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.profile:
                        Intent intentProfile = new Intent(getApplicationContext(), ProfileActivity.class);
                        intentProfile.putExtra("USERNAME",userName);
                        startActivity(intentProfile);
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.home:
                        return true;

                }
                return false;

            }
        });


        binding.uploadPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pdfUploadDialog.show();
                pdfUploadDialog.getWindow().setLayout((6 * width)/7, WindowManager.LayoutParams.WRAP_CONTENT);
                makeSpinnerList();

                categorySpinnerList.add("Add a category");
                categoryListAdapter = new ArrayAdapter<>(MainActivity.this, com.karumi.dexter.R.layout.support_simple_spinner_dropdown_item,categorySpinnerList);
                addPdfDialogBinding.categorySpinner.setAdapter(categoryListAdapter);

                addPdfDialogBinding.categorySpinner.setOnItemSelectedListener(MainActivity.this);
                addPdfDialogBinding.add.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        validateField();

                        makeList(userName);
                    }
                });



            }
        });

        binding.emptyUploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pdfUploadDialog.show();
                pdfUploadDialog.getWindow().setLayout((6 * width)/7, WindowManager.LayoutParams.WRAP_CONTENT);
                makeSpinnerList();

                categorySpinnerList.add("Add a category");
                categoryListAdapter = new ArrayAdapter<>(MainActivity.this, com.karumi.dexter.R.layout.support_simple_spinner_dropdown_item,categorySpinnerList);
                addPdfDialogBinding.categorySpinner.setAdapter(categoryListAdapter);

                addPdfDialogBinding.categorySpinner.setOnItemSelectedListener(MainActivity.this);
                addPdfDialogBinding.add.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        validateField();

                        makeList(userName);
                    }
                });
                binding.emptyUploadButton.setVisibility(View.INVISIBLE);
                binding.noDocsTv.setVisibility(View.INVISIBLE);
                binding.noDocsIv.setVisibility(View.INVISIBLE);
            }
        });
        dbRefUser = FirebaseDatabase.getInstance().getReference();
        dbRefUser.child("User Details").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot snapshot1 : snapshot.getChildren()){
                        userName = snapshot1.child("userName").getValue(String.class);
                        password = snapshot1.child("password").getValue(String.class);
                        makeList(userName);
                        String emailDataBase = snapshot1.child("email").getValue(String.class);
                        if (Objects.equals(email, emailDataBase)){
                            binding.userNameTv.setText(userName);

                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void validateField() {
        String text = addPdfDialogBinding.name.getEditText().getText().toString();
        String pass = addPdfDialogBinding.password.getEditText().getText().toString();

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

    private void makeSpinnerList() {
        DatabaseReference dbRefSpinner = FirebaseDatabase.getInstance().getReference(userName+" Uploads");
        categorySpinnerList.clear();
        dbRefSpinner.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                        categorySpinnerList.add(dataSnapshot.getKey());
                        categoryListAdapter = new ArrayAdapter<>(MainActivity.this, com.karumi.dexter.R.layout.support_simple_spinner_dropdown_item,categorySpinnerList);
                        addPdfDialogBinding.categorySpinner.setAdapter(categoryListAdapter);
                    }

                }
                categoryListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }



    private void makeList(String userName) {
        categoryList.clear();
        binding.progressBar.setVisibility(View.VISIBLE);
       dbRefCatgeory = FirebaseDatabase.getInstance().getReference(userName +" Uploads");
       dbRefCatgeory.addListenerForSingleValueEvent(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot snapshot) {
               if (snapshot.exists()){
                   for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                       String category = dataSnapshot.getKey();
                       categoryList.add(category);
                       binding.progressBar.setVisibility(View.INVISIBLE);
                   }
               }
               else{
                    binding.progressBar.setVisibility(View.INVISIBLE);
                    binding.noDocsIv.setVisibility(View.VISIBLE);
                    binding.noDocsTv.setVisibility(View.VISIBLE);
                    binding.emptyUploadButton.setVisibility(View.VISIBLE);
                    binding.uploadPdf.setVisibility(View.INVISIBLE);

               }
               adapter.notifyDataSetChanged();
           }

           @Override
           public void onCancelled(@NonNull DatabaseError error) {

           }
       });
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


            UploadFiles(data.getData(),selectedItem);
        }
    }



    private void UploadFiles(Uri data, String selectedItem) {
       final ProgressDialog progressDialog = new ProgressDialog(this);
       progressDialog.setTitle("Uploading the file....");
       progressDialog.show();
       dbRef = FirebaseDatabase.getInstance().getReference(userName+" Uploads");
       StorageReference reference = storageReference.child("Uploads/"+addPdfDialogBinding.name.getEditText().getText().toString()+".pdf");
       reference.putFile(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
           @Override
           public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
               Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                   while (!uriTask.isComplete());
                   Uri url = uriTask.getResult();
                   ModalPdfClass pdfClass = new ModalPdfClass(addPdfDialogBinding.name.getEditText().getText().toString(),url.toString(),addPdfDialogBinding.password.getEditText().getText().toString());
                   dbRef.child(selectedItem).child(addPdfDialogBinding.name.getEditText().getText().toString()).setValue(pdfClass);
                   Toast.makeText(MainActivity.this, "File uploaded", Toast.LENGTH_SHORT).show();
                   pdfUploadDialog.dismiss();
                   progressDialog.dismiss();

           }
       }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
           @Override
           public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                  double progress = (100*snapshot.getBytesTransferred())/snapshot.getTotalByteCount();
                  progressDialog.setMessage("Uploaded"+(int)progress+" %");
                  pdfUploadDialog.cancel();

           }
       });
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        selectedItem = parent.getItemAtPosition(position).toString();
        Log.d("Itemmmmm", ""+selectedItem);
        if (selectedItem.equals("Add a category")){
            addCategoryDialog.getWindow().setLayout((6*width)/7,WindowManager.LayoutParams.WRAP_CONTENT);
            addCategoryDialog.show();
            addCategoryDialogBinding.add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String category = addCategoryDialogBinding.category.getEditText().getText().toString();
                    if (category.isEmpty()){
                        Toast.makeText(MainActivity.this, "Please give a category ", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        categorySpinnerList.add(category);
                        addPdfDialogBinding.categorySpinner.setAdapter(categoryListAdapter);
                        Toast.makeText(MainActivity.this, "Category added successfully", Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}