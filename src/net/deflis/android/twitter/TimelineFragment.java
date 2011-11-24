package net.deflis.android.twitter;

import net.deflis.android.twitter.filter.StringQuery;
import net.deflis.android.twitter.service.TwitterQueryExecuter;
import net.deflis.android.twitter.service.TwitterService;
import net.deflis.android.twitter.service.TwitterServiceBinder;
import net.deflis.android.twitter.storage.Storage;
import net.deflis.android.twitter.view.StorageAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ListFragment;

public class TimelineFragment extends ListFragment {

	private Storage mStorage;
	private StorageAdapter mStorageAdapter;
	protected TwitterServiceBinder mBinder;

	protected ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			mStorageAdapter.close();
			mStorage.close();
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mBinder = (TwitterServiceBinder) service;
			String query = "";
			if(getArguments() !=null){
				query = getArguments().getString("Query");
			}
			TwitterQueryExecuter executer = mBinder.getExecuter();
			mStorage = executer.executeQuery(new StringQuery(query));
			
			mStorageAdapter = new StorageAdapter(getActivity(), mStorage);
			setListAdapter(mStorageAdapter);

		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActivity().bindService(new Intent(getActivity(), TwitterService.class), serviceConnection, Context.BIND_AUTO_CREATE);
	}
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}
	
	@Override
	public void onResume() {
		super.onResume();
	}
	
	@Override
	public void onStop() {
		super.onStop();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		getActivity().unbindService(serviceConnection);
		if (mStorageAdapter != null) {
			mBinder = null;
		}
		if (mStorageAdapter != null) {
			mStorageAdapter.close();
		}
		if (mStorage != null) {
			mStorage.close();
		}
	}
}
