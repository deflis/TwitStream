package net.deflis.android.twitter.storage;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import twitter4j.StatusDeletionNotice;

public class MasterStorage extends Storage {
	List<Tweet> statuses = new CopyOnWriteArrayList<Tweet>();

	@Override
	public void add(Tweet status) {
		statuses.add(status);
		onAdd(status);
	}

	@Override
	public int size() {
		return statuses.size();
	}

	@Override
	public Tweet get(int location) {
		return statuses.get(location);
	}

	@Override
	public Tweet remove(int location) {
		Tweet status = statuses.remove(location);
		onRemove(status);
		return status;
	}

	@Override
	public boolean remove(Tweet status) {
		if (statuses.remove(status)) {
			onRemove(status);
			return true;
		} else {
			return false;
		}
	}

	public Tweet remove(StatusDeletionNotice statusDeletionNotice) {
		Tweet status = null;
		for (Tweet s : statuses) {
			if (s.getId() == statusDeletionNotice.getStatusId()) {
				status = s;
				break;
			}
		}
		if (status != null) {
			this.remove(status);
		}
		return status;
	}

	@Override
	public List<Tweet> toList() {
		return statuses.subList(0, statuses.size());
	}

	@Override
	public Tweet[] toArray() {
		List<Tweet> list = this.toList();
		Tweet[] array = new Tweet[list.size()];
		return toList().toArray(array);
	}

	@Override
	public Iterator<Tweet> iterator() {
		return statuses.iterator();
	}
}
