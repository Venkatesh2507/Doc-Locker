package com.example.doclocker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.doclocker.databinding.ActivityContactBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.concurrent.Executor;

public class ContactActivity extends AppCompatActivity implements ContactDetailsAdapter.ShareCredentials {
   ActivityContactBinding binding;
   Dialog contactsDialog;
   int width;
   Executor executor;
   String contactName,contactNumber,userName,email,password,message;
    AlertDialog alertDialog;
   DatabaseReference dbRef,dbRefDocs,dbRefContacts;
   private static final int CONTACT_PERMISSION_CODE=1;
   ContactDetailsAdapter adapter;
   ArrayList<ModalContactDetails> contactDetails;
   private static final int CONTACT_PICK_CODE=2;
   private int contactsCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityContactBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        contactDetails = new ArrayList<>();
        adapter = new ContactDetailsAdapter(ContactActivity.this,contactDetails,this);
        binding.bottomSheetNavigation.setSelectedItemId(R.id.contacts);
        binding.bottomSheetNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.contacts:
                        return true;
                    case R.id.profile:
                        Intent intent = new Intent(ContactActivity.this, ProfileActivity.class);
                        intent.putExtra("USERNAME",userName);
                        startActivity(intent);
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.home:
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        overridePendingTransition(0,0);
                        return true;

                }
                return false;

            }
        });

        userName = getIntent().getStringExtra("USERNAME");

        dbRefDocs = FirebaseDatabase.getInstance().getReference();
        binding.contactAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkContactPermission()){
                    DatabaseReference dbContacts = FirebaseDatabase.getInstance().getReference("User Details");
                    dbContacts.child(userName).child("Contact Details").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            contactsCount = (int) snapshot.getChildrenCount();
                            if (contactsCount < 3){
                                pickContactIntent();
                            }
                            else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(ContactActivity.this);
                                builder.setIcon(R.drawable.warning);
                                builder.setTitle("Alert!!!!");
                                builder.setMessage("Due to the privacy reasons you can enter only 3 beneficiary contacts who can access your credentials. If you" +
                                        "want to add this contact then please remove a contact from your beneficiary list");
                                builder.show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });



                }
                else {
                    requestContactPermission();
                }

            }
        });
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.setNestedScrollingEnabled(false);
        makeList();

    }



    private void makeList() {
        contactDetails.clear();
        binding.progressBar.setVisibility(View.VISIBLE);
        dbRef = FirebaseDatabase.getInstance().getReference("User Details");
        dbRef.child(userName).child("Contact Details").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot snapshot1 : snapshot.getChildren()){
                        ModalContactDetails m = snapshot1.getValue(ModalContactDetails.class);
                        contactDetails.add(m);
                        binding.progressBar.setVisibility(View.INVISIBLE);
                        binding.noContactsIv.setVisibility(View.INVISIBLE);
                        binding.noContactsTv.setVisibility(View.INVISIBLE);
                    }
                }
                else{
                    binding.progressBar.setVisibility(View.INVISIBLE);
                    binding.noContactsIv.setVisibility(View.VISIBLE);
                    binding.noContactsTv.setVisibility(View.VISIBLE);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void requestContactPermission() {
        String[] permission = {Manifest.permission.READ_CONTACTS};
        ActivityCompat.requestPermissions(this,permission,CONTACT_PERMISSION_CODE);
    }

    private void pickContactIntent() {
        Intent intent = new Intent(Intent.ACTION_PICK,ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent,CONTACT_PICK_CODE);
    }

    private boolean checkContactPermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                ==(PackageManager.PERMISSION_GRANTED);
        return result;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==CONTACT_PERMISSION_CODE){
            if (grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                pickContactIntent();
            }
            else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("Range")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode==RESULT_OK){
            if (requestCode==CONTACT_PICK_CODE){
                Cursor cursor1,cursor2;
                assert data != null;
                Uri uri = data.getData();
                cursor1 = getContentResolver().query(uri,null,null,null,null);
                if (cursor1.moveToFirst()){
                    contactName = cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    @SuppressLint("Range") String contactId = cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts._ID));
                    @SuppressLint("Range") String idResults = cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                    int idResultsHold = Integer.parseInt(idResults);

                    if (idResultsHold==1) {
                        cursor2 = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null,
                                null);
                        while (cursor2.moveToNext()){
                            contactNumber = cursor2.getString(cursor2.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        }
                        cursor2.close();
                    }
                }
                cursor1.close();
                addToDatabase(contactName,contactNumber);

            }
        }
    }

    private void addToDatabase(String contactName, String contactNumber) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ContactActivity.this);
        builder.setMessage("Are you sure you want to add this contact?");
        builder.setTitle("Alert !");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", (DialogInterface.OnClickListener) (dialog, which) -> {
            binding.progressBar.setVisibility(View.VISIBLE);
            dbRef = FirebaseDatabase.getInstance().getReference("User Details");
            ModalContactDetails m = new ModalContactDetails(contactName,contactNumber);
            dbRef.child(userName).child("Contact Details").child(contactName).setValue(m).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Toast.makeText(ContactActivity.this, "Contact added successfully", Toast.LENGTH_SHORT).show();
                    binding.progressBar.setVisibility(View.INVISIBLE);
                    makeList();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(ContactActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            });

        });

        builder.setNegativeButton("No", (DialogInterface.OnClickListener) (dialog, which) -> {
            dialog.cancel();
        });

        alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void sendCredentials(String contactNumber) {

        AlertDialog.Builder builder = new AlertDialog.Builder(ContactActivity.this);
        builder.setMessage("Are you sure you want to send your credentials to this number");
        builder.setTitle("Alert !");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", (DialogInterface.OnClickListener) (dialog, which) -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            email = user.getEmail();
            password = getIntent().getStringExtra("PASSWORD");
            Log.d("Passs", ""+password);
            message = "Hii These are my login credentials for my Doc locker account \n Email: "+email+"\n Password: "+password;
            if (isWhatsappInstalled()) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=" + contactNumber + "&text=" + message));
                startActivity(intent);
            } else {
                Toast.makeText(ContactActivity.this, "Please install whatsapp", Toast.LENGTH_SHORT).show();
            }


        });

        builder.setNegativeButton("No", (DialogInterface.OnClickListener) (dialog, which) -> {
            dialog.cancel();
        });

        alertDialog = builder.create();
        alertDialog.show();


    }

    @Override
    public void deleteContacts(String contactName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ContactActivity.this);
        builder.setIcon(R.drawable.warning);
        builder.setMessage("Are you sure you want to delete this contact?");
        builder.setTitle("Alert !");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", (DialogInterface.OnClickListener) (dialog, which) -> {
            DatabaseReference dbref= FirebaseDatabase.getInstance().getReference("User Details").child(userName).child("Contact Details");
            Query query=dbref.child(contactName);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    // remove the value at reference
                    dataSnapshot.getRef().removeValue();
                    Toast.makeText(ContactActivity.this, "Contacts deleted successfully", Toast.LENGTH_SHORT).show();
                    contactDetails.clear();
                    makeList();
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

    private boolean isWhatsappInstalled(){
        boolean whatsappInstalled;
        PackageManager packageManager = getPackageManager();
        try {
            packageManager.getPackageInfo("com.whatsapp",PackageManager.GET_ACTIVITIES);
            whatsappInstalled=true;

        } catch (PackageManager.NameNotFoundException e) {
            whatsappInstalled=false;
            throw new RuntimeException(e);
        }
        return whatsappInstalled;
    }



}
