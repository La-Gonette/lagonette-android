package org.lagonette.app.locator;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.lagonette.app.room.database.LaGonetteDatabase;

public final class DB {

	@Nullable
	private static LaGonetteDatabase DB;

	private DB() {
	}

	@NonNull
	public static LaGonetteDatabase get() {
		if (DB == null) throw new IllegalStateException("Database was not set!");
		return DB;
	}

	public static void set(@NonNull LaGonetteDatabase db) {
		DB = db;
	}
}
