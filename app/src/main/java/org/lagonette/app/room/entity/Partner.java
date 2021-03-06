package org.lagonette.app.room.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;


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

	@ColumnInfo(name = "phone")
	public String phone;

	@ColumnInfo(name = "website")
	public String website;

	@ColumnInfo(name = "email")
	public String email;

	@ColumnInfo(name = "is_gonette_headquarter")
	public boolean isGonetteHeadquarter;

	@ColumnInfo(name = "main_category_id")
	public long mainCategoryId;

}