package com.example.multipleimageupload;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    Button upload, choose;
    TextView alert;
    private static final int pickImage = 1;

    ArrayList<Uri> ImageList = new ArrayList<Uri>();

    private Uri ImageUri;
    private ProgressDialog progressDialog;
    private int upload_count = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        alert = findViewById(R.id.alert);
        choose = findViewById(R.id.chooser);
        upload = findViewById(R.id.upload_image);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("image  uploading please wait...!");


        choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
                startActivityForResult(intent, pickImage);
            }
        });

        upload.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {

                progressDialog.show();
                alert.setText("if loading takes too much time please press the button again!");
                StorageReference ImageFolder = FirebaseStorage.getInstance().getReference().child("ImageFolder");

                for (upload_count = 0; upload_count < ImageList.size(); upload_count++){

                    Uri individualImage = ImageList.get(upload_count);
                    final StorageReference ImageName = ImageFolder.child("Image"+individualImage.getLastPathSegment());
                    ImageName.putFile(individualImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            ImageName.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String url = String.valueOf(uri);

                                    storeLink(url);
                                }
                            });

                        }
                    });

                }

            }
        });


    }

    @SuppressLint("SetTextI18n")
    private void storeLink(String url) {

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("singleUser");

        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("imgLink", url);

        databaseReference.push().setValue(hashMap);

        progressDialog.dismiss();
        alert.setText("Image  uploaded successfully!");
        upload.setVisibility(View.GONE);
        ImageList.clear();
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == pickImage){
            if (resultCode == RESULT_OK){

                if (data.getClipData() != null){

                    int countClipData = data.getClipData().getItemCount();
                    int currentImageSelected = 0;

                    while(currentImageSelected < countClipData){

                        ImageUri = data.getClipData().getItemAt(currentImageSelected).getUri();
                        ImageList.add(ImageUri);
                        currentImageSelected+=1;



                    }
                    alert.setVisibility(View.VISIBLE);
                    alert.setText("You have selected "+ImageList.size()+" images!");

                    choose.setVisibility(View.GONE);

                }else{
                    Toast.makeText(MainActivity.this, "please select images", Toast.LENGTH_SHORT).show();
                }

            }
        }
    }
}
