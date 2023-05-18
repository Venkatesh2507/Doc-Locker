package com.example.doclocker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doclocker.databinding.ContactDetailsCardviewBinding;

import java.util.ArrayList;

public class ContactDetailsAdapter extends RecyclerView.Adapter<ContactDetailsAdapter.ViewHolder> {
    Context context;
    ShareCredentials mCredentials;
    ArrayList<ModalContactDetails> contactDetails;

    public ContactDetailsAdapter(Context context, ArrayList<ModalContactDetails> contactDetails,ShareCredentials shareCredentials) {
        this.context = context;
        this.contactDetails = contactDetails;
        this.mCredentials = shareCredentials;
    }

    @NonNull
    @Override
    public ContactDetailsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_details_cardview,parent,false);
        return new ViewHolder(view,mCredentials);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ModalContactDetails m =  contactDetails.get(position);
        holder.binding.nameTv.setText(m.getContactName());

        holder.binding.shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCredentials.sendCredentials(m.getContactNumber());
            }
        });

        holder.binding.deleteContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCredentials.deleteContacts(m.getContactName());
            }
        });
    }

    @Override
    public int getItemCount() {
        return contactDetails.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ContactDetailsCardviewBinding binding;
        public ViewHolder(@NonNull View itemView,ShareCredentials shareCredentials) {
            super(itemView);
            binding = ContactDetailsCardviewBinding.bind(itemView);

        }
    }
    interface ShareCredentials{
        void sendCredentials(String contactNumber);
        void deleteContacts(String contactName);
    }
}
