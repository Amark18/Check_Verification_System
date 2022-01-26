package com.akapps.check_verification_system.classes;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.nfc.NfcAdapter;
import android.os.Handler;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.RecyclerView;

import com.akapps.check_verification_system.activities.MainActivity;

public class Animation {

    // layout
    private final NestedScrollView scrollView;
    private final SearchView searchView;
    private final TextView closeSearch;
    private final ImageView nfcStatus;
    private final LinearLayout searchLayout;
    private final RecyclerView customerRecyclerview;
    private final TextView emptyRecyclerviewMessage;
    private final ImageView settings;

    // variables
    private final int longAnimationDuration = 1000;
    private final int mediumAnimationDuration = 500;
    private final int smallDelay = 100;
    private final NfcAdapter nfcAdapter;
    private int initialWidthOfTapToRefresh;

    public Animation(NestedScrollView scrollView, SearchView searchView, TextView closeSearch, ImageView nfcStatus,
                     LinearLayout searchLayout, RecyclerView customerRecyclerview,
                     TextView emptyRecyclerviewMessage, NfcAdapter nfcAdapter, ImageView settings) {
        this.scrollView = scrollView;
        this.searchView = searchView;
        this.closeSearch = closeSearch;
        this.nfcStatus = nfcStatus;
        this.searchLayout = searchLayout;
        this.customerRecyclerview = customerRecyclerview;
        this.emptyRecyclerviewMessage = emptyRecyclerviewMessage;
        this.nfcAdapter = nfcAdapter;
        this.settings = settings;
        new Handler().postDelayed(() -> showTopOfScreen(scrollView), smallDelay);
    }

    // slides up the search bar to be on the top and sets whatever on top to be invisible
    public void slideUp(View tapToRefresh, View date, View move, View other, boolean showKeyboard){
        new Handler().postDelayed(() -> {
            nfcStatus.animate().alpha(0.0f).setDuration(mediumAnimationDuration).withEndAction(() -> nfcStatus.setVisibility(View.INVISIBLE));
            settings.animate().alpha(0.0f).setDuration(mediumAnimationDuration).withEndAction(() -> settings.setVisibility(View.INVISIBLE));
            closeSearch.setVisibility(View.VISIBLE);
            emptyRecyclerviewMessage.animate().alpha(1.0f).setDuration(mediumAnimationDuration).withStartAction(() -> emptyRecyclerviewMessage.setVisibility(View.VISIBLE));
            move.animate().alpha(1.0f).setDuration(mediumAnimationDuration).withStartAction(() -> move.setVisibility(View.VISIBLE));
            other.animate().alpha(0.0f).setDuration(mediumAnimationDuration);
            ObjectAnimator dateAnimation = ObjectAnimator.ofFloat(date, "translationY",(date.getY()) + 30);
            ObjectAnimator closeAnimation = ObjectAnimator.ofFloat(closeSearch, "translationY",(date.getY()) + 30);
            ObjectAnimator searchAnimation = ObjectAnimator.ofFloat(searchLayout, "translationY",(-1 * (move.getY() - other.getY())) + 120);
            ObjectAnimator refreshAnimation = ObjectAnimator.ofFloat(tapToRefresh, "translationY",(-1 * (tapToRefresh.getY() + date.getY())) + 70);
            searchAnimation.setDuration(longAnimationDuration);
            refreshAnimation.setDuration(longAnimationDuration);
            dateAnimation.setDuration(longAnimationDuration);
            closeAnimation.setDuration(longAnimationDuration);
            dateAnimation.start();
            closeAnimation.start();
            searchAnimation.start();
            refreshAnimation.start();
            tapToRefresh.setVisibility(View.VISIBLE);
            initialWidthOfTapToRefresh = tapToRefresh.getLayoutParams().width;
            tapToRefresh.getLayoutParams().width = Helper.getWidthScreen((MainActivity)tapToRefresh.getContext()) / 2;
            // clear recyclerview data
            customerRecyclerview.setAdapter(null);
            // show keyboard after animation to ensure layout has been populated
            new Handler().postDelayed(() -> showKeyboard(showKeyboard), longAnimationDuration);
            new Handler().postDelayed(() -> showTopOfScreen(scrollView), longAnimationDuration + smallDelay);
        }, smallDelay);
    }

    // slides down the search bar to its default position and
    public void slideDown(View tapToRefresh, View date, View move, View other){
        new Handler().postDelayed(() -> {
            nfcStatus.animate().alpha(1.0f).setDuration(mediumAnimationDuration).withEndAction(() -> nfcStatus.setVisibility(View.VISIBLE));
            settings.animate().alpha(1.0f).setDuration(mediumAnimationDuration).withEndAction(() -> settings.setVisibility(View.VISIBLE));
            move.animate().alpha(0.0f).setDuration(mediumAnimationDuration).withEndAction(() -> move.setVisibility(View.INVISIBLE));
            other.animate().alpha(1.0f).setDuration(mediumAnimationDuration);
            ObjectAnimator dateAnimation = ObjectAnimator.ofFloat(date, "translationY",0f);
            ObjectAnimator closeAnimation = ObjectAnimator.ofFloat(closeSearch, "translationY", 0f);
            ObjectAnimator animation = ObjectAnimator.ofFloat(move, "translationY",0f);
            ObjectAnimator refreshAnimation = ObjectAnimator.ofFloat(tapToRefresh, "translationY",0f);
            animation.setDuration(mediumAnimationDuration);
            refreshAnimation.setDuration(mediumAnimationDuration);
            dateAnimation.setDuration(mediumAnimationDuration);
            closeAnimation.setDuration(mediumAnimationDuration);
            closeAnimation.start();
            dateAnimation.start();
            animation.start();
            refreshAnimation.start();
            tapToRefresh.getLayoutParams().width = initialWidthOfTapToRefresh;
            // clears recyclerview data
            customerRecyclerview.setAdapter(null);
            // clear search input
            searchView.setQuery("", false);
            new Handler().postDelayed(() -> showTopOfScreen(scrollView), mediumAnimationDuration + smallDelay);
            new Handler().postDelayed(() -> closeSearch.setVisibility(View.INVISIBLE), mediumAnimationDuration);
        }, smallDelay);
    }

    public void showTopOfScreen(NestedScrollView scrollView){
        // ensures that top of screen is currently viewed
        scrollView.smoothScrollTo(0, 0, 500);
    }

    public void showKeyboard(boolean showKeyboard){
        if(showKeyboard) {
            // focuses on search and opens keyboard
            searchView.setIconified(true);
            searchView.setIconified(false);
        }
    }
}
