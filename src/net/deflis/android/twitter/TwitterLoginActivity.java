package net.deflis.android.twitter;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class TwitterLoginActivity extends Activity {
	public static final String CALLBACK = "callback";
	public static final String CONSUMER_KEY = "consumer_key";
	public static final String CONSUMER_SECRET = "consumer_secret";

	public static final String USER_ID = "user_id";
	public static final String SCREEN_NAME = "screen_name";
	public static final String TOKEN = "token";
	public static final String TOKEN_SECRET = "token_secret";

	protected static final String TAG = "TwitterLoginActivity";
	protected static final String OAUTH_VERIFIER = "oauth_verifier";
	protected WebView mWebView;
	protected String mCallback;
	protected Twitter mTwitter;

	private WebChromeClient mWebChromeClient = new CustomWebChromeClient();
	private WebViewClient mWebViewClient = new CustomWebViewClient();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		if (!intent.hasExtra(CALLBACK) && !intent.hasExtra(CONSUMER_KEY) && !intent.hasExtra(CONSUMER_SECRET)) {
			finish();
			return;
		}

		mCallback = intent.getStringExtra(CALLBACK);
		String consumerKey = intent.getStringExtra(CONSUMER_KEY);
		String consumerSecret = intent.getStringExtra(CONSUMER_SECRET);

		mWebView = new WebView(this);
		WebSettings webSettings = mWebView.getSettings();
		webSettings.setSavePassword(false);
		webSettings.setSaveFormData(false);
		webSettings.setJavaScriptEnabled(true);
		webSettings.setPluginsEnabled(false);
		mWebView.setScrollBarStyle(WebView.SCROLLBARS_INSIDE_OVERLAY);
		mWebView.setVerticalScrollbarOverlay(true);
		mWebView.setWebChromeClient(mWebChromeClient);
		mWebView.setWebViewClient(mWebViewClient);

		CookieManager cookieManager = CookieManager.getInstance();
		cookieManager.setAcceptCookie(false);

		requestWindowFeature(Window.FEATURE_PROGRESS);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(mWebView);

		LayoutParams params = getWindow().getAttributes();
		params.width = LayoutParams.FILL_PARENT;
		params.height = LayoutParams.FILL_PARENT;
		getWindow().setAttributes(params);

		mTwitter = TwitterFactory.getSingleton();
		mTwitter.setOAuthConsumer(consumerKey, consumerSecret);

		new PreTask(mTwitter).execute();
	}

	protected void hookCallbackUrl(String url) {
		Uri uri = Uri.parse(url);
		new PostTask(mTwitter).execute(uri.getQueryParameter(OAUTH_VERIFIER));
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mWebView.destroy();
		mTwitter.shutdown();
	}

	protected class PreTask extends AsyncTask<Void, Void, String> {
		protected final Twitter mTwitter;

		public PreTask(Twitter twitter) {
			this.mTwitter = twitter;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			setProgressBarIndeterminateVisibility(true);
			setProgressBarIndeterminate(true);
			setProgressBarVisibility(true);
		}

		@Override
		protected String doInBackground(Void... params) {
			String authorizationUrl = null;
			try {
				RequestToken requestToken = mTwitter.getOAuthRequestToken();
				if (requestToken != null)
					authorizationUrl = requestToken.getAuthenticationURL();
			} catch (TwitterException e) {
				Log.e(TAG, "", e);
			}
			return authorizationUrl;
		}

		@Override
		protected void onPostExecute(String result) {
			if (result != null) {
				mWebView.loadUrl(result);
			} else {
				setResult(RESULT_CANCELED);
				finish();
			}
		}
	}

	protected class PostTask extends AsyncTask<String, Void, AccessToken> {
		protected final Twitter mTwitter;

		public PostTask(Twitter twitter) {
			this.mTwitter = twitter;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			setProgressBarIndeterminateVisibility(true);
			setProgressBarIndeterminate(true);
		}

		@Override
		protected AccessToken doInBackground(String... params) {
			AccessToken accessToken = null;
			if (params != null) {
				try {
					accessToken = mTwitter.getOAuthAccessToken(params[0]);
				} catch (TwitterException e) {
					Log.e(TAG, "", e);
				}
			}
			return accessToken;
		}

		@Override
		protected void onPostExecute(AccessToken result) {
			if (result != null) {
				Intent intent = new Intent();
				intent.putExtra(USER_ID, result.getUserId());
				intent.putExtra(SCREEN_NAME, result.getScreenName());
				intent.putExtra(TOKEN, result.getToken());
				intent.putExtra(TOKEN_SECRET, result.getTokenSecret());
				setResult(RESULT_OK, intent);
			} else {
				setResult(RESULT_CANCELED);
			}
			super.onPostExecute(result);
			finish();
		}
	}

	protected class CustomWebChromeClient extends WebChromeClient {
		@Override
		public void onProgressChanged(WebView view, int newProgress) {
			super.onProgressChanged(view, newProgress);
			setProgress(newProgress * 100);
		}
	}

	protected class CustomWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			if (url != null) {
				if (url.startsWith(mCallback)) {
					hookCallbackUrl(url);
					return false;
				} else if (url.matches("https?://.*\\.?twitter\\.[a-z\\.]/.*")) {
					return true;
				} else {
					try {
						startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
					} catch (ActivityNotFoundException e) {
					}
					return false;
				}
			}
			return false;
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			if (url != null && url.startsWith(mCallback)) {
				hookCallbackUrl(url);
				return;
			}
			setProgressBarIndeterminateVisibility(true);
			setProgressBarIndeterminate(true);
			setProgressBarVisibility(true);
			super.onPageStarted(view, url, favicon);
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			// TODO 自動生成されたメソッド・スタブ
			super.onPageFinished(view, url);
			setProgressBarIndeterminateVisibility(false);
			setProgressBarVisibility(false);

		}

		@Override
		public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
			super.onReceivedError(view, errorCode, description, failingUrl);
			setResult(RESULT_CANCELED);
			finish();
		}
	}
}
