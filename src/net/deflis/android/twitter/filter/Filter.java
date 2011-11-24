package net.deflis.android.twitter.filter;

import net.deflis.android.twitter.storage.Tweet;

public abstract class Filter {
	public boolean filter(Tweet status){
		return true;
	}
}
