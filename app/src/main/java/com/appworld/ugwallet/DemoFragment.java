package com.appworld.ugwallet;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.SearchView;
import android.widget.Toast;

import com.afollestad.bridge.Bridge;
import com.afollestad.bridge.BridgeException;
import com.afollestad.bridge.Response;
import com.afollestad.bridge.ResponseConvertCallback;
import com.appworld.ugwallet.adapters.PointsAdapter;
import com.appworld.ugwallet.models.OnLoadMoreListener;
import com.appworld.ugwallet.utils.Utils;
import com.appworld.ugwallet.widgets.RecyclerViewEmptySupport;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import static com.appworld.ugwallet.AgentHistoryActivty.TAG;
import static com.appworld.ugwallet.AgentHistoryActivty.clientObject;


/**
 *
 */
public class DemoFragment extends Fragment {
	
	private FrameLayout fragmentContainer;
	private RecyclerViewEmptySupport recyclerView;
	private SwipeRefreshLayout refreshLayout;
	private RecyclerView.LayoutManager layoutManager;
	SearchView searchView;


	ArrayList<Object> itemsDataEmp = new ArrayList<>();
	private String nextUrl = null;


	/**
	 * Create a new instance of the fragment
	 */
	public static DemoFragment newInstance(int index) {
		DemoFragment fragment = new DemoFragment();
		Bundle b = new Bundle();
		b.putInt("index", index);
		fragment.setArguments(b);
		return fragment;
	}
	public static DemoFragment newInstance(int index,int outlet) {
		DemoFragment fragment = new DemoFragment();
		Bundle b = new Bundle();
		b.putInt("index", index);
		b.putInt("outlet", outlet);
		fragment.setArguments(b);
		return fragment;
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		View view = null;
				switch (getArguments().getInt("index", 0)){

					case 0: // for all clients
                        view = inflater.inflate(R.layout.fragment_demo_list_clients, container, false);
                        initClientsSearchList(view,0);
						break;
					case 1: // for all clients
						view = inflater.inflate(R.layout.fragment_demo_list_clients, container, false);
						initClientsSearchList(view,1);
						break;
					case 2: // for assets pager
                        view = inflater.inflate(R.layout.fragment_demo_list_clients, container, false);
                        initClientsSearchList(view,2);
						break;

				}

		return view;
	}

