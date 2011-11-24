package net.deflis.android.twitter.service;

import net.deflis.android.twitter.filter.Query;
import net.deflis.android.twitter.storage.FilteredStorage;
import net.deflis.android.twitter.storage.Storage;

public class TwitterQueryExecuter {
	protected final Storage mStorage;

	public TwitterQueryExecuter(Storage storage) {
		this.mStorage = storage;
	}

	public Storage executeQuery(Query query) {
		return new FilteredStorage(mStorage, query.getFilter());
	}

}
