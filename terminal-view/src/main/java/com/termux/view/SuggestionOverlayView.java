package com.termux.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import java.util.List;

public class SuggestionOverlayView extends PopupWindow {

    private ListView listView;
    private SuggestionAdapter adapter;
    private OnSuggestionSelectedListener listener;

    public interface OnSuggestionSelectedListener {
        void onSuggestionSelected(String suggestion);
    }

    public SuggestionOverlayView(Context context) {
        super(context);
        listView = new ListView(context);
        listView.setBackgroundColor(Color.parseColor("#073642"));
        setContentView(listView);
        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setFocusable(false); // Let TerminalView handle key events
        setOutsideTouchable(true);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (listener != null) {
                String suggestion = adapter.getItem(position);
                listener.onSuggestionSelected(suggestion);
            }
        });
    }

    public void setSuggestions(Context context, List<String> suggestions) {
        adapter = new SuggestionAdapter(context, suggestions);
        listView.setAdapter(adapter);
    }

    public void setOnSuggestionSelectedListener(OnSuggestionSelectedListener listener) {
        this.listener = listener;
    }

    public void moveSelection(int offset) {
        if (adapter != null && adapter.getCount() > 0) {
            int current = listView.getSelectedItemPosition();
            if (current == ListView.INVALID_POSITION) {
                current = 0;
            } else {
                current = (current + offset + adapter.getCount()) % adapter.getCount();
            }
            listView.setSelection(current);
            adapter.setSelectedIndex(current);
        }
    }

    public void selectCurrent() {
        int current = listView.getSelectedItemPosition();
        if (current == ListView.INVALID_POSITION && adapter != null && adapter.getCount() > 0) {
            current = 0;
        }
        if (current != ListView.INVALID_POSITION && listener != null) {
            listener.onSuggestionSelected(adapter.getItem(current));
        }
    }

    public void showAtCursor(View parent, int cursorX, int cursorY) {
        showAtLocation(parent, Gravity.NO_GRAVITY, cursorX, cursorY);
    }

    private class SuggestionAdapter extends ArrayAdapter<String> {
        private int selectedIndex = -1;

        public SuggestionAdapter(Context context, List<String> suggestions) {
            super(context, android.R.layout.simple_list_item_1, suggestions);
        }

        public void setSelectedIndex(int index) {
            this.selectedIndex = index;
            notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView view = (TextView) super.getView(position, convertView, parent);
            view.setTextColor(Color.parseColor("#eee8d5"));
            // Try to load OpenDyslexicMono from assets
            try {
                Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "font.ttf");
                view.setTypeface(tf);
            } catch (Exception e) {
                view.setTypeface(Typeface.MONOSPACE);
            }

            if (position == selectedIndex) {
                view.setBackgroundColor(Color.parseColor("#cb4b16")); // Amber focus highlight
            } else {
                view.setBackgroundColor(Color.parseColor("#073642"));
            }
            return view;
        }
    }
}
