package net.deflis.android.twitter.storage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import net.deflis.android.twitter.filter.Filter;

public class FilteredStorage extends Storage {
	protected final Storage mStorage;
	protected final Filter mFilter;

	public FilteredStorage(Storage storage, Filter filter) {
		this.mStorage = storage;
		this.mFilter = filter;
		storage.registerOnTweetListener(listener);
	}

	@Override
	public void close() {
		mStorage.unregisterOnTweetListener(listener);
		super.close();
	}
	
	@Override
	public void add(Tweet status) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Tweet get(int location) {
		if(!isClosed())
			throw new IndexOutOfBoundsException();
		Iterator<Tweet> iterator = iterator();
		for (int i = 0; i < location; i++) {
			iterator.next();
			if(!iterator.hasNext())
				throw new IndexOutOfBoundsException();
		}
		return iterator.next();
	}

	@Override
	public Tweet remove(int location) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean remove(Tweet status) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Tweet> toList() {
		ArrayList<Tweet> list = new ArrayList<Tweet>();
		Iterator<Tweet> iterator = iterator();
		while (iterator.hasNext()) {
			list.add(iterator.next());
		}
		return list;
	}

	@Override
	public Iterator<Tweet> iterator() {
		return new Iterator<Tweet>() {
			private final Iterator<Tweet> iterator = mStorage.iterator();
			private Tweet next = null;
			private boolean hasNextExecuted = false;

			@Override
			public boolean hasNext() {
				hasNextExecuted = true;
				while (iterator.hasNext()) {
					next = iterator.next();
					if (mFilter.filter(next)) {
						return true;
					}
				}
				next = null;
				return false;
			}

			@Override
			public Tweet next() {
				if (!hasNextExecuted)
					if (!hasNext())
						throw new NoSuchElementException();
				if (next == null)
					throw new NoSuchElementException();
				hasNextExecuted = false;
				return next;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	private final OnTweetListener listener = new OnTweetListener() {

		@Override
		public void onAdd(Storage storage, Tweet status) {
			if (mFilter.filter(status)) {
				FilteredStorage.this.onAdd(status);
			}
		}

		@Override
		public void onRemove(Storage storage, Tweet status) {
			if (mFilter.filter(status)) {
				FilteredStorage.this.onRemove(status);
			}
		}
	};
}
