# ListSearchView
A simple SearchView widget extended from [AutoCompleteTextView](https://developer.android.com/reference/android/widget/AutoCompleteTextView) and stylized as such. It can show a drop down list of suggestions from a [ListAdapter](https://developer.android.com/reference/android/widget/ListAdapter).

## Screenshots
<img src="/art/screenshot-animation.gif" alt="Screenshot" height=600> <img src="/art/screenshot-styled.png" alt="Screenshot" height=600>

## Usage
The source code can be copied from the single class file and attrs.xml in to your project or included by adding [jitpack.io](https://jitpack.io/#com.unary/listsearchview) to the root build.gradle and `implementation 'com.unary:listsearchview:1.0.0'` as a module dependency.
```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```
This widget is typically used as a menu item action view but can be added as part of an activity layout. An example app is provided in the project repository to illustrate its use and the `OnSearchChangeListener` interface.
```
<?xml version="1.0" encoding="utf-8"?>
<menu xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto">

    <item
        android:id="@+id/action_search"
        android:icon="@drawable/ic_baseline_search_24"
        android:title="Search"
        app:actionViewClass="com.unary.listsearchview.ListSearchView"
        app:showAsAction="collapseActionView|ifRoom" />

</menu>
```
As part of a layout:
```
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">

    <com.unary.listsearchview.ListSearchView
        android:id="@+id/search_view"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_gravity="center" />

</FrameLayout>
```
The listener interface:
```
searchView.setOnSearchChangeListener(this);

@Override
public void onQueryTextChanged(ListSearchView listSearchView, String query) { ... }

@Override
public void onQueryTextSubmitted(ListSearchView listSearchView, String query) { ... }

@Override
public void onSuggestionSubmitted(ListSearchView listSearchView, int position) { ... }
```

## XML attributes
The following optional attributes from the android namespace are provided default values:
```
app:actionViewAnimator="reference" // Animator to use when the action view is expanded

android:hint="string"              // Hint given for the search. Default is "Search\u2026"
android:imeOptions="flags"         // The IME options set for the search field
android:inputType="flags"          // Input type for the search text field
android:minWidth="dimension"       // Minimum width for the view. Default is Integer.MAX_VALUE
```
