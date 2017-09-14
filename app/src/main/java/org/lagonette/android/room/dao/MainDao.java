package org.lagonette.android.room.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;
import android.database.Cursor;

import org.lagonette.android.room.statement.FilterStatement;
import org.lagonette.android.room.statement.MapPartnerStatement;
import org.lagonette.android.room.statement.PartnerDetailStatement;

@Dao
public interface MainDao {

    // TODO Return Partner ?
    @Query(PartnerDetailStatement.sql)
    Cursor getPartnerDetail(long id);

    // TODO Return reader (TypeAdapter) ?
    @Query(MapPartnerStatement.sql)
    Cursor getMapPartner(String search);

    @Query(FilterStatement.sql)
    Cursor getFilters(String search);
}