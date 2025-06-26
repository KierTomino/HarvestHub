package com.example.harvesthub;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class ActivityLogDetailsDialogFragment extends DialogFragment {
    private static final String ARG_TYPE = "type";
    private static final String ARG_DATE = "date";
    private static final String ARG_DESCRIPTION = "description";
    private static final String ARG_NOTES = "notes";
    private static final String ARG_PHOTO = "photo";

    public static ActivityLogDetailsDialogFragment newInstance(ActivityLog log) {
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, log.getType());
        args.putString(ARG_DATE, log.getDate());
        args.putString(ARG_DESCRIPTION, log.getDescription());
        args.putString(ARG_NOTES, log.getNotes());
        args.putString(ARG_PHOTO, log.getPhotoBase64());
        ActivityLogDetailsDialogFragment fragment = new ActivityLogDetailsDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_activity_log_details, container, false);
        Bundle args = getArguments();
        if (args != null) {
            ((TextView) view.findViewById(R.id.detailsType)).setText(args.getString(ARG_TYPE, ""));
            ((TextView) view.findViewById(R.id.detailsDate)).setText(args.getString(ARG_DATE, ""));
            ((TextView) view.findViewById(R.id.detailsDescription)).setText(args.getString(ARG_DESCRIPTION, ""));
            ((TextView) view.findViewById(R.id.detailsNotes)).setText(args.getString(ARG_NOTES, ""));
            String base64Photo = args.getString(ARG_PHOTO, null);
            ImageView imageView = view.findViewById(R.id.detailsPhoto);
            if (base64Photo != null && !base64Photo.isEmpty()) {
                try {
                    byte[] imageBytes = Base64.decode(base64Photo, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                    imageView.setImageBitmap(bitmap);
                    imageView.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    imageView.setVisibility(View.GONE);
                }
            } else {
                imageView.setVisibility(View.GONE);
            }
        }
        view.findViewById(R.id.btnCloseDetails).setOnClickListener(v -> dismiss());
        return view;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setTitle("Activity Log Details");
        return dialog;
    }
} 