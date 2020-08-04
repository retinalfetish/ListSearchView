package com.unary.test;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.unary.listsearchview.ListSearchView;

public class MainActivity extends AppCompatActivity {

    private TextView mTextView;
    private ListSearchView mSearchView;

    private ArrayAdapter<String> mAdapter;
    private String[] mItems = {"apples", "apricots", "avocados", "bananas", "blueberries", "cherries",
            "grapes", "grapefruits", "lemons", "limes", "oranges", "peaches", "pears", "pineapples", "strawberries"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = findViewById(R.id.text_view);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options, menu);

        MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        mSearchView = (ListSearchView) searchMenuItem.getActionView();

        mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, mItems);
        mSearchView.setAdapter(mAdapter);

        // Remove default underline
        //mSearchView.setBackgroundColor(0);

        // Remove default animator
        //mSearchView.setActionViewAnimator(null);

        // Change the view appearance
        //mSearchView.setBackground(getResources().getDrawable(R.drawable.edittext_rounded));
        //mSearchView.setTextColor(getResources().getColor(android.R.color.primary_text_light));
        //mSearchView.setHintTextColor(getResources().getColor(android.R.color.darker_gray));

        // Modify dropdown background
        //mSearchView.setDropDownBackgroundDrawable(getResources().getDrawable(R.drawable.edittext_rounded));

        // Change hint and threshold
        //mSearchView.setHint("Fruit\u2026");
        //mSearchView.setThreshold(1);

        mSearchView.setOnSearchChangeListener(new ListSearchView.OnSearchChangeListener() {
            @Override
            public void onQueryTextChanged(@NonNull ListSearchView listSearchView, String query) {
                mTextView.setText("Text changed to \"" + query + "\".");
            }

            @Override
            public void onQueryTextSubmitted(@NonNull ListSearchView listSearchView, String query) {
                mTextView.setText("Query \"" + query + "\" was submitted.");
            }

            @Override
            public void onSuggestionSubmitted(@NonNull ListSearchView listSearchView, int position) {
                mTextView.setText("Item \"" + mAdapter.getItem(position).toString() + "\" was submitted.");
            }
        });

        return true;
    }
}