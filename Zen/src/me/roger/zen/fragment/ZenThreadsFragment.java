package me.roger.zen.fragment;

import java.util.ArrayList;

import me.roger.zen.R;
import me.roger.zen.activity.ZenMainActivity;
import me.roger.zen.adapter.ZenThreadsAdapter;
import me.roger.zen.data.ZenThreadData;
import me.roger.zen.model.ZenPhotoModel;
import me.roger.zen.model.ZenThreadsModel;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.devspark.appmsg.AppMsg;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class ZenThreadsFragment extends Fragment {
	
	static final String LOG_TAG = "Zen";
	static final String ZEN_AD_UNIT_ID = "a1535f6167e679e";
	public String fid;
	private PullToRefreshListView mThreadsListView;
	private ZenThreadsAdapter mThreadsAdapter;
	private ListView mList;
	private ZenThreadsModel mModel;
	private Context mContext;
	private boolean isFirstTime;
	AdView mAds;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mContext = getActivity();
		
		mThreadsAdapter = new ZenThreadsAdapter(mContext);
		mThreadsAdapter.array = new ArrayList<ZenThreadData>();
		mThreadsListView = (PullToRefreshListView)getView().findViewById(R.id.zen_threads_list);
		
		mThreadsListView.setOnRefreshListener(new OnRefreshListener2<ListView>() {

			@Override
			public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
				ZenMainActivity ac = (ZenMainActivity)mContext;
				if(ac.isLoading()) {
					mThreadsListView.onRefreshComplete();
					return;
				}
				mModel.refresh();
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
				ZenMainActivity ac = (ZenMainActivity)mContext;
				if(ac.isLoading()) {
					mThreadsListView.onRefreshComplete();
					return;
				}
				Log.v(LOG_TAG, "Pull To Load More Task Execute.");
				mModel.loadMore();
			}
			
		});
		
		ListView actureListView = (ListView)mThreadsListView.getRefreshableView();
		mList = actureListView;
		AdView ads = new AdView(mContext);
		ads.setAdUnitId(ZEN_AD_UNIT_ID);
		ads.setAdSize(AdSize.BANNER);
		mAds = ads;
		mList.addHeaderView(ads);
		AdRequest adRequest = new AdRequest.Builder().build();
		ads.loadAd(adRequest);
		actureListView.setAdapter(mThreadsAdapter);
		actureListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				System.out.println("position: " + position);
				if (getActivity() instanceof ZenMainActivity) {
					ZenMainActivity ar = (ZenMainActivity)getActivity();
					ZenThreadData data = (ZenThreadData)parent.getAdapter().getItem(position);
					//ZenThreadData data = (ZenThreadData)mModel.threads.get(position);
					ar.onThreadClicked(data.fid, data.tid);
				}
			}
			
		});
		mModel = new ZenThreadsModel(mContext, fid);
		isFirstTime = true;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.zen_threads_list, null);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		IntentFilter filter = new IntentFilter();
		filter.addAction(ZenThreadsModel.DidFinishedLoad);
		filter.addAction(ZenThreadsModel.DidFailedLoad);
		mContext.registerReceiver(mBroadcastReceiver, filter);
		mAds.resume();
		IntentFilter adsFilter = new IntentFilter();
		adsFilter.addAction(ZenPhotoModel.ZEN_PHOTO_FINISHED);
				
		if (isFirstTime) {
			mModel.refresh();
			ZenMainActivity ac = (ZenMainActivity)mContext;
			ac.showLoadingView(true);
			isFirstTime = false;
		}
		
	}
	
	@Override
	public void onPause() {
		try {
			mModel.cancel();
			mContext.unregisterReceiver(mBroadcastReceiver);
			mAds.pause();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		super.onPause();
	}
	
	@Override
	public void onDestroy() {
		mAds.destroy();
		super.onDestroy();
	}
	
	public void refresh() {
		mModel.refresh();
	}
	

	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			// model finished load threads
			Log.d("zen", "onReceive");
			ZenMainActivity ac = (ZenMainActivity)mContext;
			ac.showLoadingView(false);
			mThreadsListView.onRefreshComplete();
			String action = intent.getAction();
			if (action.equals(ZenThreadsModel.DidFinishedLoad)) {
				
				mThreadsAdapter.array = mModel.threads;
				mThreadsAdapter.notifyDataSetChanged();
				mList.scrollTo(0, 0);
			}
			else if(action.equals(ZenThreadsModel.DidFailedLoad)) {
				AppMsg appmsg = AppMsg.makeText(ac, "º”‘ÿ ß∞‹...", AppMsg.STYLE_ALERT);
				appmsg.show();
			}
		}
	};
}
