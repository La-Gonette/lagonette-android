package com.zxcv.gonette.app.fragment;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.data.geojson.GeoJsonFeature;
import com.google.maps.android.data.geojson.GeoJsonLayer;
import com.google.maps.android.data.geojson.GeoJsonLineStringStyle;
import com.zxcv.gonette.R;
import com.zxcv.gonette.app.widget.maps.PartnerItem;
import com.zxcv.gonette.app.widget.maps.PartnerRenderer;
import com.zxcv.gonette.content.contract.GonetteContract;
import com.zxcv.gonette.content.loader.PartnerCursorLoaderHelper;
import com.zxcv.gonette.content.reader.PartnerReader;
import com.zxcv.gonette.util.SharedPreferencesUtil;
import com.zxcv.gonette.util.SnackbarUtil;
import com.zxcv.gonette.util.UiUtil;

import org.json.JSONException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MapsFragment
        extends Fragment
        implements OnMapReadyCallback,
        LoaderManager.LoaderCallbacks<Cursor>,
        ClusterManager.OnClusterClickListener<PartnerItem>,
        ClusterManager.OnClusterItemClickListener<PartnerItem>,
        GoogleMap.OnMapClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public interface Callback {

        void hideMyLocationButton();

        void showMyLocationButton();

        void showPartner(long partnerId, boolean zoom);

        void showFullMap();

        void onMapReady();

    }

    private Location mLastLocation;

    private GoogleApiClient mGoogleApiClient;

    public static final String TAG = "MapsFragment";

    public static final int ANIMATION_LENGTH_LONG = 600;

    public static final int ANIMATION_LENGTH_SHORT = 300;

    public static final int ZOOM_LEVEL_STREET = 15;

    public static final int CLUSTER_CLICK_ZOOM_IN = 1;

    public static final int PERMISSIONS_REQUEST_LOCATION = 666;

    private static final String STATE_ASK_FOR_MY_LOCATION_PERMISSION = "state:ask_for_my_location_permission";

    private static final String STATE_SELECTED_MARKER_POSITION = "state:selected_marker_position";

    private GoogleMap mMap;

    private ClusterManager<PartnerItem> mClusterManager;

    private boolean mLocationPermissionGranted = false;

    private boolean mAskFormMyPositionPermission = true;

    private int mStatusBarHeight;

    private Callback mCallback;

    private Map<Long, PartnerItem> mPartnerItems;

    private Marker mSelectedMarker;

    private boolean mConfChanged = false;

    private double mStartLatitude;

    private double mStartLongitude;

    private float mStartZoom;

    private LatLng mSelectedMarkerPosition = null;

    public static MapsFragment newInstance() {
        return new MapsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mConfChanged = true;
            mAskFormMyPositionPermission = savedInstanceState.getBoolean(
                    STATE_ASK_FOR_MY_LOCATION_PERMISSION
            );
            mSelectedMarkerPosition = savedInstanceState.getParcelable(
                    STATE_SELECTED_MARKER_POSITION
            );
        } else {
            mConfChanged = false;
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
            mStartLatitude = sharedPref.getFloat(SharedPreferencesUtil.PREFERENCE_START_LATITUDE, SharedPreferencesUtil.DEFAULT_VALUE_START_LATITUDE);
            mStartLongitude = sharedPref.getFloat(SharedPreferencesUtil.PREFERENCE_START_LONGITUDE, SharedPreferencesUtil.DEFAULT_VALUE_START_LONGITUDE);
            mStartZoom = sharedPref.getFloat(SharedPreferencesUtil.PREFERENCE_START_ZOOM, SharedPreferencesUtil.DEFAULT_VALUE_START_ZOOM);
        }

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                    .addConnectionCallbacks(MapsFragment.this)
                    .addOnConnectionFailedListener(MapsFragment.this)
                    .addApi(LocationServices.API)
                    .build();
        }

        mStatusBarHeight = UiUtil.getStatusBarHeight(getResources());

        mPartnerItems = new HashMap<>();
    }

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        try {
            mCallback = (Callback) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(mCallback.toString() + " must implement " + Callback.class);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(STATE_ASK_FOR_MY_LOCATION_PERMISSION, mAskFormMyPositionPermission);
        if (mSelectedMarker != null) {
            outState.putParcelable(STATE_SELECTED_MARKER_POSITION, mSelectedMarker.getPosition());
        }
    }

    @Override
    public void onPause() {
        if (mMap != null) {
            CameraPosition cameraPosition = mMap.getCameraPosition();
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
            sharedPref.edit()
                    .putFloat(SharedPreferencesUtil.PREFERENCE_START_LATITUDE, (float) cameraPosition.target.latitude)
                    .putFloat(SharedPreferencesUtil.PREFERENCE_START_LONGITUDE, (float) cameraPosition.target.longitude)
                    .putFloat(SharedPreferencesUtil.PREFERENCE_START_ZOOM, cameraPosition.zoom)
                    .apply();
        }
        super.onPause();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mCallback.onMapReady();
        setupMap();
        setupFootprint();
        queryPartners();
    }

    private void setupMap() {
        mMap.setPadding(0, mStatusBarHeight, 0, 0);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);

        updateLocationUI();

        if (!mConfChanged) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(
                            mStartLatitude,
                            mStartLongitude
                    ),
                    mStartZoom
            ));
        } else if (mSelectedMarkerPosition != null) {
            addSelectedMarker(mSelectedMarkerPosition);
            mSelectedMarkerPosition = null;
        }

        mClusterManager = new ClusterManager<>(getContext(), mMap);
        mClusterManager.setRenderer(
                new PartnerRenderer(
                        getContext(),
                        LayoutInflater.from(getContext()),
                        mMap,
                        mClusterManager
                )
        );
        mMap.setOnMapClickListener(MapsFragment.this);
        mMap.setOnCameraIdleListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);
        mClusterManager.setOnClusterClickListener(MapsFragment.this);
        mClusterManager.setOnClusterItemClickListener(MapsFragment.this);
    }

    private void queryPartners() {
        LoaderManager loaderManager = getLoaderManager();
        loaderManager.initLoader(R.id.loader_query_map_partners, null, MapsFragment.this);
    }

    private void queryPartners(@NonNull String search) {
        LoaderManager loaderManager = getLoaderManager();
        loaderManager.restartLoader(
                R.id.loader_query_map_partners,
                PartnerCursorLoaderHelper.getArgs(search),
                MapsFragment.this
        );
    }

    private void setupFootprint() {
        try {
            GeoJsonLayer footprintLayer = new GeoJsonLayer(mMap, R.raw.footprint, getContext());
            for (GeoJsonFeature feature : footprintLayer.getFeatures()) {
                GeoJsonLineStringStyle lineStringStyle = new GeoJsonLineStringStyle();
                lineStringStyle.setColor(ContextCompat.getColor(getContext(), R.color.footprint));
                feature.setLineStringStyle(lineStringStyle);
            }
            footprintLayer.addLayerToMap();
        } catch (IOException | JSONException e) {
            Log.e(TAG, "onMapReady: " + e.getMessage(), e);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case R.id.loader_query_map_partners:
                String search = PartnerCursorLoaderHelper.getSearch(args);
                return new CursorLoader(
                        getContext(),
                        GonetteContract.Partner.METADATA_CONTENT_URI,
                        new String[]{
                                GonetteContract.Partner.ID,
                                GonetteContract.Partner.NAME,
                                GonetteContract.Partner.DESCRIPTION,
                                GonetteContract.Partner.LATITUDE,
                                GonetteContract.Partner.LONGITUDE
                        },
                        GonetteContract.PartnerMetadata.IS_VISIBLE + " = 1 AND " + GonetteContract.Partner.NAME + " LIKE ?",

                        new String[]{
                                "%" + search + "%"
                        },
                        null
                );
            default:
                throw new IllegalArgumentException("Unknown loader id: " + id);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        int id = loader.getId();
        switch (id) {
            case R.id.loader_query_map_partners:
                onQueryPartnerLoadFinished(cursor);
                break;
            default:
                throw new IllegalArgumentException("Unknown loader id: " + id);
        }
    }

    private void onQueryPartnerLoadFinished(Cursor cursor) {
        if (cursor != null) {
            mPartnerItems.clear();
            mClusterManager.clearItems();
            PartnerReader partnerReader = new PartnerReader(cursor);
            if (partnerReader.moveToFirst()) {
                do {
                    PartnerItem item = new PartnerItem(
                            partnerReader.getId(),
                            partnerReader.getLatitude(),
                            partnerReader.getLongitude()
                    );
                    mPartnerItems.put(item.getId(), item);
                    mClusterManager.addItem(item);
                } while (partnerReader.moveToNext());
            }
            mClusterManager.cluster();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        int id = loader.getId();
        switch (id) {
            case R.id.loader_query_map_partners:
                // Do nothing.
                break;
            default:
                throw new IllegalArgumentException("Unknown loader id: " + id);
        }
    }

    @Override
    public boolean onClusterClick(Cluster<PartnerItem> cluster) {
        mMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                        cluster.getPosition(),
                        mMap.getCameraPosition().zoom + CLUSTER_CLICK_ZOOM_IN
                ),
                ANIMATION_LENGTH_LONG,
                null
        );
        showFullMap();
        return true;
    }

    @Override
    public boolean onClusterItemClick(PartnerItem partnerItem) {
        mCallback.showPartner(partnerItem.getId(), false);
        return true;
    }

    @Override
    public void onMapClick(LatLng latLng) {
        showFullMap();
    }

    private void showFullMap() {
        removeSelectedMarker();
        mCallback.showFullMap();
    }

    private void addSelectedMarker(@NonNull LatLng position) {
        mSelectedMarker = mMap.addMarker(
                new MarkerOptions()
                        .position(position)
                        .zIndex(1f)
        );
    }

    private void removeSelectedMarker() {
        if (mSelectedMarker != null) {
            mSelectedMarker.remove();
            mSelectedMarker = null;
        }
    }

    public void processParallaxTranslation(float translationY) {
        int parallaxPadding = -(int) translationY;
        int topPadding = mStatusBarHeight + parallaxPadding;
        mMap.setPadding(0, topPadding, 0, parallaxPadding);
    }


    public void moveOnMyLocation() {
        if (mLocationPermissionGranted) {
            //noinspection MissingPermission
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLastLocation != null) {
                mMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                                new LatLng(
                                        mLastLocation.getLatitude(),
                                        mLastLocation.getLongitude()
                                ),
                                ZOOM_LEVEL_STREET
                        ),
                        ANIMATION_LENGTH_LONG,
                        null
                );
            } else {
                Log.e(TAG, "moveOnMyLocation: Last location is NULL");
            }
        }
    }

    public void moveOnFootprint() {
        mMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                        new LatLng(
                                SharedPreferencesUtil.DEFAULT_VALUE_START_LATITUDE,
                                SharedPreferencesUtil.DEFAULT_VALUE_START_LONGITUDE
                        ),
                        SharedPreferencesUtil.DEFAULT_VALUE_START_ZOOM
                ),
                ANIMATION_LENGTH_SHORT,
                null
        );
    }

    public void showPartner(long id, boolean zoom, GoogleMap.CancelableCallback callback) {
        removeSelectedMarker();
        PartnerItem partnerItem = mPartnerItems.get(id);
        if (partnerItem != null) {
            LatLng latLng = new LatLng(
                    partnerItem.getPosition().latitude,
                    partnerItem.getPosition().longitude
            );
            addSelectedMarker(latLng);
            if (zoom) {
                mMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(latLng, ZOOM_LEVEL_STREET),
                        ANIMATION_LENGTH_LONG,
                        callback
                );
            } else {
                mMap.animateCamera(
                        CameraUpdateFactory.newLatLng(latLng),
                        ANIMATION_LENGTH_SHORT,
                        callback
                );
            }
        }
    }

    public void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    public void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // Called when location service is connected and my position available.
        // Do nothing here.
    }

    @Override
    public void onConnectionSuspended(int i) {
        // Called when location service is suspended and my position is not available anymore.
        // Do nothing here.
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // Called when the connection to the location service fail.
        Log.e(TAG, "onConnectionFailed: " + connectionResult.getErrorMessage());
    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }

        if (checkLocationPermission()) {
            mMap.setMyLocationEnabled(true);
            mCallback.showMyLocationButton();
        } else {
            mMap.setMyLocationEnabled(false);
            mCallback.hideMyLocationButton();
        }
    }

    private boolean checkLocationPermission() {
        if (!mLocationPermissionGranted) {
            if (ContextCompat.checkSelfPermission(
                    getContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
            )
                    == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
            } else if (mAskFormMyPositionPermission) {
                mAskFormMyPositionPermission = false;
                requestPermissions(
                        new String[]{
                                Manifest.permission.ACCESS_FINE_LOCATION
                        },
                        PERMISSIONS_REQUEST_LOCATION
                );
            }
        }

        return mLocationPermissionGranted;
    }

    private void onLocationPermissionResult(@NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        // If request is cancelled, the result arrays are empty.
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String permissions[],
            @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_LOCATION:
                onLocationPermissionResult(grantResults);
                updateLocationUI();
                break;
            default:
                throw new IllegalArgumentException("Unknown request code: " + requestCode);
        }
    }

    public void startDirection(long partnerId) {
        PartnerItem partnerItem = mPartnerItems.get(partnerId);
        if (partnerItem != null) {
            Intent intent = new Intent(
                    android.content.Intent.ACTION_VIEW,
                    Uri.parse("google.navigation:q=" + partnerItem.getPosition().latitude + "," + partnerItem.getPosition().longitude)
            );
            PackageManager packageManager = getActivity().getPackageManager();
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent);
            } else {
                Snackbar
                        .make(
                                SnackbarUtil.getViewGroup(getActivity()).getChildAt(0),
                                R.string.error_no_direction_app_found,
                                Snackbar.LENGTH_LONG
                        )
                        .show();
            }
        }
    }

    public void filterPartner(@NonNull String search) {
        queryPartners(search);
    }

}
