package net.deflis.android.twitter.storage;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import twitter4j.DirectMessage;
import twitter4j.Status;
import twitter4j.User;

public class Tweet implements Comparable<Tweet> {
	protected final Status mStatus;
	protected final Status mOriginalStatus;
	protected final User mUser;
	protected final User mReciveUser;
	protected final boolean isRetweet;
	protected final boolean isDirectMessage;
	protected final DirectMessage mDirectMessage;
	protected final List<User> retweetUsers = new ArrayList<User>();
	
	public Tweet(DirectMessage message, User reciveUser) {
		isRetweet = false;
		isDirectMessage = true;
		mStatus = null;
		mOriginalStatus = null;
		mDirectMessage = message;
		mReciveUser = reciveUser;
		if (reciveUser.getId() == message.getRecipientId()) {
			mUser = message.getSender();
		} else {
			mUser = message.getRecipient();
		}
	}

	public Tweet(Status status, User reciveUser) {
		this.isDirectMessage = false;
		mDirectMessage = null;
		mReciveUser = reciveUser;
		if (this.isRetweet = status.isRetweet()) {
			mOriginalStatus = status;
			this.mStatus = status.getRetweetedStatus();
			retweetUsers.add(status.getUser());
		}else{
			this.mStatus = status;
			mOriginalStatus = status;
		}
		this.mUser = this.mStatus.getUser();
		
	}
	
	public long getId(){
		if(isDirectMessage)
			return mDirectMessage.getId();
		else
			return mStatus.getId();
	}

	public String getText(){
		if(isDirectMessage)
			return mDirectMessage.getText();
		else
			return mStatus.getText();
	}
	
	public User getUser(){
		return mUser;
	}

	public User getReciveUser(){
		return mReciveUser;
	}
	
	public Status getStatus(){
		return mStatus;
	}
	
	public Status getOrginalStatus(){
		return mOriginalStatus;
	}

	public Date getCreatedAt() {
		if(isDirectMessage)
			return mDirectMessage.getCreatedAt();
		else
			return mStatus.getCreatedAt();
	}
	
	public DirectMessage getDirectMessage(){
		return mDirectMessage;
	}

	@Override
	public int compareTo(Tweet another) {
		if(!this.isDirectMessage() && !another.isDirectMessage())
			return this.getOrginalStatus().compareTo(another.getOrginalStatus());
		else
			return this.getCreatedAt().compareTo(another.getCreatedAt());
	}

	public boolean isRetweet() {
		return isRetweet;
	}
	
	public boolean isDirectMessage(){
		return isDirectMessage;
	}
}
