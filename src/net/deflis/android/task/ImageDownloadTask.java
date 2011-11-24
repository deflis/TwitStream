package net.deflis.android.task;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import net.deflis.util.FileUtil;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.DisplayMetrics;
import android.widget.ImageView;

public class ImageDownloadTask extends DownloadTask<Void, Bitmap> {
	protected final String mUrl;
	protected final int mWidth;
	protected final int mHeight;
	protected final ImageView mImageView;
	protected final File mCacheDir;

	public void execute() {
		super.execute((Void) null);
	}

	public ImageDownloadTask(String url) {
		this(url, -1, -1, null);
	}

	public ImageDownloadTask(String url, File cacheDir) {
		this(url, -1, -1, null);
	}

	public ImageDownloadTask(String url, int width, int height) {
		this(url, width, height, null);
	}

	public ImageDownloadTask(String url, int width, int height, File cacheDir) {
		this.mUrl = url;
		this.mWidth = width;
		this.mHeight = height;
		this.mImageView = null;
		this.mCacheDir = cacheDir;
	}

	public ImageDownloadTask(String url, ImageView imageView) {
		this(url, imageView, null);
	}

	public ImageDownloadTask(String url, ImageView imageView, File cacheDir) {
		this.mUrl = url;
		DisplayMetrics displayMetrics = new DisplayMetrics();
		this.mWidth = (int) (imageView.getWidth() * displayMetrics.scaledDensity);
		this.mHeight = (int) (imageView.getHeight() * displayMetrics.scaledDensity);
		this.mImageView = imageView;
		this.mCacheDir = cacheDir;
	}

	public String getUrl() {
		return mUrl;
	}

	@Override
	protected Bitmap doInBackground(Void... params) {
		return doInBackground();
	}

	public Bitmap doInBackground() {
		final File image;
		final File lock;
		if (mCacheDir != null) {
			image = new File(mCacheDir, FileUtil.urlToFileHash(mUrl));
			lock = new File(mCacheDir, FileUtil.urlToFileHash(mUrl) + ".lock");
			if (!image.exists()) {
				try {
					image.createNewFile();
					lock.createNewFile();
				} catch (IOException e) {
				}
			} else {
				while (lock.exists()) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						return null;
					}
					if (isCancelled())
						return null;
				}
				Bitmap bitmap = BitmapFactory.decodeFile(image.getPath());
				if (bitmap == null) {
					return null;
				}
				int width = bitmap.getWidth();
				int height = bitmap.getHeight();

				Matrix matrix = new Matrix();
				matrix.postScale((float) 72.0 / width, (float) 72.0 / height);
				bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, false);

				return bitmap;
			}
		} else {
			image = null;
			lock = null;
		}

		final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		if (download(mUrl, outputStream)) {
			byte[] bytes = outputStream.toByteArray();
			Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
			if (bitmap == null) {
				return null;
			}

			int width = bitmap.getWidth();
			int height = bitmap.getHeight();

			if (mWidth > 0 && mHeight > 0) {
				Matrix matrix = new Matrix();
				matrix.postScale((float) mWidth / width, (float) mHeight / height);
				bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, false);
			}

			if (mCacheDir != null) {
				if (lock != null)
					lock.delete();
				VoidAsyncTask.executeRunnable(new Runnable() {
					@Override
					public void run() {
						try {
							image.createNewFile();
							FileOutputStream fileOutputStream = new FileOutputStream(image);
							try {
								outputStream.writeTo(fileOutputStream);
							} finally {
								fileOutputStream.close();
							}
						} catch (IOException e) {
						}
					}
				});
			}
			return bitmap;
		}
		return null;
	}

	@Override
	protected void onPostExecute(Bitmap result) {
		super.onPostExecute(result);
		if (mImageView != null) {
			mImageView.setImageBitmap(result);
		}
	}
}
