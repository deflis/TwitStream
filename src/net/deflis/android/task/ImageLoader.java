package net.deflis.android.task;

import java.io.ByteArrayOutputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.support.v4.app.LoaderManager;

public class ImageLoader extends DownloadLoader<Bitmap> {
	private final String mUrl;
	private final int mWidth;
	private final int mHeight;

	public ImageLoader(Context context, String url) {
		this(context, url, -1, -1);
	}

	public ImageLoader(Context context, String url, int width, int height) {
		super(context);
		this.mUrl = url;
		this.mWidth = width;
		this.mHeight = height;
	}

	@Override
	public Bitmap loadInBackground() {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		if (download(mUrl, outputStream)) {
			byte[] bytes = outputStream.toByteArray();
			Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

			int width = bitmap.getWidth();
			int height = bitmap.getHeight();

			if (mWidth > 0 && mHeight > 0) {
				Matrix matrix = new Matrix();
				matrix.postScale((float) mWidth / width, (float) mHeight / height);
				bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, false);
			}
			return bitmap;
		}
		return null;
	}

	public interface LoaderCallbacks extends LoaderManager.LoaderCallbacks<Bitmap> {
	}
}
