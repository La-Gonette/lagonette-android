package org.lagonette.app.app.widget.performer.portrait;

import android.content.res.Resources;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;

import org.lagonette.app.app.widget.performer.impl.BottomSheetPerformer;

public class PortraitBottomSheetPerformer
        extends BottomSheetPerformer {

    public PortraitBottomSheetPerformer(
            @NonNull Resources resources,
            @IdRes int bottomSheetRes) {
        super(resources, bottomSheetRes);
    }

    @Override
    public void openBottomSheet() {
        if (mBehavior != null) {
            mBottomSheet.post(() -> mBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED));
        }
    }

}
