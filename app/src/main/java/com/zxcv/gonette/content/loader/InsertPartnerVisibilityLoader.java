package com.zxcv.gonette.content.loader;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.OperationApplicationException;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.util.Log;

import com.zxcv.gonette.BuildConfig;
import com.zxcv.gonette.R;
import com.zxcv.gonette.content.contract.GonetteContract;
import com.zxcv.gonette.content.loader.base.BundleLoader;

import java.util.ArrayList;

public class InsertPartnerVisibilityLoader extends BundleLoader {

    private static final String TAG = "InsertPartnerVisibility";

    private static final String ARG_PARTNER_ID = "arg:partner_id";

    private static final String ARG_IS_VISIBLE = "arg:is_visible";

    private boolean mIsVisible;

    private long mPartnerId;

    @NonNull
    public static Bundle getArgs(long partnerId, boolean isVisible) {
        Bundle args = new Bundle(2);
        args.putLong(ARG_PARTNER_ID, partnerId);
        args.putBoolean(ARG_IS_VISIBLE, isVisible);
        return args;
    }

    public InsertPartnerVisibilityLoader(Context context, Bundle args) {
        super(context, args);
    }

    @Override
    protected void readArguments(@NonNull Bundle args) {
        mPartnerId = args.getLong(ARG_PARTNER_ID, GonetteContract.NO_ID);
        mIsVisible = args.getBoolean(ARG_IS_VISIBLE, false);

        if (BuildConfig.DEBUG) {
            if (mPartnerId == GonetteContract.NO_ID) {
                throw new IllegalArgumentException("A partner id must specified.");
            }
        }
    }

    @Override
    public void deliverResult(Bundle data) {
        if (isReset()) {
            return;
        }
        if (isStarted()) {
            Log.d(TAG, "deliverResult() " + mBundle.toString() + " " + toString());
            super.deliverResult(data);
        }
    }

    @Override
    public Bundle loadInBackground() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        operations.add(
                ContentProviderOperation.newInsert(GonetteContract.PartnerMetadata.CONTENT_URI)
                        .withValue(GonetteContract.PartnerMetadata.PARTNER_ID, mPartnerId)
                        .withValue(GonetteContract.PartnerMetadata.IS_VISIBLE, mIsVisible)
                        .build()
        );
        try {
            mContext.getContentResolver().applyBatch(
                    GonetteContract.AUTHORITY,
                    operations
            );
        } catch (RemoteException e) {
            e.printStackTrace(); // TODO
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
        mBundle.putInt(ARG_STATUS, STATUS_OK);
        return mBundle;
    }
}