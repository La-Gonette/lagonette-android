package org.lagonette.app.app.widget.viewholder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.lagonette.app.R;
import org.lagonette.app.util.LocationUtils;

public class LocationViewHolder
		extends RecyclerView.ViewHolder {

	@NonNull
	public final TextView nameTextView;

	@NonNull
	public final TextView addressTextView;

	@NonNull
	public final ImageView exchangeOfficeIndicatorImage;

	@NonNull
	public final ImageButton visibilityButton;

	public long locationId;

	public boolean isVisible;

	public boolean isCategoryVisible;

	public boolean isMainPartner;

	public boolean isExchangeOffice;

	public boolean isGonetteHeadquarter;

	public LocationViewHolder(
			@NonNull ViewGroup parent,
			boolean isOrphan) {
		super(
				LayoutInflater
						.from(parent.getContext())
						.inflate(
								isOrphan
										? R.layout.row_location_orphan
										: R.layout.row_location,
								parent,
								false
						)
		);
		nameTextView = itemView.findViewById(R.id.location_name);
		addressTextView = itemView.findViewById(R.id.location_address);
		exchangeOfficeIndicatorImage = itemView.findViewById(R.id.location_exchange_office_indicator);
		visibilityButton = itemView.findViewById(R.id.location_visibility);
	}

	public boolean displayAsExchangeOffice() {
		return LocationUtils.displayAsExchangeOffice(isExchangeOffice, isGonetteHeadquarter);
	}
}