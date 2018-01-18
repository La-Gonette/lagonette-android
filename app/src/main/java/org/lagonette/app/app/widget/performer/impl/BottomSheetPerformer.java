package org.lagonette.app.app.widget.performer.impl;

import android.content.res.Resources;
import android.support.annotation.IdRes;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.view.View;

import org.lagonette.app.app.widget.performer.base.ViewPerformer;
import org.lagonette.app.util.UiUtils;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.SOURCE;

public abstract class BottomSheetPerformer
        extends BottomSheetBehavior.BottomSheetCallback
        implements ViewPerformer {

    @Retention(SOURCE)
    @IntDef({
            BottomSheetBehavior.STATE_DRAGGING,
            BottomSheetBehavior.STATE_SETTLING,
            BottomSheetBehavior.STATE_EXPANDED,
            BottomSheetBehavior.STATE_COLLAPSED,
            BottomSheetBehavior.STATE_HIDDEN
    })
    public @interface State {

    }

    public interface OnStateChangedCommand {

        void notifyStateChanged(@State int newState);
    }

    public interface OnSlideChangedCommand {

        void updateTopPadding(int topPadding);

    }

    public static class Padding {

        public int bottomSheetTop, searchBarBottom, statusBarHeight;

        private int mTopPadding;

        public boolean updateTop() {
            int limit = Math.max(statusBarHeight, searchBarBottom);
            if (bottomSheetTop <= limit) {
                mTopPadding = limit - bottomSheetTop;
                return true;
            }
            else if (mTopPadding != 0) {
                mTopPadding = 0;
                return true;
            }
            else {
                return false;
            }
        }

        public int getTop() {
            return mTopPadding;
        }
    }

    @Nullable
    protected OnSlideChangedCommand mOnSlideChangedCommand;

    @Nullable
    private OnStateChangedCommand mOnStateChangedCommand;

    @Nullable
    protected BottomSheetBehavior<View> mBehavior;

    @Nullable
    protected View mBottomSheet;

    @IdRes
    private int mBottomSheetRes;

    @NonNull
    protected Padding mPadding;

    public BottomSheetPerformer(
            @NonNull Resources resources,
            @IdRes int bottomSheetRes) {
        mBottomSheetRes = bottomSheetRes;
        mPadding = new Padding();
        mPadding.statusBarHeight = UiUtils.getStatusBarHeight(resources);
        mPadding.searchBarBottom = 0;
        mPadding.bottomSheetTop = 0;
    }

    @Override
    public void inject(@NonNull View view) {
        mBottomSheet = view.findViewById(mBottomSheetRes);
        mBehavior = BottomSheetBehavior.from(mBottomSheet);
        mBehavior.setBottomSheetCallback(BottomSheetPerformer.this);
    }

    public void closeBottomSheet() {
        if (mBehavior != null) {
            mBottomSheet.post(() -> mBehavior.setState(BottomSheetBehavior.STATE_HIDDEN));
        }
    }

    public abstract void openBottomSheet();

    @Override
    public void onStateChanged(@NonNull View bottomSheet, @State int newState) {
        if (mOnStateChangedCommand != null) {
            mOnStateChangedCommand.notifyStateChanged(newState);
        }
    }

    @Override
    public void onSlide(@NonNull View bottomSheet, float slideOffset) {
        if (mBottomSheet != null) {
            mPadding.bottomSheetTop = mBottomSheet.getTop();
            updateBottomSheetTopPadding();
        }
    }

    public void notifySearchBarBottomChanged(int searchBarBottom) {
        mPadding.searchBarBottom = searchBarBottom;
        updateBottomSheetTopPadding();
    }

    private void updateBottomSheetTopPadding() {
        if (mPadding.updateTop()) {
            if (mOnSlideChangedCommand != null) {
                mOnSlideChangedCommand.updateTopPadding(mPadding.getTop());
            }
        }
    }

    public void onStateChanged(@Nullable OnStateChangedCommand onStateChangedCommand) {
        mOnStateChangedCommand = onStateChangedCommand;
    }

    public void onSlideChanged(@Nullable OnSlideChangedCommand command) {
        mOnSlideChangedCommand = command;
    }

    @State
    public int getState() {
        return mBehavior.getState();
    }

}
