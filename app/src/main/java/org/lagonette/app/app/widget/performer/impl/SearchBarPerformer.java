package org.lagonette.app.app.widget.performer.impl;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.lagonette.app.R;
import org.lagonette.app.app.widget.performer.base.ViewPerformer;
import org.lagonette.app.background.worker.WorkerState;
import org.lagonette.app.util.UiUtils;
import org.zxcv.functions.main.Consumer;
import org.zxcv.functions.main.IntConsumer;

public abstract class SearchBarPerformer
		implements ViewPerformer {

	private static final String EMPTY_TEXT = "";

	@NonNull
	public IntConsumer onBottomChanged = IntConsumer::doNothing;

	@NonNull
	public Consumer<String> onSearch = Consumer::doNothing;

	@Nullable
	protected View mSearchBar;

	@Nullable
	private ProgressBar mProgressBar;

	@Nullable
	private TextView mSearchTextView;

	@Nullable
	private ImageButton mClearImageButton;

	@Override
	public void inject(@NonNull View view) {
		mSearchBar = view.findViewById(R.id.search_bar);
		mProgressBar = view.findViewById(R.id.progress_bar);
		mSearchTextView = view.findViewById(R.id.search_text);
		mClearImageButton = view.findViewById(R.id.search_clear);

		setupClearButton(mClearImageButton, mSearchTextView);
		setupSearchBarMarginTop(mSearchBar);
		setupSearchTextView(mSearchTextView);

		mSearchBar.addOnLayoutChangeListener(
				(searchBar, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> onBottomChanged(searchBar)
		);
	}

	protected void onBottomChanged(@NonNull View searchBar) {
		onBottomChanged.accept((int) (searchBar.getBottom() + searchBar.getTranslationY()));
	}

	public void setWorkStatus(@NonNull Integer status) {
		switch (status) {

			case WorkerState.LOADING:
				mProgressBar.setVisibility(View.VISIBLE);
				break;

			case WorkerState.ERROR:
			case WorkerState.SUCCESS:
				mProgressBar.setVisibility(View.GONE);
				break;
		}
	}

	private void setupClearButton(@NonNull View clearImageButton, @NonNull TextView searchText) {
		clearImageButton.setOnClickListener(view -> searchText.setText(EMPTY_TEXT));
	}

	private void setupSearchBarMarginTop(@NonNull View searchBar) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) searchBar.getLayoutParams();
			params.setMargins(
					params.leftMargin,
					params.topMargin + UiUtils.getStatusBarHeight(searchBar.getResources()),
					params.rightMargin,
					params.bottomMargin
			);
		}
	}

	private void setupSearchTextView(@NonNull TextView searchText) {
		// Add TextWatcher later to avoid callback called on configuration changed.
		searchText.post(
				() -> searchText.addTextChangedListener(
						new TextWatcher() {
							@Override
							public void beforeTextChanged(
									CharSequence s,
									int start,
									int count,
									int after) {

							}

							@Override
							public void onTextChanged(
									CharSequence s,
									int start,
									int before,
									int count) {

							}

							@Override
							public void afterTextChanged(Editable editable) {
								onSearch.accept(editable.toString());
							}
						}
				)
		);
	}

}