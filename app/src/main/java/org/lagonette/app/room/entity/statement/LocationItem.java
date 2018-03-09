package org.lagonette.app.room.entity.statement;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Ignore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class LocationItem
        implements ClusterItem {

    @Ignore //TODO Try to use type converter
    @NonNull
    private final LatLng mPosition;

    @ColumnInfo(name = "id")
    private long mId;

    @ColumnInfo(name = "latitude")
    private double mLatitude;

    @ColumnInfo(name = "longitude")
    private double mLongitude;

    @ColumnInfo(name = "icon")
    @NonNull
    private final String mIconUrl;

    @ColumnInfo(name = "is_exchange_office")
    private boolean mIsExchangeOffice;

    @ColumnInfo(name = "is_gonette_headquarter")
    private boolean mIsGonetteHeadquarter;

    @ColumnInfo(name = "main_category_id") //TODO Use CategoryKey
    private long mCategoryId;

    public LocationItem(long id, long categoryId, boolean isGonetteHeadquarter, boolean isExchangeOffice, double latitude, double longitude, @NonNull String iconUrl) {
        mId = id;
        mIsGonetteHeadquarter = isGonetteHeadquarter;
        mIsExchangeOffice = isExchangeOffice;
        mPosition = new LatLng(latitude, longitude);
        mLatitude = latitude;
        mLongitude = longitude;
        mCategoryId = categoryId;
        mIconUrl = iconUrl;
    }

    public long getId() {
        return mId;
    }

    @NonNull
    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    @Nullable
    @Override
    public String getTitle() {
        return null;
    }

    @Nullable
    @Override
    public String getSnippet() {
        return null;
    }

    public boolean isExchangeOffice() {
        return mIsExchangeOffice;
    }

    @NonNull
    public String getIconUrl() {
        return mIconUrl;
    }

    @NonNull
    public long getCategoryId() {
        return mCategoryId;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public boolean isGonetteHeadquarter() {
        return mIsGonetteHeadquarter;
    }
}
