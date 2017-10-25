package org.lagonette.app.app.widget.coordinator;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.util.Log;

import com.google.maps.android.clustering.Cluster;

import org.lagonette.app.app.fragment.MapsFragment;
import org.lagonette.app.app.widget.performer.BottomSheetFragmentManager;
import org.lagonette.app.app.widget.performer.BottomSheetPerformer;
import org.lagonette.app.app.widget.performer.MapFragmentPerformer;
import org.lagonette.app.room.entity.statement.PartnerItem;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.SOURCE;

public class MainCoordinator {

    private static final String TAG = "MainCoordinator";

    public interface BottomSheetCallback {

        void closeBottomSheet();

        void openBottomSheet();

    }

    public interface FragmentLoader {

        void loadFragment(@BottomSheetFragmentManager.FragmentType int type);

    }

    private static final int ACTION_IDLE = 0;

    private static final int ACTION_BACK = 1;

    private static final int ACTION_OPEN_FILTERS = 2;

    private static final int ACTION_MOVE_ON_MY_LOCATION = 3;

    private static final int ACTION_MOVE_ON_FOOTPRINT = 4;

    private static final int ACTION_MOVE_ON_CLUSTER = 5;

    @Retention(SOURCE)
    @IntDef({
            ACTION_IDLE,
            ACTION_BACK,
            ACTION_OPEN_FILTERS,
            ACTION_MOVE_ON_MY_LOCATION,
            ACTION_MOVE_ON_FOOTPRINT,
            ACTION_MOVE_ON_CLUSTER
    })
    private @interface Action {

    }

    @NonNull
    private final BottomSheetCallback mBottomSheetCallback;

    @NonNull
    private final FragmentLoader mFragmentLoader;

    @NonNull
    private final MapFragmentPerformer mMapFragmentPerformer;

    @Action
    private int mPendingAction;

    @Nullable
    private Cluster<PartnerItem> mPendingCluster;

    @BottomSheetPerformer.State
    private int mBottomSheetState;

    @BottomSheetFragmentManager.FragmentType
    private int mBottomSheetFragment;

    @MapsFragment.Movement
    private int mMapMovement;

    public MainCoordinator(
            @NonNull BottomSheetCallback bottomSheetCallback,
            @NonNull FragmentLoader fragmentLoader,
            @NonNull MapFragmentPerformer mapFragmentPerformer) {
        mBottomSheetCallback = bottomSheetCallback;
        mFragmentLoader = fragmentLoader;
        mMapFragmentPerformer = mapFragmentPerformer;
        mPendingAction = ACTION_IDLE;
        mBottomSheetState = BottomSheetBehavior.STATE_HIDDEN;
        mBottomSheetFragment = BottomSheetFragmentManager.FRAGMENT_NONE;
        mMapMovement = MapsFragment.STATE_MOVEMENT_IDLE;
    }

    public void openFilters() {
        Log.d(TAG, "Coordinator - Action: OPEN FILTERS");
        mPendingAction = ACTION_OPEN_FILTERS;
        computeFiltersOpening();
    }

    public boolean back() {
        Log.d(TAG, "Coordinator - Action: BACK");
        switch (mBottomSheetState) {

            case BottomSheetBehavior.STATE_COLLAPSED:
            case BottomSheetBehavior.STATE_DRAGGING:
            case BottomSheetBehavior.STATE_EXPANDED:
            case BottomSheetBehavior.STATE_SETTLING:
                mPendingAction = ACTION_BACK;
                computeBack();
                return true;

            default:
            case BottomSheetBehavior.STATE_HIDDEN:
                markPendingActionDone();
                return false;
        }
    }

    public void moveOnMyLocation() {
        Log.d(TAG, "Coordinator - Action: MOVE ON MY LOCATION");
        mPendingAction = ACTION_MOVE_ON_MY_LOCATION;
        computeMovementToMyLocation();
    }

    public void moveOnFootprint() {
        Log.d(TAG, "Coordinator - Action: MOVE ON FOOTPRINT");
        mPendingAction = ACTION_MOVE_ON_FOOTPRINT;
        computeMovementToFootprint();
    }

