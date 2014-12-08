package com.kcdev.android.localfileshare;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kcdev.android.getimage.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ImagesNearMeFragment extends Fragment {

    public ImagesNearMeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_images_near_me, container, false);
    }


}
