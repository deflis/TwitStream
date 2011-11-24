package net.deflis.android.twitter;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class TwitterApiKey {
	public static final String STRING_CONSUMER_KEY = "consumer_key";
	public static final String STRING_CONSUMER_SECRET = "consumer_secret";
	public static final String STRING_ACCESS_TOKEN = "access_token";
	public static final String STRING_ACCESS_SECRET = "access_secret";

	private final String consumerKey = "iS8B2OQzEHPibmvVtlPeIQ";
	private final String consumerSecret = "OPJFs0Pt3fTNtpdtyLe2L54oJQsHbPkvuUoykoNZvU";
	private final String accessToken;
	private final String accessSecret;
	private final boolean hasToken;

	public String getConsumerKey() {
		return consumerKey;
	}

	public String getConsumerSecret() {
		return consumerSecret;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public String getAccessSecret() {
		return accessSecret;
	}

	public boolean hasToken() {
		return hasToken;
	}

	public static TwitterApiKey getApiKey(Context context) {
		return new TwitterApiKey(context.getApplicationContext());
	}

	public static void setApiKey(Context context, String accessToken, String accessSecret) {
		SharedPreferences pref = context.getApplicationContext().getSharedPreferences("twitter", Context.MODE_WORLD_WRITEABLE);
		Editor editor = pref.edit();
		editor.putString(STRING_ACCESS_TOKEN, accessToken);
		editor.putString(STRING_ACCESS_SECRET, accessSecret);
		editor.commit();
	}

	private TwitterApiKey(Context context) {
		SharedPreferences pref = context.getSharedPreferences("twitter", Context.MODE_WORLD_READABLE);
		hasToken = pref.contains(STRING_ACCESS_TOKEN) && pref.contains(STRING_ACCESS_SECRET);

		accessToken = pref.getString(STRING_ACCESS_TOKEN, null);
		accessSecret = pref.getString(STRING_ACCESS_SECRET, null);
	}
}
