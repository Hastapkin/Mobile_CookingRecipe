package com.example.cookingrecipe.ui.courses;

import android.view.View;
import android.widget.AdapterView;

public class SimpleItemSelectedListener implements AdapterView.OnItemSelectedListener {

    public interface OnSelect {
        void onSelect(int position);
    }

    private final OnSelect onSelect;

    public SimpleItemSelectedListener(OnSelect onSelect) {
        this.onSelect = onSelect;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (onSelect != null) {
            onSelect.onSelect(position);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }
}
