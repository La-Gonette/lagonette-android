package org.lagonette.app.room.entity.statement;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Embedded;

import org.lagonette.app.room.embedded.Address;
import org.lagonette.app.util.LocationUtils;

public class LocationDetail {

	@ColumnInfo(name = "id")
	public long id;

	@ColumnInfo(name = "name")
	public String name;

	@ColumnInfo(name = "description")
	public String description;

	@ColumnInfo(name = "is_gonette_headquarter")
	public boolean isGonetteHeadquarter;

	@ColumnInfo(name = "latitude")
	public double latitude;

	@ColumnInfo(name = "longitude")
	public double longitude;

	@ColumnInfo(name = "client_code")
	public String clientCode;

	@ColumnInfo(name = "logo")
	public String logo;

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

	@ColumnInfo(name = "short_description")
	public String shortDescription;

	@Embedded
	public Address address;

	@ColumnInfo(name = "main_category_id")
	public long mainCategoryId;

	@ColumnInfo(name = "main_category_icon")
	public String mainCategoryIcon;

	@ColumnInfo(name = "main_category_label")
	public String mainCategoryLabel;

	public boolean displayAsExchangeOffice() {
		return LocationUtils.displayAsExchangeOffice(isExchangeOffice, isGonetteHeadquarter);
	}

}
