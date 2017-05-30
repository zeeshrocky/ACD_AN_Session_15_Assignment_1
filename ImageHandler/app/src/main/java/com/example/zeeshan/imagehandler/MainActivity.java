package com.example.zeeshan.imagehandler;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    //private ProgressDialog progressDialog;
    private ProgressBar progressBar;
    //private int mProgressStatus = 0;
    EditText editText;
    private ImageView imageView1,imageView2;
    File fileForBitmapImage;
    File makeDirectory;

    //File imagePath;
    //private static final int PERMISSIONS_REQUEST_WRITE_IMAGE = 100;
    static final Integer WRITE_EXST = 0x3;
    static final Integer READ_EXST = 0x4;
    public static String strpath =  android.os.Environment.getExternalStorageDirectory().toString();
    public static String dirName = "DIR_NAME";
    private String url = "http://www.kingofwallpapers.com/wallpaper-cool/wallpaper-cool-018.jpg";
    String et_url;
    private Bitmap bitmap = null;
    Button downButton, showSDButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView1 = (ImageView) findViewById(R.id.imageView1);
        imageView2 = (ImageView) findViewById(R.id.imageView2);

        progressBar =(ProgressBar) findViewById(R.id.pbar);
        progressBar.setVisibility(View.INVISIBLE);
        editText = (EditText) findViewById(R.id.et_imageurl);

        downButton = (Button) findViewById(R.id.button1);
        showSDButton = (Button) findViewById(R.id.button2);
        downButton.setOnClickListener(this);
        showSDButton.setOnClickListener(this);

    }

    private Handler messageHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            imageView1.setImageBitmap(bitmap);
            progressBar.setVisibility(View.INVISIBLE);
        }
    };

    private void askForPermission(String permission, Integer requestCode) {
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permission)) {
                //This is called if user has denied the permission before
                //In this case I am just asking the permission again
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, requestCode);
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, requestCode);
            }
        } else {
            //Toast.makeText(this, "" + permission + " is already granted.", Toast.LENGTH_SHORT).show();
        }
    }

    //    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if(ActivityCompat.checkSelfPermission(this, permissions[0]) == PackageManager.PERMISSION_GRANTED){
//            switch (requestCode) {
//                //Write external Storage
//                case 3:
//                   // writeFile();
//                    break;
//                //Read External Storage
//                case 4:
////                    Intent imageIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
////                    startActivityForResult(imageIntent, 11);
//                    break;
//            }
//
//            Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
//        }else{
//            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
//        }
//    }
    public boolean saveImageInSDCard(Bitmap bitmap){
        boolean success = false;
        //Create first dir in your sd card :
        makeDirectory = new File(strpath+"/"+dirName);
        makeDirectory.mkdir();
        String filename =  "yourImageName"+".png";
        String dirpath =strpath + "/"+dirName + "/";

        FileOutputStream outStream;
        try {
            File storagePath =  new File(dirpath);
            fileForBitmapImage = new File(storagePath, filename);
            outStream = new FileOutputStream(fileForBitmapImage);
            //outStream.write(bitmap);
            success = bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            Log.e("SISD","COMPRESS");
            outStream.flush();
            Log.e("SISD","FLUSH");
            outStream.close();
            Log.e("SISD","CLOSE");
//            Log.e("SISD",imagePath.toString());
//            sdCardDirectory.mkdirs();
//            Log.e("SISD","mkDirs");
//            outStream = new FileOutputStream(imagePath);
//            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);/* 100 to keep full quality of the imagePath */
//            Log.e("SISD","COMPRESS");
//            outStream.flush();
//            Log.e("SISD","FLUSH");
//            outStream.close();
//            Log.e("SISD","CLOSE");

        } catch (FileNotFoundException e) {
            Log.e("SISD","FILE NOT FOUND");
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("SISD","IO EXCEPTION");
            e.printStackTrace();
        }
        Log.e("SISD"," FILE WRITTEN SUCCESS");
        return success;
    }

    @Override
    public void onClick(View v) {
        ask(v);
    }

    public void ask(View v){
        switch (v.getId()){
            case R.id.button1:
                askForPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,WRITE_EXST);
                downloadValidImageFromInternet();
                break;
            case R.id.button2:
                askForPermission(Manifest.permission.READ_EXTERNAL_STORAGE,READ_EXST);
                showImageFromSD();
                break;
            default:
                break;
        }
    }



    public void downloadValidImageFromInternet(){
        et_url = editText.getText().toString();
        if(checkURL(et_url)){
            url = et_url;
        }else {
            Toast.makeText(getApplicationContext(),"Not Valid URL",Toast.LENGTH_SHORT);
        }
        progressBar.setVisibility(View.VISIBLE);

        new Thread() {
            public void run() {
                bitmap = downloadBitmap(url);
                messageHandler.sendEmptyMessage(0);
            }
        }.start();
    }

    private Bitmap downloadBitmap(String imageURL) {
        bitmap = null;
        try {
            // Download Image from URL
            InputStream input = new java.net.URL(imageURL).openStream();
            // Decode Bitmap
            bitmap = BitmapFactory.decodeStream(input);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        Boolean flag = false;
//        if(askPermission()){
        Boolean flag = saveImageInSDCard(bitmap);
        //}

        if (flag) {
            Log.e("DB ","Image saved");
        } else {
            Log.e("DB ","Image NOT saved");
        }
        return bitmap;
    }

    public static boolean checkURL(CharSequence input) {
        if (TextUtils.isEmpty(input)) {
            return false;
        }
        Pattern URL_PATTERN = Patterns.WEB_URL;
        boolean isURL = URL_PATTERN.matcher(input).matches();
        if (!isURL) {
            String urlString = input + "";
            if (URLUtil.isNetworkUrl(urlString)) {
                try {
                    new URL(urlString);
                    isURL = true;
                } catch (Exception ignored) {
                }
            }
        }
        return isURL;
    }

    private void showImageFromSD() {
        if(fileForBitmapImage.exists()){
            imageView2.setImageBitmap(BitmapFactory.decodeFile(fileForBitmapImage.getAbsolutePath()));
        }else {
            Log.e("showImg ","FILE NOT EXIST");
        }

    }
}