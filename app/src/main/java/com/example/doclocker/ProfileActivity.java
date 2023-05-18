package com.example.doclocker;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.doclocker.databinding.ActivityProfileBinding;
import com.google.android.gms.tasks.OnFailureListener;
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
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.concurrent.Executor;

public class ProfileActivity extends AppCompatActivity {
    ActivityProfileBinding binding;
    String userName,email,password;
    DatabaseReference dbRef,dbRefContacts,dbRefPass,ddbRefUpdate,dbRefProfile;
    private Uri imageUri=null;
    FirebaseUser user;
    private Executor executor;
    public int width;
    Dialog editDialog;
    private static final String tag = "PROFILE_EDIT_TAG";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        userName = getIntent().getStringExtra("USERNAME");
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        width = metrics.widthPixels;
        int height = metrics.heightPixels;
        executor = ContextCompat.getMainExecutor(this);
        editDialog = new Dialog(this);
        user = FirebaseAuth.getInstance().getCurrentUser();
        email = user.getEmail();
        dbRef = FirebaseDatabase.getInstance().getReference(userName + " Uploads");
        dbRefContacts = FirebaseDatabase.getInstance().getReference("User Details");
        dbRefPass = FirebaseDatabase.getInstance().getReference("User Details");
        ddbRefUpdate = FirebaseDatabase.getInstance().getReference("User Details");
        dbRefProfile = FirebaseDatabase.getInstance().getReference("User Details");

        dbRefPass.child(userName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
               password = snapshot.child("password").getValue(String.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        binding.bottomSheetNavigation.setSelectedItemId(R.id.profile);
        binding.bottomSheetNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.contacts:
                        Intent intentContacts = new Intent(ProfileActivity.this, ContactActivity.class);
                        Log.d("nameeee", ""+userName);
                        intentContacts.putExtra("USERNAME",userName);
                        intentContacts.putExtra("PASSWORD",password);
                        Log.d("Passss", ""+password);
                        startActivity(intentContacts);
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.profile:
                        return true;
                    case R.id.home:
                        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                        intent.putExtra("USERNAME",userName);
                        startActivity(intent);
                        overridePendingTransition(0, 0);
                        return true;

                }
                return false;

            }
        });

        loadUserInfo();


        binding.profilePic.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showImageAttachMenu();
                return false;
            }
        });
        binding.logOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                  FirebaseAuth.getInstance().signOut();
                  startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
            }
        });

    }

    private void showImageAttachMenu() {

        PopupMenu popupMenu = new PopupMenu(this,binding.profilePic);
        popupMenu.getMenu().add(Menu.NONE,0,0,"Camera");
        popupMenu.getMenu().add(Menu.NONE,1,1,"Gallery");

        popupMenu.show();
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int which = item.getItemId();
                if (which == 0) {
                    pickImageCamera();

                } else if(which==1) {
                    pickGalleryCamera();
                }
                return false;
            }
        });
    }

    private void pickImageCamera() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE,"New pick");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION,"Sample Image Description");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
        cameraActivityResultLauncher.launch(intent);


    }

    private void pickGalleryCamera() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryActivityResultLauncher.launch(intent);
    }

    private ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
             if (result.getResultCode()== Activity.RESULT_OK){
                 Intent data = result.getData();
                 binding.profilePic.setImageURI(imageUri);
                 updateProfile(imageUri);
             }
             else {
                 Toast.makeText(ProfileActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
             }
        }
    });

    private void updateProfile(Uri imageUri) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Update");
        progressDialog.setMessage("Updating your profile");
        progressDialog.show();

        String filePathAndName = "ProfileImage "+userName;
        StorageReference ref = FirebaseStorage.getInstance().getReference(filePathAndName);
        ref.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while(!uriTask.isSuccessful());
                String uploadImageUrl = ""+uriTask.getResult();
                HashMap<String,Object> profileMap = new HashMap<>();
                profileMap.put("profileImage",uploadImageUrl);
                ddbRefUpdate.child(userName).updateChildren(profileMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Toast.makeText(ProfileActivity.this, "Updated profile image successfully", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ProfileActivity.this, "Profile not updated successfully", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ProfileActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });
    }

    private ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode()== Activity.RESULT_OK){
                Intent data = result.getData();
                imageUri = data.getData();
                binding.profilePic.setImageURI(imageUri);
                updateProfile(imageUri);
            }
            else {
                Toast.makeText(ProfileActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
            }
        }
    });
    private void loadUserInfo() {
        binding.userNameTv.setText(userName);
        binding.emailTv.setText(email);

        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = (int) snapshot.getChildrenCount();
                binding.countTv.setText(Integer.toString(count));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        dbRefContacts.child(userName).child("Contact Details").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = (int) snapshot.getChildrenCount();
                binding.contactsTv.setText(Integer.toString(count));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        dbRefProfile.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                        String profileImage = dataSnapshot.child("profileImage").getValue(String.class);
                        Glide.with(ProfileActivity.this)
                                .load(profileImage)
                                .placeholder(R.drawable.baseline_person_24)
                                .into(binding.profilePic);
                        Log.d("Valueeee", ""+profileImage);


                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}