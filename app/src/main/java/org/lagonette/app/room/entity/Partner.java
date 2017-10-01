package org.lagonette.app.room.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import org.lagonette.app.room.embedded.Address;


@Entity(
        tableName = "partner"
)
public class Partner {

    @PrimaryKey
    @ColumnInfo(name = "id")
    public long id;

    @ColumnInfo(name = "client_code")
    public String clientCode;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "logo")
    public String logo;

    @ColumnInfo(name = "short_description")
    public String shortDescription;

    @ColumnInfo(name = "description")
    public String description;

    @ColumnInfo(name = "latitude")
    public double latitude;

    @ColumnInfo(name = "longitude")
    public double longitude;

    @ColumnInfo(name = "phone")
    public String phone;

    @ColumnInfo(name = "website")
    public String website;

    @ColumnInfo(name = "email")
    public String email;

    @ColumnInfo(name = "opening_hours")
    public String openingHours;

    @ColumnInfo(name = "is_exchange_office")
    public boolean isExchangeOffice;

    @ColumnInfo(name = "is_localizable")
    public boolean isLocalizable;

    @ColumnInfo(name = "main_category_id")
    public long mainCategoryId;

    @Embedded
    public Address address;

}