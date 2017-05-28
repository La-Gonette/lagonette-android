package org.lagonette.android.content.reader;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.lagonette.android.content.contract.GonetteContract;
import org.lagonette.android.database.columns.FilterColumns;

public class FilterReader extends CursorReader {

    public static FilterReader create(@Nullable Cursor cursor) {
        return cursor != null
                ? new FilterReader(cursor)
                : null;
    }

    @NonNull
    public final CategoryReader categoryReader;

    @NonNull
    public final PartnerReader partnerReader;

    public FilterReader(@NonNull Cursor cursor) {
        super(cursor);
        categoryReader = new CategoryReader(cursor);
        partnerReader = new PartnerReader(cursor);
    }

    @FilterColumns.RowType
    public int getRowType() {
        //noinspection WrongConstant
        return mCursor.getInt(
                mCursor.getColumnIndex(
                        GonetteContract.Filter.ROW_TYPE
                )
        );
    }

}