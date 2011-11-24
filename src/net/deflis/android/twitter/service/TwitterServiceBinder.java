package net.deflis.android.twitter.service;

import net.deflis.android.twitter.storage.Storage;
import android.os.Binder;

public class TwitterServiceBinder extends Binder {
	protected final Storage mStorage;
	protected final TwitterQueryExecuter mExecuter;
	
	public TwitterServiceBinder(Storage masterStorage) {
		mStorage = masterStorage;
		mExecuter = new TwitterQueryExecuter(mStorage);
	}
	
	public Storage getStorage(){
		return mStorage;
	}
	
	public TwitterQueryExecuter getExecuter(){
		return mExecuter;
	}
}
