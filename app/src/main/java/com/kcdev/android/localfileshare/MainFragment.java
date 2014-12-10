package com.kcdev.android.localfileshare;

import android.app.Activity;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ShareActionProvider;
import android.widget.Toast;

import com.kcdev.android.getimage.R;

//importing parse SDK and needed features
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment {

    Button btnSelectImage;
    Button btnSendImage;
    ImageView imageView;
    ImageButton btnTakePhoto;
    String currPhotoPath;
    String currPhotoPathAbsolute;

    private static final int CAM_REQUEST = 1313;
    private ShareActionProvider mShareActionProvider;
    Uri globalUri;

    //variables for location data
    private LocationManager locationManager;
    private String provider;
    private int latitude = 0;
    private int longitude = 0;
    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                latitude = (int) location.getLatitude();
                longitude = (int) location.getLongitude();
            }
            else {
                latitude = 0;
                longitude = 0;
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_main, container, false);
        // get reference to views
        btnSelectImage = (Button) rootView.findViewById(R.id.btnSelectImage);
        btnTakePhoto = (ImageButton) rootView.findViewById(R.id.btnTakePhoto);
        btnSendImage = (Button) rootView.findViewById(R.id.btnSendImage);
        imageView = (ImageView) rootView.findViewById(R.id.imgView);

        // add click listener to button
        btnSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 1. on Upload click call ACTION_GET_CONTENT intent
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                // 2. pick image only
                intent.setType("image/*");
                // 3. start activity
                startActivityForResult(intent, 0);
            }
        });
        btnSendImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                sendImage(v);
            }
        });
        btnTakePhoto.setOnClickListener(new buttonTakePhotoClick());

        //get the location when app loads
        locationManager = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);
        Location location = locationManager.getLastKnownLocation(provider);
        if (location != null) {
            latitude = (int) (location.getLatitude());
            longitude = (int) (location.getLongitude());
        }
        else {
            latitude = 0;
            longitude = 0;
        }


        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //initialize Parse
        Parse.initialize(getActivity(), "pxTOcONFXPsseHcKpV8QUYR7xIpfbOAUgWwATyYj", "Oda0Rzk4MzS70OPiWvba80DZov6XXWtlEiyoNgkL");
        ParseUser.enableAutomaticUser();
        ParseACL defaultACL = new ParseACL();

        //set read access to public for Parse
        defaultACL.setPublicReadAccess(true);
        ParseACL.setDefaultACL(defaultACL, true);
    }

    class buttonTakePhotoClick implements Button.OnClickListener
    {
        @Override
        public void onClick(View v) {

            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            if (cameraIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                // Create the File where the photo should go
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    // Error occurred while creating the File
                    Toast toast = Toast.makeText(getActivity().getApplicationContext(), "Failed to create photo", Toast.LENGTH_SHORT);
                    toast.show();
                }
                // Continue only if the File was successfully created
                if (photoFile != null) {
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                    startActivityForResult(cameraIntent, CAM_REQUEST);
                }
            }
        }
    }

    // Call to update the share intent
    private void setShareIntent(Intent shareIntent) {
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    public void sendImage(View view){
        Intent shareImageIntent = new Intent();
        shareImageIntent.setAction(Intent.ACTION_SEND);
        shareImageIntent.putExtra(Intent.EXTRA_STREAM, globalUri);
        shareImageIntent.setType("image/jpeg");
        setShareIntent(shareImageIntent);
        startActivity(Intent.createChooser(shareImageIntent, "Send Photo..."));
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    @Override
    public void onActivityResult(int reqCode, int resCode, Intent data) {
        if(resCode == Activity.RESULT_OK && data != null && reqCode != CAM_REQUEST){
            String realPath;
            // SDK < API11
            if (Build.VERSION.SDK_INT < 11)
                realPath = RealPathUtil.getRealPathFromURI_BelowAPI11(getActivity(), data.getData());

                // SDK >= 11 && SDK < 19
            else if (Build.VERSION.SDK_INT < 19)
                realPath = RealPathUtil.getRealPathFromURI_API11to18(getActivity(), data.getData());

                // SDK > 19 (Android 4.4)
            else
                realPath = RealPathUtil.getRealPathFromURI_API19(getActivity(), data.getData());

            //setTextViews(data.getData().getPath(),realPath);
            setTextViews(realPath);
        }
        else if (reqCode == CAM_REQUEST && resCode == Activity.RESULT_OK ) {
            //simplified code for displaying image after photo capture
            setTextViews(currPhotoPathAbsolute);
            //function to add photo to media provider
            addPhotoToGallery();

            //code to auto upload to Parse
            File imageFile = new File(currPhotoPathAbsolute);
            String imageFileName = imageFile.getName();
            Uri uriFromPath = Uri.fromFile(imageFile);
            Bitmap imageBitmap = null;
            try {
                imageBitmap = BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(uriFromPath));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            ByteArrayOutputStream uploadArrayStream = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, uploadArrayStream);
            byte[] imageUpload = uploadArrayStream.toByteArray();
            ParseFile parseUpload = new ParseFile(imageFileName, imageUpload);
            //auto upload to Parse
            parseUpload.saveInBackground(new SaveCallback() {
                @Override
                public void done(com.parse.ParseException e) {
                    if (e == null) {
                        Toast.makeText(getActivity(), "Image Shared to the Cloud",Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getActivity(), "Failed to Share to the Cloud",Toast.LENGTH_LONG).show();
                    }
                }
            });
            //get the location before storing into parse

            //create the structure in Parse to store the photo
            ParseObject photoUploads = new ParseObject("UploadedImages");
            photoUploads.put("ImageName", imageFileName);
            photoUploads.put("FileName", parseUpload);
            photoUploads.put("latitude", latitude);
            photoUploads.put("longitude", longitude);
            photoUploads.saveInBackground();
            //Toast.makeText(getActivity(), "Image Shared to the Cloud",Toast.LENGTH_LONG).show();
        }
    }


    //function to save image
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imgName = "JPEG_" + timeStamp + "_";
        File storagePath = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File photo = File.createTempFile(
                imgName,  /* prefix */
                ".jpg",         /* suffix */
                storagePath      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currPhotoPath = "file:" + photo.getAbsolutePath();
        currPhotoPathAbsolute = String.valueOf(photo.getAbsoluteFile());
        return photo;
    }

    private void addPhotoToGallery() {
        Intent mediaScannerIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currPhotoPathAbsolute);
        Uri photoUri = Uri.fromFile(f);
        mediaScannerIntent.setData(photoUri);
        getActivity().sendBroadcast(mediaScannerIntent);
    }

    //private void setTextViews(int sdk, String uriPath,String realPath){
    private void setTextViews(String realPath){
        Uri uriFromPath = Uri.fromFile(new File(realPath));
        globalUri = uriFromPath;

        // you have two ways to display selected image

        // ( 1 ) imageView.setImageURI(uriFromPath);
        // ( 2 ) imageView.setImageBitmap(bitmap);


        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(uriFromPath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        imageView.setImageBitmap(bitmap);


        //imageView.setImageURI(uriFromPath);


        ExifInterface exif = null;
        try {
            exif = new ExifInterface(realPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
        //rotate image if not in proper orientation
        if (orientation == 3 || orientation == 6 || orientation == 8){
            Matrix matrix = new Matrix();
            switch(orientation)
            {
                //under rotated by 90 degrees
                case 6: matrix.postRotate(90);
                    break;
                //upside down
                case 3: matrix.postRotate(180);
                    break;
                //over rotated by 90 degrees
                case 8: matrix.preRotate(90);
                    break;
            }
            Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            imageView.setImageBitmap(rotatedBitmap);
        }
        else
        {
            imageView.setImageBitmap(bitmap);
        }

    }

}
