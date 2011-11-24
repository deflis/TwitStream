package net.deflis.android.twitter;

import net.deflis.android.twitter.service.TwitterService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Window;
import android.os.Build;

public class TwitStreamActivity extends FragmentActivity {
	private static final int REQUEST_LOGIN = 100;
	@SuppressWarnings("unused")
	private static final String TAG = "TwitStreamActivity";
	private FragmentManager fragmentManager;

	@SuppressWarnings("unused")
	private Handler handler;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		DisplayMetrics displayMetrics = new DisplayMetrics();
		Log.i("ImageDownloadTask", Float.toString(displayMetrics.density) + " / " + Float.toString(displayMetrics.scaledDensity) + " / " + Float.toString(displayMetrics.densityDpi));

		super.onCreate(savedInstanceState);
		if (Build.VERSION.SDK_INT > 10) {
			getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
		}
		handler = new Handler();

		TwitterApiKey.getApiKey(getApplicationContext());

		TwitterApiKey apiKey = TwitterApiKey.getApiKey(getApplicationContext());

		if (!apiKey.hasToken()) {
			Intent intent = new Intent(getApplicationContext(), TwitterLoginActivity.class);
			intent.putExtra(TwitterLoginActivity.CALLBACK, "http://deflis.net/ts/login");
			intent.putExtra(TwitterLoginActivity.CONSUMER_KEY, apiKey.getConsumerKey());
			intent.putExtra(TwitterLoginActivity.CONSUMER_SECRET, apiKey.getConsumerSecret());
			startActivityForResult(intent, REQUEST_LOGIN);
			return;
		}

		Intent intent = new Intent(this, TwitterService.class);
		intent.putExtra("streaming", true);
		intent.putExtra(TwitterApiKey.STRING_CONSUMER_KEY, apiKey.getConsumerKey());
		intent.putExtra(TwitterApiKey.STRING_CONSUMER_SECRET, apiKey.getConsumerSecret());
		intent.putExtra(TwitterApiKey.STRING_ACCESS_TOKEN, apiKey.getAccessToken());
		intent.putExtra(TwitterApiKey.STRING_ACCESS_SECRET, apiKey.getAccessSecret());
		startService(intent);

		setContentView(R.layout.main);

		fragmentManager = getSupportFragmentManager();

		Bundle bundle = new Bundle();
		bundle.putString("query", "");

		Fragment fragment = new TimelineFragment();
		fragment.setArguments(bundle);

		fragmentManager.beginTransaction().replace(R.id.fragment1, fragment).commit();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO 自動生成されたメソッド・スタブ
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
		case REQUEST_LOGIN:
			if (resultCode == RESULT_OK) {
				TwitterApiKey.setApiKey(getApplicationContext(), data.getStringExtra(TwitterLoginActivity.TOKEN), data.getStringExtra(TwitterLoginActivity.TOKEN_SECRET));
			}
			break;

		default:
			break;
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		stopService(new Intent(this, TwitterService.class));
		System.gc();
	}

}