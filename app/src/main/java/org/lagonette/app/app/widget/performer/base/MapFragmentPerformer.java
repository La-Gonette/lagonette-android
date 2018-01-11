package org.lagonette.app.app.widget.performer.base;

import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.google.maps.android.clustering.Cluster;

import org.lagonette.app.R;
import org.lagonette.app.app.fragment.MapsFragment;
import org.lagonette.app.app.widget.coordinator.state.MainState;
import org.lagonette.app.room.entity.statement.LocationItem;

public abstract class MapFragmentPerformer implements Performer {

    public interface OnMapMouvementChangedCommand {

        void notifyMapMovementChanged(@NonNull MainState.MapMovement mapMovement);
    }

    protected MapsFragment mFragment;

    @NonNull
    private final FragmentManager mFragmentManager;

    @Nullable
    private OnMapMouvementChangedCommand mMapMouvementChangedCommand;

    @NonNull
    protected MainState.MapMovement mMapMovement;

    @IdRes
    protected int mMapFragmentRes;

    public MapFragmentPerformer(@NonNull AppCompatActivity activity, @IdRes int mapFragmentRes) {
        mFragmentManager = activity.getSupportFragmentManager();
        mMapFragmentRes = mapFragmentRes;
        mMapMovement = MainState.MapMovement.IDLE;
    }

    public void loadFragment() {
        mFragment = MapsFragment.newInstance();
        mFragmentManager.beginTransaction()
                .add(R.id.content, mFragment, MapsFragment.TAG)
                .commit();
    }

    public void restoreFragment() {
        mFragment = (MapsFragment) mFragmentManager.findFragmentByTag(MapsFragment.TAG);
    }

    @Nullable
    public LocationItem retrieveLocationItem(long locationId) {
        if (mFragment != null) {
            return mFragment.retrieveLocationItem(locationId);
        }
        return null;
    }

    public MainState.MapMovement getMapMovement() {
        return mMapMovement;
    }

    public void notifyMapMovement(@NonNull MainState.MapMovement mapMovement) {
        mMapMovement = mapMovement;
        if (mMapMouvementChangedCommand != null) {
            mMapMouvementChangedCommand.notifyMapMovementChanged(mMapMovement);
        }
    }

    public void moveToMyLocation() {
        if (mFragment != null) {
            mFragment.moveToMyLocation();
        }
    }

    public boolean moveToFootprint() {
        if (mFragment != null) {
            mFragment.moveToFootprint();
            return true;
        }
        return false;
    }

    public void onMapMouvementChanged(@Nullable OnMapMouvementChangedCommand command) {
        mMapMouvementChangedCommand = command;
    }

    // TODO Improve fragment communication may be with LiveEvent
    public void moveToCluster(@NonNull Cluster<LocationItem> cluster) {
        if (mFragment != null) {
            mFragment.moveToCluster(cluster);
        }
    }

    public void moveToLocation(@NonNull LocationItem item) {
        if (mFragment != null) {
            mFragment.moveToLocation(item);
        }
    }

    public void stopMoving() {
        if (mFragment != null) {
            mFragment.stopMoving();
        }
    }
}
