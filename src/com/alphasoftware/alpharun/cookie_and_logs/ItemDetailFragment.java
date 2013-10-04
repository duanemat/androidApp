package com.alphasoftware.alpharun.cookie_and_logs;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alphasoftware.alpharun.R;
import com.alphasoftware.alpharun.cookie_and_logs.ItemListFragment.LogItem;

public class ItemDetailFragment extends Fragment {
	/**
	 * The fragment argument representing the item ID that this fragment
	 * represents.
	 */
	public static final String ARG_ITEM_ID = "item_id";
	public static final String ITEM_TYPE = "item_type";

	/**
	 * The content this fragment is presenting.
	 */
	private Object mItem;

	private static DatabaseContent dbcontent;
	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public ItemDetailFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d("tracking", this.getClass().toString());
		super.onCreate(savedInstanceState);

		// Figure out what to show in the list
		String itemType = getArguments().getString(ITEM_TYPE);


		if (getArguments().containsKey(ARG_ITEM_ID)) {
			// Load the dummy content specified by the fragment
			// arguments. In a real-world scenario, use a Loader
			// to load content from a content provider.

			mItem = (LogItem)ItemListFragment.itemMap.get(getArguments().getString(
					ARG_ITEM_ID));			
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.log_fragment_item_detail,
				container, false);		

		// Show the dummy content as text in a TextView.
		if (mItem != null) {						
			((TextView) rootView.findViewById(R.id.item_detail))
			.setText(mItem.toString());
		}

		return rootView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onViewCreated(view, savedInstanceState);
	}
}
