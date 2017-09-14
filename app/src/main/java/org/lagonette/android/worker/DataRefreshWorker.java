package org.lagonette.android.worker;

import android.content.Context;
import android.content.OperationApplicationException;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;

import org.lagonette.android.api.response.CategoriesResponse;
import org.lagonette.android.api.response.PartnersResponse;
import org.lagonette.android.api.service.LaGonetteService;
import org.lagonette.android.locator.DB;
import org.lagonette.android.room.database.LaGonetteDatabase;
import org.lagonette.android.room.entity.Category;
import org.lagonette.android.room.entity.CategoryMetadata;
import org.lagonette.android.room.entity.Partner;
import org.lagonette.android.room.entity.PartnerMetadata;
import org.lagonette.android.room.entity.PartnerSideCategory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class DataRefreshWorker
        extends BackgroundWorker {

    private static final String TAG = "DataRefreshWorker";

    public DataRefreshWorker(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void doWork(@NonNull WorkerResponse response) {

        LaGonetteDatabase database = DB.get();
        try {
            LaGonetteService service = LaGonetteService.retrofit.create(LaGonetteService.class);
            List<Category> categories = new ArrayList<>();
            List<CategoryMetadata> categoryMetadataList = new ArrayList<>();
            List<Partner> partners = new ArrayList<>();
            List<PartnerMetadata> partnerMetadataList = new ArrayList<>();
            List<PartnerSideCategory> partnerSideCategories = new ArrayList<>();

            database.beginTransaction();

            if (getCategories(getContext(), service, categories, categoryMetadataList)) {
                database.categoryDao().deleteCategories();
                database.categoryDao().insertCategories(categories);
                database.categoryDao().insertCategoriesMetadatas(categoryMetadataList);
            }

            if (getPartners(getContext(), service, partners, partnerMetadataList, partnerSideCategories)) {
                database.partnerDao().deletePartners();
                database.partnerDao().insertPartners(partners);
                database.partnerDao().insertPartnersMetadatas(partnerMetadataList);
                database.partnerDao().deletePartnerSideCategories();
                database.partnerDao().insertPartnersSideCategories(partnerSideCategories);
            }

            database.setTransactionSuccessful();
            response.setIsSuccessful(true);

            // TODO Maybe find a way to avoid request data again if they not change

        } catch (IOException | RemoteException | OperationApplicationException e) {
            Log.e(TAG, "loadInBackground: ", e);
            FirebaseCrash.report(e);
            response.setIsSuccessful(false);
            // TODO set message
        } finally {
            database.endTransaction();
        }
    }

    private boolean getCategories(
            @NonNull Context context,
            @NonNull LaGonetteService service,
            @NonNull List<Category> categories,
            @NonNull List<CategoryMetadata> categoryMetadataList)
            throws IOException, RemoteException, OperationApplicationException {

        Call<CategoriesResponse> categoryCall = service.getCategories();
        Response<CategoriesResponse> response = categoryCall.execute();

        if (response.isSuccessful()) {
            CategoriesResponse result = response.body();
            return result.prepareInsert(context, categories, categoryMetadataList);
        } else {
            FirebaseCrash.logcat(Log.ERROR, TAG, response.code() + ": " + response.message());
            throw new IllegalStateException("response is not successful!"); // TODO Use custom exception
        }
    }

    private boolean getPartners(
            @NonNull Context context,
            @NonNull LaGonetteService service,
            @NonNull List<Partner> partners,
            @NonNull List<PartnerMetadata> partnerMetadataList,
            @NonNull List<PartnerSideCategory> partnerSideCategories)
            throws IOException, RemoteException, OperationApplicationException {

        Call<PartnersResponse> partnersCall = service.getPartners();
        Response<PartnersResponse> response = partnersCall.execute();

        if (response.isSuccessful()) {
            PartnersResponse result = response.body();
            return result.prepareInsert(context, partners, partnerMetadataList, partnerSideCategories);
        } else {
            FirebaseCrash.logcat(Log.ERROR, TAG, response.code() + ": " + response.message());
            throw new IllegalStateException("response is not successful!"); // TODO Use custom exception
        }
    }
}