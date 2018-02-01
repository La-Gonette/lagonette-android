package org.lagonette.app.app.widget.presenter;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.view.View;

import org.lagonette.app.R;
import org.lagonette.app.app.activity.PresenterActivity;
import org.lagonette.app.app.viewmodel.MainLiveEventBusViewModel;
import org.lagonette.app.app.viewmodel.StateMapActivityViewModel;
import org.lagonette.app.app.viewmodel.UiStateStore;
import org.lagonette.app.app.widget.coordinator.MainCoordinator;
import org.lagonette.app.app.widget.coordinator.state.MainAction;
import org.lagonette.app.app.widget.coordinator.state.MainState;
import org.lagonette.app.app.widget.performer.impl.BottomSheetPerformer;
import org.lagonette.app.app.widget.performer.impl.FabButtonsPerformer;
import org.lagonette.app.app.widget.performer.impl.FiltersFragmentPerformer;
import org.lagonette.app.app.widget.performer.impl.LocationDetailFragmentPerformer;
import org.lagonette.app.app.widget.performer.impl.MapFragmentPerformer;
import org.lagonette.app.app.widget.performer.impl.SearchBarPerformer;
import org.lagonette.app.room.statement.Statement;
import org.lagonette.app.tools.arch.LongObserver;

import static org.lagonette.app.app.viewmodel.MainLiveEventBusViewModel.Action.MOVE_TO_CLUSTER;
import static org.lagonette.app.app.viewmodel.MainLiveEventBusViewModel.Action.OPEN_LOCATION_ID;
import static org.lagonette.app.app.viewmodel.MainLiveEventBusViewModel.Action.OPEN_LOCATION_ITEM;
import static org.lagonette.app.app.viewmodel.MainLiveEventBusViewModel.Action.SHOW_FULL_MAP;

