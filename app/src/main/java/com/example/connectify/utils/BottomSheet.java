package com.example.connectify.utils;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.connectify.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.card.MaterialCardView;

public class BottomSheet extends BottomSheetDialogFragment {

    private BottomSheetListener mListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet, container, false);
        MaterialCardView mRemoveProfile = view.findViewById(R.id.remove_profile);
        MaterialCardView mGallery = view.findViewById(R.id.gallery);
        MaterialCardView mCamera = view.findViewById(R.id.camera);
        mRemoveProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onCardListener("removeProfile");
                dismiss();
            }
        });
        mGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onCardListener("gallery");
                dismiss();
            }
        });
        mCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onCardListener("camera");
                dismiss();
            }
        });


        return view;
    }

    public interface BottomSheetListener {
        public void onCardListener(String cardName);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mListener = (BottomSheetListener) context;
        } catch (ClassCastException e) {
            Toast.makeText(context, "BottomSheetListener must be implement", Toast.LENGTH_SHORT).show();
        }

    }
}
