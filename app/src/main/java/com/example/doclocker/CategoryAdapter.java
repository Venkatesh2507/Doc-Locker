package com.example.doclocker;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doclocker.databinding.CategoryCardviewBinding;

import java.util.ArrayList;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {
    public ArrayList<String> categoryList;
    public Context context;

    public CategoryAdapter(ArrayList<String> categoryList, Context context) {
        this.categoryList = categoryList;
        this.context = context;
    }

    @NonNull
    @Override
    public CategoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_cardview,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryAdapter.ViewHolder holder, int position) {
      String category = categoryList.get(position);
      holder.binding.nameTv.setText(category);

      holder.itemView.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              Intent intent = new Intent(context, PdfCategoryActivity.class);
              intent.putExtra("CATEGORY_NAME",category);
              context.startActivity(intent);
          }
      });




    }

    @Override
    public int getItemCount() {
       return categoryList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CategoryCardviewBinding binding;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = CategoryCardviewBinding.bind(itemView);

        }
    }
}
