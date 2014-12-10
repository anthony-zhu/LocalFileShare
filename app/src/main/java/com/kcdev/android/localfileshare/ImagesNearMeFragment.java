package com.kcdev.android.localfileshare;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.kcdev.android.getimage.R;

import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ImagesNearMeFragment extends Fragment {
    private ProgressDialog progressDialog;
    ImageView imageView;
    private LocationManager locationManager;
    private String provider;
    private int latitude;
    private int longitude;

    public ImagesNearMeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Get the location manager
        locationManager = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
        // Define the criteria how to select the locatioin provider -> use
        // default
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);
        Location location = locationManager.getLastKnownLocation(provider);
        latitude = (int) (location.getLatitude());
        longitude = (int) (location.getLongitude());
        Toast.makeText(getActivity(), "This is the latitude - "+latitude, Toast.LENGTH_SHORT).show();
        Toast.makeText(getActivity(), "This is the longitude - "+longitude, Toast.LENGTH_SHORT).show();

        final ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_images_near_me, container, false);
        final ParseObject image = new ParseObject("UploadedImages");
        ParseQuery<ParseObject> query = ParseQuery.getQuery("UploadedImages");
        query.whereGreaterThan("latitude", String.valueOf(latitude - 1));
        query.whereLessThan("latitude", String.valueOf(latitude + 1));
        query.whereGreaterThan("longitude", String.valueOf(longitude - 1));
        query.whereLessThan("longitude", String.valueOf(longitude + 1));
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> object, ParseException e) {
                if (e == null) {
                    String imageName = object.get(0).getString("ImageName");
                    ParseFile image = (ParseFile) object.get(0).get("FileName");
                    image.getDataInBackground(new GetDataCallback() {
                        @Override
                        public void done(byte[] bytes, ParseException e) {
                            if (e == null)
                            {
                                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                imageView =(ImageView) rootView.findViewById(R.id.image);
                                imageView.setImageBitmap(bmp);
                            }
                            else
                            {
                                Log.d("test", "There was a problem downloading the data.");
                            }
                        }
                    });

                    //Toast.makeText(getActivity(), "Image Name:" + imageName, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getActivity(), "No Image Downloaded", Toast.LENGTH_LONG).show();// something went wrong
                }
            }
        });

        // Inflate the layout for this fragment
        return rootView;
    }




}
