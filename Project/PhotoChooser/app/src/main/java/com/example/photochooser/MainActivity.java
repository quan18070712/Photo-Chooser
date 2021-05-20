package com.example.photochooser;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    Button btnCam;
    Button btnEdit;
    private static final int PICK_IMAGE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Open Camera
        btnCam = findViewById(R.id.camera);
        btnCam.setOnClickListener(view -> {
            try {
                Intent intent= new Intent();
                intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivity(intent);
            }catch (Exception e){
                e.printStackTrace();
            }
        });

        // Open Gallery
        btnEdit = findViewById(R.id.gallery);
        btnEdit.setOnClickListener(v -> openGallery());
    }

    private void openGallery() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE);
    }

    private static final int REQUEST_PERMISSIONS = 1234;

    private static final String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
//            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private static final int PERMISSIONS_COUNT = 1;

    @SuppressLint("NewApi")
    private boolean arePermissionsDenied(){
        for (int i = 0; i < PERMISSIONS_COUNT; i++){
            if (checkSelfPermission(PERMISSIONS[i]) != PackageManager.PERMISSION_GRANTED){
                return true;
            }
        }
        return false;
    }

    @SuppressLint("NewApi")
    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSIONS && grantResults.length > 0){
            if (arePermissionsDenied()){
                ((ActivityManager) Objects.requireNonNull(this.getSystemService(ACTIVITY_SERVICE)))
                        .clearApplicationUserData();
                recreate();
            }else {
                onResume();
            }
        }
    }

    private List<String> fileList;
    private void addImagesFrom(String dirPath){
        final File imagesDir = new File(dirPath);
        final File[] files = imagesDir.listFiles();

        assert files != null;
        for (File file : files) {
            final String path = file.getAbsolutePath();
            if (path.endsWith(".jpg") || path.endsWith(".png") || path.endsWith(".jepg")) {
                fileList.add(path);
            }
        }
    }

    private boolean isGalleryInitialized;

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && arePermissionsDenied()){
            requestPermissions(PERMISSIONS, REQUEST_PERMISSIONS);
            return;
        }
        // Initialize my app
        if (!isGalleryInitialized){
            fileList = new ArrayList<>();
            addImagesFrom(String.valueOf(Environment.
                    getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)));
            addImagesFrom(String.valueOf(Environment.
                    getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)));
            addImagesFrom(String.valueOf(Environment.
                    getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)));

            final ListView listView = findViewById(R.id.listView);
            final GalleryAdapter galleryAdapter = new GalleryAdapter();

            galleryAdapter.setData(fileList);
            listView.setAdapter(galleryAdapter);

            isGalleryInitialized = true;
        }
    }

    final class GalleryAdapter extends BaseAdapter {

        private final List<String> data = new ArrayList<>();

        void setData(List<String> data){
            if (this.data.size() > 0){
                data.clear();
            }

            this.data.addAll(data);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ImageView imageView;
            
            if (convertView  == null){
                imageView = (ImageView) LayoutInflater.from(parent.getContext()).inflate(R.layout.item,
                        parent, false);
            }else {
                imageView = (ImageView) convertView;
            }

            Glide.with(MainActivity.this).load(data.get(position)).centerCrop().into(imageView);
            
            return imageView;
        }
    }
}