    public void moveOnCluster(@NonNull Cluster<PartnerItem> cluster) {
        Log.d(TAG, "Coordinator - Action: MOVE ON CLUSTER");
        mPendingAction = ACTION_MOVE_ON_CLUSTER;
        mPendingCluster = cluster;
        computeMovementToCluster();
    }

    public void notifyMapMovementChanged(@MapsFragment.Movement int newMovement) {
        Log.d(TAG, "Coordinator - Notification: Map movement " + newMovement);
        mMapMovement = newMovement;
        dispatchAction();
    }

    public void notifyBottomSheetStateChanged(@BottomSheetPerformer.State int newState) {
        Log.d(TAG, "Coordinator - Notification: Bottom sheet state " + newState);
        mBottomSheetState = newState;
        dispatchAction();
    }

    public void notifyBottomSheetFragmentChanged(@BottomSheetFragmentManager.FragmentType int newFragment) {
        Log.d(TAG, "Coordinator - Notification: Bottom sheet fragment " + newFragment);
        mBottomSheetFragment = newFragment;
        dispatchAction();
    }

    private void dispatchAction() {
        switch (mPendingAction) {

            case ACTION_BACK:
                computeBack();
                break;

            case ACTION_OPEN_FILTERS:
                computeFiltersOpening();
                break;

            case ACTION_MOVE_ON_MY_LOCATION:
                computeMovementToMyLocation();
                break;

            case ACTION_MOVE_ON_FOOTPRINT:
                computeMovementToFootprint();
                break;

            case ACTION_MOVE_ON_CLUSTER:
                computeMovementToCluster();
                break;

            default:
            case ACTION_IDLE:
                // Do nothing
                break;
        }
    }

    private void computeMovementToCluster() {
        switch (mBottomSheetState) {

            case BottomSheetBehavior.STATE_COLLAPSED:
            case BottomSheetBehavior.STATE_EXPANDED:
                mBottomSheetCallback.closeBottomSheet();
                break;

            case BottomSheetBehavior.STATE_DRAGGING:
            case BottomSheetBehavior.STATE_SETTLING:
                // Well, it's okay. Just wait.
                break;

            case BottomSheetBehavior.STATE_HIDDEN:
                switch (mMapMovement) {

                    case MapsFragment.STATE_MOVEMENT_MOVE:
                        // Well, it's okay. Just wait.
                        break;

                    case MapsFragment.STATE_MOVEMENT_IDLE:
                        if (mPendingCluster != null) {
                            mMapFragmentPerformer.moveOnCluster(mPendingCluster);
                            mPendingCluster = null;
                        }
                        else {
                            markPendingActionDone();
                        }
                        break;
                }
                break;
        }
    }

    private void computeMovementToFootprint() {
        switch (mBottomSheetState) {

            case BottomSheetBehavior.STATE_SETTLING:
                mMapFragmentPerformer.stopMoving();
                break;

            case BottomSheetBehavior.STATE_COLLAPSED:
            case BottomSheetBehavior.STATE_DRAGGING:
            case BottomSheetBehavior.STATE_EXPANDED:
                mMapFragmentPerformer.stopMoving();
                mBottomSheetCallback.closeBottomSheet();
                break;

            case BottomSheetBehavior.STATE_HIDDEN:
                switch (mMapMovement) {

                    case MapsFragment.STATE_MOVEMENT_IDLE:
                        mMapFragmentPerformer.moveOnFootprint();
                        break;

                    case MapsFragment.STATE_MOVEMENT_MOVE:
                        markPendingActionDone();
                        break;
                }
                break;
        }
    }

    private void computeMovementToMyLocation() {
        switch (mBottomSheetState) {

            case BottomSheetBehavior.STATE_SETTLING:
                mMapFragmentPerformer.stopMoving();
                break;

            case BottomSheetBehavior.STATE_COLLAPSED:
            case BottomSheetBehavior.STATE_DRAGGING:
            case BottomSheetBehavior.STATE_EXPANDED:
                mMapFragmentPerformer.stopMoving();
                mBottomSheetCallback.closeBottomSheet();
                break;

            case BottomSheetBehavior.STATE_HIDDEN:
                switch (mMapMovement) {

                    case MapsFragment.STATE_MOVEMENT_IDLE:
                        mMapFragmentPerformer.moveOnMyLocation();
                        break;

                    case MapsFragment.STATE_MOVEMENT_MOVE:
                        markPendingActionDone();
                        break;
                }
                break;
        }
    }

