package org.lagonette.app.api.response;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;
import com.google.gson.annotations.SerializedName;

import org.lagonette.app.room.entity.PartnerMetadata;
import org.lagonette.app.util.PreferenceUtil;

import java.util.List;

public class PartnersResponse extends ApiResponse {

    private static final String TAG = "PartnersResponse";

    @SerializedName("partners")
    private List<Partner> mPartners;

    public boolean prepareInsert(
            @NonNull Context context,
            @NonNull List<org.lagonette.app.room.entity.Partner> partners,
            @NonNull List<org.lagonette.app.room.entity.PartnerMetadata> partnerMetadataList,
            @NonNull List<org.lagonette.app.room.entity.PartnerSideCategory> partnerSideCategories) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        String md5Sum = preferences.getString(
                PreferenceUtil.KEY_PARTNER_MD5_SUM,
                PreferenceUtil.DEFAULT_VALUE_PARTNER_MD5_SUM
        );


        if (!mMd5Sum.equals(md5Sum)) {

            for (Partner partner : mPartners) {
                if (partner != null) {
                    partner.prepareInsert(partners, partnerMetadataList, partnerSideCategories);
                }
            }
            addOfficePartner(partners, partnerMetadataList);

            // TODO Ensure data are saved before saving md5 sum, maybe put this in a runnable an execute it after closing transaction
            preferences.edit()
                    .putString(PreferenceUtil.KEY_PARTNER_MD5_SUM, mMd5Sum)
                    .apply();
            return true;
        }

        return false;
    }

    private void addOfficePartner(@NonNull List<org.lagonette.app.room.entity.Partner> partners, @NonNull List<PartnerMetadata> partnerMetadataList) {
//        partners.add(new OfficePartner());
//        partnerMetadataList.add(new OfficePartnerMetadata());
    }
}