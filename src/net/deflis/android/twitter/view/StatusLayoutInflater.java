package net.deflis.android.twitter.view;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import net.deflis.android.task.ImageDownloadTask;
import net.deflis.android.twitter.R;
import twitter4j.Status;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class StatusLayoutInflater {
	private final LayoutInflater mInflater;
	private final ViewGroup mRoot;
	private final List<AsyncTask<?, ?, ?>> mTaskList = new ArrayList<AsyncTask<?,?,?>>();
	private SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("HH:mm:ss");

	public StatusLayoutInflater(LayoutInflater inflater, ViewGroup root) {
		mInflater = inflater;
		mRoot = root;
	}

	public View getLayout(Status status) {
		View view = mInflater.inflate(R.layout.status, mRoot);

		TextView title = (TextView) view.findViewById(R.id.textTextView);
		TextView screenName = (TextView) view.findViewById(R.id.idTextView);
		TextView time = (TextView) view.findViewById(R.id.timeTextView);
		

		title.setText(status.getText());
		screenName.setText((status.getUser().isProtected() ? "○┯" : "") + status.getUser().getScreenName() + " " + status.getUser().getName());
		time.setText(mSimpleDateFormat.format(status.getCreatedAt()));
		
		ImageDownloadTask task = new ImageDownloadTask(status.getUser().getProfileImageURL().toString(), (ImageView) view.findViewById(R.id.iconView));
		task.execute();
		return view;
	}

	public void close(){
		for(AsyncTask<?, ?, ?> task: mTaskList){
			task.cancel(true);
		}
	}
			
}
