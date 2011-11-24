package net.deflis.android.twitter.service;

import java.util.Iterator;

import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.User;
import twitter4j.UserMentionEntity;
import twitter4j.UserStreamAdapter;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import net.deflis.android.twitter.TwitterApiKey;
import net.deflis.android.twitter.storage.MasterStorage;
import net.deflis.android.twitter.storage.Tweet;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class TwitterService extends Service {
	protected static final String TAG = "TwitterService";
	protected final MasterStorage masterStorage = new MasterStorage();
	protected boolean mStreaming;
	private TwitterStream mTwitterStream = null;
	private Configuration twitterConfigration;
	private User mUser;

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		ConfigurationBuilder builder = new ConfigurationBuilder();

		Log.i(TAG, "Start service...");

		if (intent.hasExtra(TwitterApiKey.STRING_ACCESS_TOKEN) && intent.hasExtra(TwitterApiKey.STRING_ACCESS_SECRET) && intent.hasExtra(TwitterApiKey.STRING_CONSUMER_KEY) && intent.hasExtra(TwitterApiKey.STRING_CONSUMER_SECRET)) {
			Log.i(TAG, "Set keys");
			builder.setOAuthConsumerKey(intent.getStringExtra(TwitterApiKey.STRING_CONSUMER_KEY));
			builder.setOAuthConsumerSecret(intent.getStringExtra(TwitterApiKey.STRING_CONSUMER_SECRET));
			builder.setOAuthAccessToken(intent.getStringExtra(TwitterApiKey.STRING_ACCESS_TOKEN));
			builder.setOAuthAccessTokenSecret(intent.getStringExtra(TwitterApiKey.STRING_ACCESS_SECRET));
		}

		twitterConfigration = builder.build();

		mStreaming = intent.getBooleanExtra("streaming", false);

		Twitter twitter = new TwitterFactory(twitterConfigration).getInstance();

		try {
			mUser = twitter.showUser(twitter.getId());
		} catch (IllegalStateException e) {
			Log.i(TAG, "", e);
		} catch (TwitterException e) {
			Log.i(TAG, "", e);
		}

		if (mStreaming && mTwitterStream == null) {
			Log.i(TAG, "Start streaming..");
			TwitterStreamFactory factory = new TwitterStreamFactory(twitterConfigration);
			mTwitterStream = factory.getInstance();
			mTwitterStream.addListener(new UserStreamAdapter() {
				@Override
				public void onException(Exception e) {
					Log.i(TAG, "", e);
				}

				@Override
				public void onFriendList(long[] friendIds) {

				}

				@Override
				public void onStatus(Status status) {
					// Log.i(TAG, status.toString());
					masterStorage.add(new Tweet(status, mUser));
					Iterator<Tweet> iterator = masterStorage.iterator();
					int tweets = 0;
					int mentions = 0;
					int dm = 0;
					while (iterator.hasNext()) {
						Tweet tweet = iterator.next();
						boolean isMention = false;
						UserMentionEntity[] userMentionEntities = tweet.getStatus().getUserMentionEntities();
						for (UserMentionEntity userMentionEntity : userMentionEntities) {
							if (userMentionEntity.getId() == mUser.getId()) {
								isMention = true;
							}
						}
						if (tweet.isDirectMessage()) {
							dm++;
							if (dm > 100) {
								iterator.remove();
							}
						} else {
							tweets++;
							if (isMention) {
								mentions++;
							}
							if (tweets > 100) {
								if (!isMention) {
									iterator.remove();
								} else if (mentions > 50) {
									iterator.remove();
								}
							}
						}
					}
				}

				@Override
				public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
					masterStorage.remove(statusDeletionNotice);
				}
			});
			mTwitterStream.user();
		} else if (!mStreaming && mTwitterStream == null) {
			mTwitterStream.shutdown();
			mTwitterStream.cleanUp();
			mTwitterStream = null;
		}

	}

	@Override
	public IBinder onBind(Intent arg0) {
		masterStorage.startListen();
		return new TwitterServiceBinder(masterStorage);
	}

	@Override
	public void onRebind(Intent intent) {
		super.onRebind(intent);
	}

	@Override
	public boolean onUnbind(Intent intent) {
		return super.onUnbind(intent);
	}

	@Override
	public void onDestroy() {
		Log.i(TAG, "Stop service...");

		super.onDestroy();
		mTwitterStream.shutdown();
		mTwitterStream.cleanUp();
		mTwitterStream = null;
		System.gc();
	}

}
