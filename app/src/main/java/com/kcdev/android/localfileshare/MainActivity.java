package com.kcdev.android.localfileshare;

import java.io.ByteArrayOutputStream;
import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ShareActionProvider;

import com.kcdev.android.getimage.R;


public class MainActivity extends Activity implements OnClickListener {

    Button btnSelectImage;
    Button btnSendImage;
    ImageView imageView;
    Button btnTakePhoto;
    private static final int CAM_REQUEST = 1313;
    private ShareActionProvider mShareActionProvider;
    Uri globalUri;

    //Creating share menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu resource file.
        getMenuInflater().inflate(R.menu.share_menu, menu);

        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.menu_item_share);

        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) item.getActionProvider();

        // Return true to display menu
        return true;
    }

    // Call to update the share intent
    private void setShareIntent(Intent shareIntent) {
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    class buttonTakePhotoClick implements Button.OnClickListener
    {
        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub

            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, CAM_REQUEST);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // get reference to views
        btnSelectImage = (Button) findViewById(R.id.btnSelectImage);
        btnTakePhoto = (Button) findViewById(R.id.btnTakePhoto);
        btnSendImage = (Button) findViewById(R.id.btnSendImage);
        imageView = (ImageView) findViewById(R.id.imgView);

        // add click listener to button
        btnSelectImage.setOnClickListener(this);
        btnSendImage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v){
                sendImage(v);
            }
        });
        btnTakePhoto.setOnClickListener(new buttonTakePhotoClick());

    }

    public void sendImage(View view){
        Intent shareIntent2 = new Intent();
        shareIntent2.setAction(Intent.ACTION_SEND);
        shareIntent2.putExtra(Intent.EXTRA_STREAM, globalUri);
        shareIntent2.setType("image/jpeg");
        setShareIntent(shareIntent2);
        startActivity(Intent.createChooser(shareIntent2, "Send Photo..."));
    }

    @Override
    public void onClick(View view) {

        // 1. on Upload click call ACTION_GET_CONTENT intent
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        // 2. pick image only
        intent.setType("image/*");
        // 3. start activity
        startActivityForResult(intent, 0);

        // define onActivityResult to do something with picked image

    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    @Override
    protected void onActivityResult(int reqCode, int resCode, Intent data) {
        if(resCode == Activity.RESULT_OK && data != null){
            String realPath;
            // SDK < API11
            if (Build.VERSION.SDK_INT < 11)
                realPath = RealPathUtil.getRealPathFromURI_BelowAPI11(this, data.getData());

                // SDK >= 11 && SDK < 19
            else if (Build.VERSION.SDK_INT < 19)
                realPath = RealPathUtil.getRealPathFromURI_API11to18(this, data.getData());

                // SDK > 19 (Android 4.4)
            else
                realPath = RealPathUtil.getRealPathFromURI_API19(this, data.getData());


            setTextViews(Build.VERSION.SDK_INT, data.getData().getPath(),realPath);
        }
        if (reqCode == CAM_REQUEST) {
            Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(thumbnail);

            String realPath;
            // SDK < API11
            if (Build.VERSION.SDK_INT < 11)
                realPath = RealPathUtil.getRealPathFromURI_BelowAPI11(this, data.getData());

                // SDK >= 11 && SDK < 19
            else if (Build.VERSION.SDK_INT < 19)
                realPath = RealPathUtil.getRealPathFromURI_API11to18(this, data.getData());

                // SDK > 19 (Android 4.4)
            else
                realPath = RealPathUtil.getRealPathFromURI_API19(this, data.getData());


            setTextViews(Build.VERSION.SDK_INT, data.getData().getPath(),realPath);


            /*Uri uriFromPath = getImageUri(getApplicationContext(), thumbnail);
            globalUri = uriFromPath;

            imageView.setImageURI(uriFromPath);
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, uriFromPath);
            shareIntent.setType("image/jpeg");
            setShareIntent(shareIntent);*/
        }
    }

    private void setTextViews(int sdk, String uriPath,String realPath){

        Uri uriFromPath = Uri.fromFile(new File(realPath));
        globalUri = uriFromPath;

        // you have two ways to display selected image

        // ( 1 ) imageView.setImageURI(uriFromPath);

        // ( 2 ) imageView.setImageBitmap(bitmap);
        /*
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uriFromPath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        imageView.setImageBitmap(bitmap);
        */

        imageView.setImageURI(uriFromPath);

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uriFromPath);
        shareIntent.setType("image/jpeg");
        setShareIntent(shareIntent);

    }

}