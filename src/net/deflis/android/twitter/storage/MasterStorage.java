package net.deflis.android.twitter.storage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import android.util.Log;

import twitter4j.StatusDeletionNotice;
import twitter4j.UserMentionEntity;

public class MasterStorage extends Storage {
	List<Tweet> statuses = new CopyOnWriteArrayList<Tweet>();
	private int maxTweets = 100;
	private int maxDirectMessages = 100;
	private int maxMentions = 50;

	@Override
	public void add(Tweet status) {
		if(!statuses.contains(status)){
			statuses.add(status);
			onAdd(status);
		}
		List<Tweet> statuses = new ArrayList<Tweet>(this.toList());
		Collections.sort(statuses, new Comparator<Tweet>(){
			@Override
			public int compare(Tweet lhs, Tweet rhs) {
				return rhs.compareTo(lhs);
			}});
		Iterator<Tweet> iterator = statuses.iterator();
		int tweets = 0;
		int mentions = 0;
		int dm = 0;
		while (iterator.hasNext()) {
			Tweet currentTweet = iterator.next();
			boolean isMention = false;
			if (currentTweet.isDirectMessage()) {
				dm++;
				if (dm > maxDirectMessages) {
					remove(currentTweet);
				}
			} else {
				UserMentionEntity[] userMentionEntities = currentTweet.getStatus().getUserMentionEntities();
				for (UserMentionEntity userMentionEntity : userMentionEntities) {
					if (userMentionEntity.getId() == status.getReciveUser().getId()) {
						isMention = true;
					}
				}
				tweets++;
				if (isMention) {
					mentions++;
				}
				if (tweets > maxTweets) {
					if (!isMention) {
						Log.i("MasterStorage", "removed " + currentTweet.getStatus().getText() + " tweets=" + tweets + " mentions=" + mentions);
						remove(currentTweet);
					} else if (mentions > maxMentions) {
						remove(currentTweet);
					}
				}
			}
		}

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
