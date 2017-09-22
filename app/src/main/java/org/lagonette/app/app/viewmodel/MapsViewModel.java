package org.lagonette.app.app.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;

import org.lagonette.app.app.arch.EventShipper;
import org.lagonette.app.locator.Repo;
import org.lagonette.app.repo.Resource;
import org.lagonette.app.room.entity.statement.PartnerItem;

import java.util.List;

public class MapsViewModel extends ViewModel {

    private final LiveData<Resource<List<PartnerItem>>> mMapPartnersResource;

    private final MutableLiveData<String> mSearch;

    public MapsViewModel() {
        mSearch = new MutableLiveData<>();
        mMapPartnersResource = Repo.get().getMapPartners(mSearch);
    }

    @NonNull
    public LiveData<Resource<List<PartnerItem>>> getMapPartners() {
        return mMapPartnersResource;
    }

    @NonNull
    public EventShipper.Sender<String> getSearch() {
        return search -> mSearch.setValue(search);
    }
}
