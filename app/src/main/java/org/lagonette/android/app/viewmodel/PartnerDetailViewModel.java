package org.lagonette.android.app.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.support.annotation.NonNull;

import org.lagonette.android.app.viewmodel.base.DatabaseObserverViewModel;
import org.lagonette.android.room.reader.PartnerDetailReader;
import org.lagonette.android.room.statement.Statement;

public class PartnerDetailViewModel extends DatabaseObserverViewModel {

    private long mPartnerId = Statement.NO_ID;

    private MutableLiveData<PartnerDetailReader> mPartnerDetailReaderLiveData;

    public PartnerDetailViewModel(@NonNull Context context) {
        super(context);

        mPartnerDetailReaderLiveData = new MediatorLiveData<>();
    }

    @Override
    protected void onDatabaseInvalidated() {
        updatePartnerDetailReader();
    }

    private void updatePartnerDetailReader() {
        PartnerDetailReader reader = mPartnerId > Statement.NO_ID
                ? PartnerDetailReader.create(
                        mDatabase.mainDao().getPartnerDetail(mPartnerId)
                )
                : null;
        mPartnerDetailReaderLiveData.postValue(reader);
    }

    public void setPartnerId(long partnerId) {
        mPartnerId = partnerId;
        updatePartnerDetailReader();
    }

    public LiveData<PartnerDetailReader> getPartnerDetailReaderLiveData() {
        return mPartnerDetailReaderLiveData;
    }
}
