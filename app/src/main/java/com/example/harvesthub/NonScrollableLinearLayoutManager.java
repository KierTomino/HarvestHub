package com.example.harvesthub;

import android.content.Context;
import androidx.recyclerview.widget.LinearLayoutManager;

public class NonScrollableLinearLayoutManager extends LinearLayoutManager {

    public NonScrollableLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    @Override
    public boolean canScrollHorizontally() {
        // this will disable horizontal scrolling
        return false;
    }
} 