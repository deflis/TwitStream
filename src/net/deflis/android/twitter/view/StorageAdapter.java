package net.deflis.android.twitter.view;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.deflis.android.twitter.R;
import net.deflis.android.twitter.storage.Storage;
import net.deflis.android.twitter.storage.Tweet;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class StorageAdapter extends ArrayAdapter<Tweet> {
	private final List<AsyncTask<?, ?, ?>> mTaskList = new ArrayList<AsyncTask<?, ?, ?>>();
	private final SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("HH:mm:ss");
	private final Storage mStorage;
	private final Map<URL, Bitmap> mImageCache = new HashMap<URL, Bitmap>(50);
	private final Context mContext;
	private final Drawable defaultIcon;
	protected boolean isNotifiOnChange = true;
	Handler handler = new Handler();

	private final Storage.OnTweetListener listener = new OnStatusListener();
	protected final Comparator<Tweet> mComperator = new StatusComparator();

	public StorageAdapter(Context context, Storage storage) {
		super(context, R.layout.status, R.id.textTextView);
		super.setNotifyOnChange(false);
		mContext = context;

		this.mStorage = storage;

		storage.registerOnTweetListener(listener);
		for (Tweet status : storage.toList()) {
			super.add(status);
		}
		storage.startListen();

		if (isNotifiOnChange)
			notifyDataSetChanged();
		defaultIcon = mContext.getResources().getDrawable(R.drawable.ic_launcher);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = super.getView(position, convertView, parent);
		Tweet status = this.getItem(position);
		ViewHolder holder = (ViewHolder) view.getTag();
		if (holder == null) {
			holder = new ViewHolder(view);
			view.setTag(holder);
		}
		holder.setStatus(status);
		return view;
	}

	protected class ViewHolder {
		protected Tweet mTweet;
		protected final TextView Title;
		protected final TextView ScreenName;
		protected final TextView Time;
		protected final ImageView Icon;
		protected URL imageUrl;
		protected ImageDownloadTask task;

		ViewHolder(View view) {
			Title = (TextView) view.findViewById(R.id.textTextView);
			ScreenName = (TextView) view.findViewById(R.id.idTextView);
			Time = (TextView) view.findViewById(R.id.timeTextView);
			Icon = (ImageView) view.findViewById(R.id.iconView);
		}

		public void setStatus(Tweet tweet) {
			if (task != null) {
				task.cancel(true);
			}
			if (!tweet.equals(mTweet)) {
				Title.setText(tweet.getText());
				ScreenName.setText((tweet.isRetweet() ? "RT " : tweet.getUser().isProtected() ? "○┯" : "") + tweet.getUser().getScreenName() + " " + tweet.getUser().getName());
				Time.setText(mSimpleDateFormat.format(tweet.getCreatedAt()));

				imageUrl = tweet.getUser().getProfileImageURL();
				if (mImageCache.containsKey(imageUrl)) {
					Icon.setImageBitmap(mImageCache.get(imageUrl));
				} else {
					task = new ImageDownloadTask(imageUrl, Icon);
					task.execute();
					mTaskList.add(task);
				}
			}
		}
	}

	@Override
	public void add(Tweet object) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void insert(Tweet status, int index) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void remove(Tweet status) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setNotifyOnChange(boolean notifyOnChange) {
		isNotifiOnChange = notifyOnChange;
	}

	public void sort() {
		super.sort(mComperator);
	}

	public void close() {
		super.clear();
		mStorage.unregisterOnTweetListener(listener);
		for (AsyncTask<?, ?, ?> task : mTaskList) {
			task.cancel(true);
		}
	}

	class StatusComparator implements Comparator<Tweet> {
		@Override
		public int compare(Tweet lhs, Tweet rhs) {
			return rhs.compareTo(lhs);
		}
	};

	class OnStatusListener implements Storage.OnTweetListener {
		@Override
		public void onAdd(Storage storage, final Tweet status) {
			handler.post(new Runnable() {
				@Override
				public void run() {
					StorageAdapter.super.add(status);
					sort();
					if (isNotifiOnChange)
						notifyDataSetChanged();
				}
			});
		}

		@Override
		public void onRemove(Storage storage, final Tweet status) {
			handler.post(new Runnable() {
				@Override
				public void run() {
					StorageAdapter.super.remove(status);
					if (isNotifiOnChange)
						notifyDataSetChanged();
				}
			});
		}
	}

	class ImageDownloadTask extends net.deflis.android.task.ImageDownloadTask {
		private URL URL;

		public ImageDownloadTask(URL url, ImageView imageView) {
			super(url.toString(), imageView);
			this.URL = url;
		}

		@Override
		protected void onPreExecute() {
			mImageView.setImageDrawable(defaultIcon);
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			super.onPostExecute(result);
			if(result != null){
				mImageCache.put(URL, result);
				mTaskList.remove(this);
			}
		}
	}
}
