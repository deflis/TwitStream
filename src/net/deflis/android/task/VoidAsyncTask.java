package net.deflis.android.task;

import android.os.AsyncTask;

public abstract class VoidAsyncTask extends AsyncTask<Void, Void, Void> {

	public static VoidAsyncTask executeRunnable(final Runnable r){
		VoidAsyncTask task = new VoidAsyncTask() {
			@Override
			protected void doInBackground() {
				r.run();
			}
		};
		task.execute();
		return task;
	}
	
	
	public void execute() {
		execute((Void) null);
	}

	@Override
	protected Void doInBackground(Void... params) {
		doInBackground();
		return null;
	}
	
	protected abstract void doInBackground();

}
