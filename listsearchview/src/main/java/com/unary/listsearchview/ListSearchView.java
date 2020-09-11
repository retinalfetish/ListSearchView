/*
 * Copyright 2020 Christopher Zaborsky
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.unary.listsearchview;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.CollapsibleActionView;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.AnimatorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;

/**
 * A simple SearchView widget extended from AutoCompleteTextView and stylized as such. It can show a
 * drop down list of suggestions from a ListAdapter.
 *
 * <p><strong>XML attributes</strong></p>
 * <p>The following optional attributes from the android namespace are provided default values:</p>
 * <pre>
 *   app:actionViewAnimator="reference" // Animator to use when the action view is expanded
 *
 *   android:hint="string"              // Hint given for the search. Default is "Search\u2026"
 *   android:imeOptions="flags"         // The IME options set for the search field
 *   android:inputType="flags"          // Input type for the search text field
 *   android:minWidth="dimension"       // Minimum width for the view. Default is Integer.MAX_VALUE
 * </pre>
 * <p>See {@link R.styleable#ListSearchView ListSearchView Attributes}, {@link R.styleable#View View Attributes}</p>
 */
public class ListSearchView extends AppCompatAutoCompleteTextView implements
        CollapsibleActionView,
        TextWatcher,
        TextView.OnEditorActionListener,
        AdapterView.OnItemClickListener {

    private static final CharSequence SEARCH_HINT = "Search\u2026";
    private static final int IME_OPTIONS = EditorInfo.IME_ACTION_SEARCH;
    private static final int INPUT_TYPE = InputType.TYPE_CLASS_TEXT;
    private static final int MIN_WIDTH = Integer.MAX_VALUE;
    private static final int ANIMATOR_DURATION = android.R.integer.config_shortAnimTime;

    private Animator mActionViewAnimator;
    private boolean mActionViewAnimatorSet;
    private OnSearchChangeListener mOnSearchChangeListener;

    /**
     * Interface to notify the client of any changes to the search. This reflects both user updates
     * and client changes to the query text.
     */
    public interface OnSearchChangeListener {

        /**
         * Notification that the search query field has changed. Changes made to the search field
         * due to this event may cause an infinite loop.
         *
         * @param listSearchView ListSearchView object that initiated the change.
         * @param query          Current text in the field.
         */
        void onQueryTextChanged(@NonNull ListSearchView listSearchView, String query);

        /**
         * Notification that a search query has been submitted. Changes made to the search field due
         * to this event may cause an infinite loop.
         *
         * @param listSearchView ListSearchView object that initiated the change.
         * @param query          The submitted search query.
         */
        void onQueryTextSubmitted(@NonNull ListSearchView listSearchView, String query);

        /**
         * Notification that a list suggestion has been submitted. Changes made to the search field
         * due to this event may cause an infinite loop.
         *
         * @param listSearchView ListSearchView object that initiated the change.
         * @param position       Adapter list position.
         */
        void onSuggestionSubmitted(@NonNull ListSearchView listSearchView, int position);
    }

    /**
     * Simple constructor to use when creating the view from code.
     *
     * @param context Context given for the view. This determines the resources and theme.
     */
    public ListSearchView(@NonNull Context context) {
        super(context);
        init(context, null, 0);
    }

    /**
     * Constructor that is called when inflating the view from XML.
     *
     * @param context Context given for the view. This determines the resources and theme.
     * @param attrs   The attributes for the inflated XML tag.
     */
    public ListSearchView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    /**
     * Constructor called when inflating from XML and applying a style.
     *
     * @param context      Context given for the view. This determines the resources and theme.
     * @param attrs        The attributes for the inflated XML tag.
     * @param defStyleAttr Default style attributes to apply to this view.
     */
    public ListSearchView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    /**
     * Shared method to initialize the member variables from the XML and provide default values.
     * Input values are not checked for sanity.
     *
     * @param context      Context given for the view. This determines the resources and theme.
     * @param attrs        The attributes for the inflated XML tag.
     * @param defStyleAttr Default style attributes to apply to this view.
     */
    private void init(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ListSearchView, defStyleAttr, 0);
        int animatorRes;
        CharSequence hint;
        int imeOptions;
        int inputType;
        float minWidth;

        try {
            animatorRes = typedArray.getResourceId(R.styleable.ListSearchView_actionViewAnimator, 0);
            hint = typedArray.getText(R.styleable.ListSearchView_android_hint);
            imeOptions = typedArray.getInt(R.styleable.ListSearchView_android_imeOptions, IME_OPTIONS);
            inputType = typedArray.getInt(R.styleable.ListSearchView_android_inputType, INPUT_TYPE);
            minWidth = typedArray.getDimension(R.styleable.ListSearchView_android_minWidth, MIN_WIDTH);
        } finally {
            typedArray.recycle();
        }

        if (animatorRes != 0) {
            setActionViewAnimatorResource(animatorRes);
        }

        // Set the default values
        setHint(hint == null ? SEARCH_HINT : hint);
        setImeOptions(imeOptions);
        setInputType(inputType);
        setMinWidth(Math.round(minWidth));

        // Set the event listeners
        addTextChangedListener(this);
        setOnEditorActionListener(this);
        setOnItemClickListener(this);
    }

    @Override
    public void onActionViewExpanded() {
        requestFocus();

        // Action views are lazy inflated
        if (!mActionViewAnimatorSet) {
            mActionViewAnimator = ObjectAnimator
                    .ofFloat(this, View.TRANSLATION_Y, -((View) getParent()).getHeight(), 0)
                    .setDuration(getResources().getInteger(ANIMATOR_DURATION));

            mActionViewAnimatorSet = true;
        }

        if (mActionViewAnimator != null && !mActionViewAnimator.isStarted()) {
            mActionViewAnimator.start();
        }
    }

    @Override
    public void onActionViewCollapsed() {
        setText(null);

        if (mActionViewAnimator != null) {
            mActionViewAnimator.cancel();
        }
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);

        InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

        // Show or hide the keyboard
        if (focused) {
            inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT);
        } else {
            inputMethodManager.hideSoftInputFromWindow(getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        if (getText().length() < getThreshold() || isPerformingCompletion()) return;

        if (mOnSearchChangeListener != null) {
            mOnSearchChangeListener.onQueryTextChanged(this, getText().toString());
        }
    }

    @Override
    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        dismissDropDown();

        if (mOnSearchChangeListener != null) {
            mOnSearchChangeListener.onQueryTextSubmitted(this, getText().toString());
        }

        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (mOnSearchChangeListener != null) {
            mOnSearchChangeListener.onSuggestionSubmitted(this, i);
        }
    }

    /**
     * Get the action view animator. A default animator is assigned if one has not been provided by
     * the client. This can be set to null to remove it.
     *
     * @return Animator for the action view.
     */
    @Nullable
    public Animator getActionViewAnimator() {
        return mActionViewAnimator;
    }

    /**
     * Set the action view animator. A default animator is assigned if one has not been provided by
     * the client. This can be set to null to remove it.
     *
     * @param actionViewAnimator Animator for the action view.
     */
    public void setActionViewAnimator(@Nullable Animator actionViewAnimator) {
        mActionViewAnimator = actionViewAnimator;
        mActionViewAnimatorSet = true;
    }

    /**
     * Set the action view animator resource. A default animator is assigned if one has not been
     * provided by the client.
     *
     * @param animatorResId Resource for the action view.
     */
    public void setActionViewAnimatorResource(@AnimatorRes int animatorResId) {
        setActionViewAnimator(AnimatorInflater.loadAnimator(getContext(), animatorResId));
        mActionViewAnimator.setTarget(this);
    }

    /**
     * Get the search listener for this instance. The interface is used to notify the client of any
     * any changes made to the search.
     *
     * @return ListSearchView search listener.
     */
    @Nullable
    public OnSearchChangeListener getOnSearchChangeListener() {
        return mOnSearchChangeListener;
    }

    /**
     * Set the search listener for this instance. The interface is used to notify the client of any
     * changes made to the search.
     *
     * @param onSearchChangeListener ListSearchView search listener.
     */
    public void setOnSearchChangeListener(@Nullable OnSearchChangeListener onSearchChangeListener) {
        mOnSearchChangeListener = onSearchChangeListener;
    }
}