	private void initClientsSearchList(View view, final int frag) {
		final ArrayList<Object> itemsData = new ArrayList<>();
		final PointsAdapter adapter;
		fragmentContainer = (FrameLayout) view.findViewById(R.id.fragment_container);
		searchView = (SearchView) view.findViewById(R.id.search_view);

		searchView.setQueryHint("Search Transactions");

		recyclerView = (RecyclerViewEmptySupport) view.findViewById(R.id.fragment_demo_recycler_view);
		recyclerView.setHasFixedSize(true);
		layoutManager = new LinearLayoutManager(getActivity());
		recyclerView.setLayoutManager(layoutManager);
		refreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
		recyclerView.setEmptyView(
				view.findViewById(R.id.list_empty)
		);

		adapter = new PointsAdapter(getContext(),itemsData,frag,recyclerView);
//        final BasicAdapter adapter = new BasicAdapter(getActivity(),getContext(), Object_types.client,itemsData,user,recyclerView);
        recyclerView.setAdapter(adapter);

		adapter.setOnLoadMoreListener(new OnLoadMoreListener() {
			@Override
			public void onLoadMore() {
				if (nextUrl != null &&	!nextUrl.isEmpty() && nextUrl.startsWith("http")) {
					itemsData.add(null);
					//adapter.notifyItemInserted(itemsData.size() - 1);
					recyclerView.post(new Runnable() {
						public void run() {
							adapter.notifyItemInserted(itemsData.size() - 1);
						}
					});
					getAllTransactions(itemsData, adapter,refreshLayout, searchView.getQuery().toString(),frag,true);

				} else {
					Toast.makeText(getContext(), "Loading data completed", Toast.LENGTH_SHORT).show();
				}
			}
		});

		refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
                getAllTransactions(itemsData, adapter,refreshLayout, searchView.getQuery().toString(),frag, false);
			}
		});



		getAllTransactions(itemsData, adapter,refreshLayout, searchView.getQuery().toString(),frag, false);
		searchView.setOnQueryTextListener(
				new SearchView.OnQueryTextListener() {
					@Override
					public boolean onQueryTextSubmit(String s) {
						getAllTransactions(itemsData, adapter,refreshLayout, s,frag, false);
						return  true;
					}
					@Override
					public boolean onQueryTextChange(String s) {
						getAllTransactions(itemsData, adapter,refreshLayout, s,frag, false);
						return  true;
					}
				}
		);
	}

	private void getAllTransactions(final ArrayList<Object> itemsData, final PointsAdapter adapter, final SwipeRefreshLayout refreshLayout, String search, final int frag, final boolean loadmore) {
		String url = "";

		switch (frag){
			case 0:
				url = Utils.WALLET_TOPUS_NEW;
				break;
			case 1:
				url = Utils.WALLET_TRANSACTIONS_NEW;
				break;
			case 2:
				url = Utils.WALLET_EARNINGS_NEW;
				break;
		}

		Bridge.cancelAll().tag("transactions"+frag).commit();
        if(search!=null && search.length()>0){
            try {
                search = URLEncoder.encode(search,"UTF-8");
                url = url+"?search="+search;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
		if (loadmore){
			url=nextUrl;
		}else {
			refreshLayout.setRefreshing(true);
		}

		try {
        	final String token = clientObject.getString("token");
			Log.d(TAG, "URL "+ url);
			Bridge.get(url)

					.tag("transactions"+frag)
					.connectTimeout(Utils.VOLLEY_TIMEOUT_MS)
					.readTimeout(Utils.VOLLEY_TIMEOUT_MS)
					.header("Authorization","Token "+clientObject.getString("token"))
					.asString(new ResponseConvertCallback<String>() {
				@Override
				public void onResponse(@org.jetbrains.annotations.Nullable Response response, @org.jetbrains.annotations.Nullable String s, @org.jetbrains.annotations.Nullable BridgeException e) {

					refreshLayout.setRefreshing(false);
					if(loadmore){
						itemsData.remove(itemsData.size() - 1);
						adapter.notifyItemRemoved(itemsData.size());
					}
					if(Utils.DEBUG){
						Log.d(TAG, "Token "+ token);
						Log.d(TAG, "onResponse: "+s);
					}
					if(s==null){
						Utils.showToastMessage(getContext(),"Please check your connection and try again");
						return;
					}
					try {
						JSONObject jsonObject = new JSONObject(s);
						if(jsonObject.has("error")){
							new AlertDialog.Builder(getContext())
									.setTitle("Error")
									.setMessage(jsonObject.getString("error"))
									.setCancelable(false)
									.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialogInterface, int i) {
											dialogInterface.dismiss();
										}
									}).show();
							return;
						}
						nextUrl = null;
						if (jsonObject.has("next")) {
							nextUrl = Utils.WALLET_HOST_URL + jsonObject.getString("next");
						}
						JSONArray jsonArray = jsonObject.getJSONArray("results");
						if (!loadmore){
							itemsData.clear();
						}

                        /*//add empty object for header
                        transactions.add(new JSONObject());*/
						for (int i = 0; i < jsonArray.length(); i++) {
							itemsData.add(jsonArray.getJSONObject(i));
						}
						adapter.notifyDataSetChanged();
						adapter.setLoaded();
					} catch (JSONException e1) {
						e1.printStackTrace();
					}

				}
			});
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}



	/**
	 * Init the fragment
	 */

	/**
	 * Refresh
	 */
	public void refresh() {
		if (getArguments().getInt("index", 0) > 0 && recyclerView != null) {
			recyclerView.smoothScrollToPosition(0);
		}
	}
	
	/**
	 * Called when a fragment will be displayed
	 */
	public void willBeDisplayed() {
		// Do what you want here, for example animate the content
		if (fragmentContainer != null) {
			Animation fadeIn = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in);
			fragmentContainer.startAnimation(fadeIn);
		}
	}
	
	/**
	 * Called when a fragment will be hidden

	 */
	public void willBeHidden() {
		if (fragmentContainer != null) {
			Animation fadeOut = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_out);
			fragmentContainer.startAnimation(fadeOut);
		}
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
//		mViewPager = (ViewPager)findViewById(R.id.pager);

	}
}