    private void computeBack() {
        switch (mBottomSheetState) {

            case BottomSheetBehavior.STATE_SETTLING:
                // Do nothing
                break;

            case BottomSheetBehavior.STATE_COLLAPSED:
            case BottomSheetBehavior.STATE_DRAGGING:
            case BottomSheetBehavior.STATE_EXPANDED:
                mBottomSheetCallback.closeBottomSheet();
                break;

            case BottomSheetBehavior.STATE_HIDDEN:
                markPendingActionDone();
                break;
        }
    }

    private void computeFiltersOpening() {
        switch (mBottomSheetState) {

            case BottomSheetBehavior.STATE_HIDDEN:
                switch (mBottomSheetFragment) {

                    case BottomSheetFragmentManager.FRAGMENT_NONE:
                        loadFiltersFragment();
                        break;

                    case BottomSheetFragmentManager.FRAGMENT_PARTNER:
                        loadFiltersFragment();
                        break;

                    case BottomSheetFragmentManager.FRAGMENT_FILTERS:
                        openBottomSheet();
                        break;
                }
                break;

            case BottomSheetBehavior.STATE_COLLAPSED:
                switch (mBottomSheetFragment) {

                    case BottomSheetFragmentManager.FRAGMENT_NONE:
                        wtf();
                        closeBottomSheet();
                        break;

                    case BottomSheetFragmentManager.FRAGMENT_PARTNER:
                        closeBottomSheet();
                        break;

                    case BottomSheetFragmentManager.FRAGMENT_FILTERS:
                        markPendingActionDone();
                        break;
                }
                break;

            case BottomSheetBehavior.STATE_EXPANDED:
                switch (mBottomSheetFragment) {

                    case BottomSheetFragmentManager.FRAGMENT_NONE:
                        wtf();
                        closeBottomSheet();
                        break;

                    case BottomSheetFragmentManager.FRAGMENT_PARTNER:
                        closeBottomSheet();
                        break;

                    case BottomSheetFragmentManager.FRAGMENT_FILTERS:
                        markPendingActionDone();
                        break;
                }
                break;

            case BottomSheetBehavior.STATE_DRAGGING:
                switch (mBottomSheetFragment) {

                    case BottomSheetFragmentManager.FRAGMENT_NONE:
                        wtf();
                        closeBottomSheet();
                        break;

                    case BottomSheetFragmentManager.FRAGMENT_FILTERS:
                        markPendingActionDone();
                        break;

                    case BottomSheetFragmentManager.FRAGMENT_PARTNER:
                        markPendingActionDone();
                        break;
                }
                break;

            case BottomSheetBehavior.STATE_SETTLING:
                switch (mBottomSheetFragment) {

                    case BottomSheetFragmentManager.FRAGMENT_NONE:
                        wtf();
                        closeBottomSheet();
                        break;

                    case BottomSheetFragmentManager.FRAGMENT_FILTERS:
                        markPendingActionDone();
                        break;

                    case BottomSheetFragmentManager.FRAGMENT_PARTNER:
                        closeBottomSheet();
                        break;
                }
                break;
        }
    }

    private void markPendingActionDone() {
        Log.d(TAG, "Coordinator - Action DONE.");
        mPendingAction = ACTION_IDLE;
        mPendingCluster = null;
    }

    private void closeBottomSheet() {
        mBottomSheetCallback.closeBottomSheet();
    }

    private void openBottomSheet() {
        mBottomSheetCallback.openBottomSheet();
    }

    private void loadFiltersFragment() {
        mFragmentLoader.loadFragment(BottomSheetFragmentManager.FRAGMENT_FILTERS);
    }

    private void loadPartnerFragment() {
        mFragmentLoader.loadFragment(BottomSheetFragmentManager.FRAGMENT_PARTNER);
    }

    private void unloadFragment() {
        mFragmentLoader.loadFragment(BottomSheetFragmentManager.FRAGMENT_NONE);
    }

    private void wtf() {
        //TODO log exception, just in case
    }
}
