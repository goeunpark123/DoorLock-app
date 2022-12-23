package com.example.doorlockapp;

import android.content.Intent;
import android.util.Log;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

public class ShowPicActivity extends AppCompatActivity {

    public static final String TAG = "ShowPicActivity";
    ImageView img;
    String file;
    String[] pic_name;
    ListView listview;
    ListViewAdapter adapter;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_pic);

        img = (ImageView)findViewById(R.id.pic);

        adapter = new ListViewAdapter();

        listview = (ListView)findViewById(R.id.p_list);
        listview.setAdapter(adapter);

        final StorageReference storage = FirebaseStorage.getInstance().getReference();
        StorageReference pathReference = storage.child("photo");

        if (pathReference==null) {
            Toast.makeText(ShowPicActivity.this, "저장소에 사진이 없습니다." ,Toast.LENGTH_SHORT).show();
        }

        else {
            pathReference.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {

                @Override
                public void onSuccess(ListResult listResult) {
                    file = listResult.getItems().toString().replace(" ", "");
                    file = file.substring(1, file.length()-1);
                    pic_name = file.split(",");

                    for (int i=0; i<pic_name.length; i++) {
                        pic_name[i] = pic_name[i].substring(36);
                        adapter.addItem("11:30", pic_name[i]);
                    }
                    adapter.notifyDataSetChanged();
                }
            });

            StorageReference pic_list = storage.child("photo/WIN_20201014_10_15_12_Pro.jpg");       // 오류 발생하는 부분!

            pic_list.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {

                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(ShowPicActivity.this).load(uri).into(img);
                }

            }).addOnFailureListener(new OnFailureListener() {

                @Override
                public void onFailure(@NonNull Exception e) {
                }
            });
        }
    }

}
