package net.deflis.android.twitter.storage;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class Storage implements Iterable<Tweet> {
	private final List<OnTweetListener> listeners = new CopyOnWriteArrayList<OnTweetListener>();
	private boolean isClosed = false;
	private boolean isStarted = false;

	protected void onAdd(Tweet status){
		if (isStarted) {
			Iterator<OnTweetListener> iterator = listeners.iterator();
			while(iterator.hasNext()){
				iterator.next().onAdd(this, status);
			}
		}
	}

	protected void onRemove(Tweet status){
		if (isStarted) {
			Iterator<OnTweetListener> iterator = listeners.iterator();
			while(iterator.hasNext()){
				iterator.next().onRemove(this, status);
			}
		}
	}

	public abstract void add(Tweet status);
	public abstract Tweet get(int location);
	public abstract Tweet remove(int location);
	public abstract boolean remove(Tweet status);

	public abstract List<Tweet> toList();	

	protected boolean isClosed() {
		return isClosed;
	}

	public void addAll(Collection<? extends Tweet> statuses) {
		Iterator<? extends Tweet> iterator = statuses.iterator();

		while(iterator.hasNext()){
			add(iterator.next());
		}
	}

	public void close() {
		isStarted = false;
		isClosed = true;
	}
	
	public int size() {
		if(isClosed)
			return 0;
		int size = 0;
		while(iterator().hasNext()){
			size++;
			iterator().next();
		}
		return size;
	}
	
	public Tweet[] toArray() {
		List<Tweet> list = this.toList();
		Tweet[] array = new Tweet[list.size()];
		return toList().toArray(array);
	}

	public boolean startListen() {
		if (!isClosed)
			return isStarted = true;
		else
			return false;
	}

	public boolean registerOnTweetListener(OnTweetListener listener) {
		if (!isClosed)
			return listeners.add(listener);
		else
			return false;
	}

	public boolean unregisterOnTweetListener(OnTweetListener listener) {
		if (!isClosed)
			return listeners.remove(listener);
		else
			return false;
	}
	
	public interface OnTweetListener{
		void onAdd(Storage storage, Tweet status);
		void onRemove(Storage storage, Tweet status);
	}
}
