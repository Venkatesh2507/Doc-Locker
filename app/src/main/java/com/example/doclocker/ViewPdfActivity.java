package com.example.doclocker;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.example.doclocker.databinding.ActivityViewPdfBinding;

import java.net.URLEncoder;

public class ViewPdfActivity extends AppCompatActivity {
   ActivityViewPdfBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityViewPdfBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        String fileName = getIntent().getStringExtra("filename");
        String fileUrl = getIntent().getStringExtra("fileurl");
        Log.d("Urllll", ""+fileUrl);
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(fileName);
        progressDialog.setMessage("Opening....");
        binding.webView.getSettings().setJavaScriptEnabled(true);
        binding.webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressDialog.show();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressDialog.cancel();
            }
        });
        String url = "";
        try {
            url = URLEncoder.encode(fileUrl,"UTF-8");


        }catch (Exception e){
            e.printStackTrace();
        }

        binding.webView.loadUrl("https://docs.google.com/gview?embedded=true&url="+url);
    }
}