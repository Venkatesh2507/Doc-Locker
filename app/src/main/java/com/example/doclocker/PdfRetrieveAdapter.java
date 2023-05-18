package com.example.doclocker;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doclocker.databinding.PdfCardviewBinding;

import java.util.ArrayList;

public class PdfRetrieveAdapter extends RecyclerView.Adapter<PdfRetrieveAdapter.Viewholder> {
   ArrayList<ModalPdfRetrieverClass> pdfList;
   DialogListner mListner;
   Context context;
   public PdfRetrieveAdapter(ArrayList<ModalPdfRetrieverClass> pdfList,Context context,DialogListner mListner){
       this.context = context;
       this.pdfList = pdfList;
       this.mListner = mListner;
   }


    @NonNull
    @Override
    public PdfRetrieveAdapter.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pdf_cardview,parent,false);
       return new Viewholder(view,mListner);
    }

    @Override
    public void onBindViewHolder(@NonNull PdfRetrieveAdapter.Viewholder holder, int position) {
       ModalPdfRetrieverClass m = pdfList.get(position);
        holder.binding.downloadPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setType("application/pdf");
                intent.setData(Uri.parse(m.getUrl()));
                holder.binding.downloadPdf.getContext().startActivity(intent);

            }
        });

        holder.binding.nameTv.setText(m.getName());
        String docUrl = m.getUrl();
        String docTitle = m.getName();
        PdfHelper.loadPdfSize(docUrl,docTitle,holder.binding.sizeTv);
        holder.binding.viewPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    int flag=0;
                    if (flag==0){
                        mListner.showDialog(view);
                        mListner.checkPassword(m.getName());
                    }
            }
        });

        holder.binding.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               mListner.deleteDocument(m.getName());
            }
        });
    }


    @Override
    public int getItemCount() {
        return pdfList.size();
    }

    public class Viewholder extends RecyclerView.ViewHolder {
       PdfCardviewBinding binding;
       DialogListner mListner;
       public Viewholder(@NonNull View itemView,DialogListner dialogListner) {
            super(itemView);
            this.mListner = dialogListner;
            binding = PdfCardviewBinding.bind(itemView);
        }
    }
    interface DialogListner{
       void showDialog(View view);
       void checkPassword(String nameOfPdf);
       void deleteDocument(String name);
    }
}
