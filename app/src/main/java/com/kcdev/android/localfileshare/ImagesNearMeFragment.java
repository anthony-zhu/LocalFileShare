package com.kcdev.android.localFileShare;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

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

    public ImagesNearMeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Get the location manager
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
        Toast.makeText(getActivity(), "Your location : ( "+latitude+", "+longitude + ")", Toast.LENGTH_SHORT).show();


        final ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_images_near_me, container, false);
        //final ParseObject image = new ParseObject("UploadedImages");
        ParseQuery<ParseObject> query = ParseQuery.getQuery("UploadedImages");
        query.whereGreaterThanOrEqualTo("latitude", latitude - 1);
        query.whereLessThanOrEqualTo("latitude", latitude + 1);
        query.whereGreaterThanOrEqualTo("longitude", longitude - 1);
        query.whereLessThanOrEqualTo("longitude", longitude + 1);
        query.orderByDescending("createdAt");
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> object, ParseException e) {
                if (e == null) {
                    if (object.size() != 0) {
                        ParseFile image = (ParseFile) object.get(0).get("FileName");
                        image.getDataInBackground(new GetDataCallback() {
                            @Override
                            public void done(byte[] bytes, ParseException e) {
                                if (e == null) {
                                    Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                    imageView = (ImageView) rootView.findViewById(R.id.image);
                                    imageView.setImageBitmap(bmp);
                                } else {
                                    Log.d("test", "There was a problem downloading the data.");
                                }
                            }
                        });
                    }
                } else {
                    Toast.makeText(getActivity(), "No Image Downloaded", Toast.LENGTH_LONG).show();
                }
            }
        });

        return rootView;
    }




}
