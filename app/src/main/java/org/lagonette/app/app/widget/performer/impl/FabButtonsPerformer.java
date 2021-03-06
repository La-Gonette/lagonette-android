package org.lagonette.app.app.widget.performer.impl;

import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.view.View;

import org.lagonette.app.R;
import org.lagonette.app.app.widget.performer.base.ViewPerformer;
import org.zxcv.functions.main.BooleanSupplier;
import org.zxcv.functions.main.Consumer;
import org.zxcv.functions.main.Runnable;

public abstract class FabButtonsPerformer
		implements ViewPerformer {

	@NonNull
	public Consumer<Location> onPositionClick = Consumer::doNothing;

	@NonNull
	public Runnable askForFineLocationPermission = Runnable::doNothing;

	@NonNull
	public BooleanSupplier checkForFineLocationPermission = BooleanSupplier.get(false);

	@NonNull
	public Runnable onPositionLongClick = Runnable::doNothing;

	@Nullable
	private FloatingActionButton mPositionFab;

	@Override
	public void inject(@NonNull View view) {
		mPositionFab = view.findViewById(R.id.my_location_fab);
		if (mPositionFab != null) {
			updatePositionFabClickListener();
			mPositionFab.setOnLongClickListener(
					button -> {
						onPositionLongClick.run();
						return true;
					}
			);
		}
	}

	private void updatePositionFabClickListener() {
		if (mPositionFab != null) {
			if (checkForFineLocationPermission.get()) {
				mPositionFab.setOnClickListener(
						button -> onPositionClick.accept((Location) button.getTag())
				);
			}
			else {
				mPositionFab.setOnClickListener(
						button -> askForFineLocationPermission.run()
				);
			}
		}
	}

	public void updateLocation(@Nullable Location location) {
		if (mPositionFab != null) {
			mPositionFab.setTag(location);
		}
	}

	public void notifyFineLocationGranted() {
		updatePositionFabClickListener();
	}
}