public abstract class MainPresenter<
        FBP extends FabButtonsPerformer,
        MFP extends MapFragmentPerformer,
        SBP extends SearchBarPerformer>
        implements PresenterActivity.Lifecycle {

    private static final String TAG = "MainPresenter";

    protected UiStateStore mUiStateStore;

    protected StateMapActivityViewModel mStateViewModel;

    protected MainLiveEventBusViewModel mEventBus;

    protected MutableLiveData<String> mSearch;

    protected LiveData<Integer> mWorkStatus;

    protected MainCoordinator mCoordinator;

    protected BottomSheetPerformer mBottomSheetPerformer;

    protected FBP mFabButtonsPerformer;

    protected MFP mMapFragmentPerformer;

    protected SBP mSearchBarPerformer;

    protected FiltersFragmentPerformer mFiltersFragmentPerformer;

    protected LocationDetailFragmentPerformer mLocationDetailFragmentPerformer;

    @Override
    @CallSuper
    public void startConstruct(@NonNull PresenterActivity activity) {

        mStateViewModel = ViewModelProviders
                .of(activity)
                .get(StateMapActivityViewModel.class);

        mEventBus = ViewModelProviders
                .of(activity)
                .get(MainLiveEventBusViewModel.class);

        mUiStateStore = ViewModelProviders
                .of(activity)
                .get(UiStateStore.class);

        mSearch = mStateViewModel.getSearch();
        mWorkStatus = mStateViewModel.getWorkStatus();

        mCoordinator = createCoordinator();
        mMapFragmentPerformer = createMapFragmentPerformer(activity);
        mFabButtonsPerformer = createFabButtonPerformer(activity);
        mSearchBarPerformer = createSearchBarPerformer(activity);
        mBottomSheetPerformer = createBottomSheetPerformer(activity);
        mFiltersFragmentPerformer = new FiltersFragmentPerformer(activity, R.id.fragment_filters);
        mLocationDetailFragmentPerformer = new LocationDetailFragmentPerformer(activity, R.id.fragment_location_detail);

        // === Coordinator > Performer === //
        mCoordinator.openBottomSheet = mBottomSheetPerformer::openBottomSheet;
        mCoordinator.closeBottomSheet = mBottomSheetPerformer::closeBottomSheet;

        mCoordinator.moveMapToCluster = mMapFragmentPerformer::moveToCluster;
        mCoordinator.moveMapToLocation = mMapFragmentPerformer::moveToLocation;
        mCoordinator.moveMapToMyLocation = mMapFragmentPerformer::moveToMyLocation;
        mCoordinator.stopMapMoving = mMapFragmentPerformer::stopMoving;
        mCoordinator.moveMapToFootprint = mMapFragmentPerformer::moveToFootprint;
        mCoordinator.openLocation = mMapFragmentPerformer::openLocation;

        mCoordinator.loadMap = mMapFragmentPerformer::loadFragment;
        mCoordinator.restoreMap = mMapFragmentPerformer::restoreFragment;

        mCoordinator.loadFilters = mFiltersFragmentPerformer::loadFragment;
        mCoordinator.unloadFilters = mFiltersFragmentPerformer::unloadFragment;
        mCoordinator.restoreFilters = mFiltersFragmentPerformer::restoreFragment;

        mCoordinator.loadLocationDetail = mLocationDetailFragmentPerformer::loadFragment;
        mCoordinator.unloadLocationDetail = mLocationDetailFragmentPerformer::unloadFragment;
        mCoordinator.restoreLocationDetail = mLocationDetailFragmentPerformer::restoreFragment;

        // === Coordinator > Store === //
        mCoordinator.finishAction = mUiStateStore::notifyActionIsFinished;
    }

    @Override
    @LayoutRes
    public int getContentView() {
        return R.layout.activity_main;
    }

    @Override
    public void inject(@NonNull View view) {
        mBottomSheetPerformer.inject(view);
        mSearchBarPerformer.inject(view);
        mFabButtonsPerformer.inject(view);
    }

    @Override
    @CallSuper
    public void init(@NonNull PresenterActivity activity) {
        MainState currentState = retrieveCurrentState();
        MainState initState = mCoordinator.init(currentState);
        mUiStateStore.setState(initState);
    }

    @Override
    @CallSuper
    public void restore(@NonNull PresenterActivity activity, @NonNull Bundle savedInstanceState) {
        MainState currentState = retrieveCurrentState();
        MainState restoredState = mCoordinator.restore(currentState);
        mUiStateStore.setState(restoredState);
    }

    @Override
    @CallSuper
    public void endConstruct(@NonNull PresenterActivity activity) {

        // === Performer > Store === //
        mEventBus.subscribe(
                OPEN_LOCATION_ITEM,
                activity,
                locationItem -> mUiStateStore.startAction(MainAction.moveToAndOpenLocation(locationItem))
        );
        mEventBus.subscribe(
                MOVE_TO_CLUSTER,
                activity,
                cluster -> mUiStateStore.startAction(MainAction.moveToCluster(cluster))
        );
        mEventBus.subscribe(
                SHOW_FULL_MAP,
                activity,
                aVoid -> mUiStateStore.startAction(MainAction.showFullMap())
        );
        mEventBus.subscribe(
                OPEN_LOCATION_ID,
                activity,
                LongObserver.unbox(
                        Statement.NO_ID,
                        locationId -> mUiStateStore.startAction(MainAction.moveToAndOpenLocation(locationId))
                )
        );

        mFabButtonsPerformer.onPositionClick = () -> mUiStateStore.startAction(MainAction.moveToMyLocation());
        mFabButtonsPerformer.onPositionLongClick = () -> mUiStateStore.startAction(MainAction.moveToFootprint());

        mLocationDetailFragmentPerformer.onFragmentLoaded(mUiStateStore::notifyLocationIdLoaded);
        mLocationDetailFragmentPerformer.onFragmentUnloaded(mUiStateStore::notifyLocationDetailUnload);
        mFiltersFragmentPerformer.onFragmentLoaded(() -> mUiStateStore.notifyFiltersLoading(true));
        mFiltersFragmentPerformer.onFragmentUnloaded(() -> mUiStateStore.notifyFiltersLoading(false));
        mBottomSheetPerformer.onStateChanged = mUiStateStore::notifyBottomSheetStateChanged;
        mMapFragmentPerformer.onMapMovementChanged = mUiStateStore::notifyMapMovement;


        // === Store > Coordinator === //
        mUiStateStore.getState()
                .observe(
                        activity,
                        mCoordinator::process
                );

        // Setup loggers
        mCoordinator.setupLoggers();


        // ------------------------------- //
        //                                 //
        //    User > ViewModels > View     //
        //                                 //
        // ------------------------------- //

        // User > ViewModels
        mSearchBarPerformer.onSearch = mSearch::setValue;

        // ViewModels > View
        mWorkStatus.observe(activity, mSearchBarPerformer::setWorkStatus);

    }

    public boolean onBackPressed(@NonNull PresenterActivity activity) {

        MainState state = mUiStateStore.getState().getValue();
        if (state == null || state.bottomSheetState == BottomSheetBehavior.STATE_HIDDEN) {
            return false;
        }

        mUiStateStore.startAction(MainAction.back());
        return true;
    }

    @NonNull
    private MainState retrieveCurrentState() {
        return new MainState(
                mUiStateStore.getState().getValue() != null
                        ? mUiStateStore.getState().getValue().action
                        : null,
                mMapFragmentPerformer.getMapMovement(),
                mBottomSheetPerformer.getState(),
                mFiltersFragmentPerformer.isLoaded(),
                mLocationDetailFragmentPerformer.isLoaded(),
                mLocationDetailFragmentPerformer.getLoadedId()
        );
    }

    @NonNull
    protected abstract MainCoordinator createCoordinator();

    @NonNull
    protected abstract BottomSheetPerformer createBottomSheetPerformer(@NonNull PresenterActivity activity);

    @NonNull
    protected abstract SBP createSearchBarPerformer(@NonNull PresenterActivity activity);

    @NonNull
    protected abstract FBP createFabButtonPerformer(@NonNull PresenterActivity activity);

    @NonNull
    protected abstract MFP createMapFragmentPerformer(@NonNull PresenterActivity activity);

}
