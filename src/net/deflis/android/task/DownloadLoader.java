package net.deflis.android.task;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

public abstract class DownloadLoader<T> extends AsyncTaskLoader<T> {

	protected static final int CONNECTION_TIMEOUT = 5000;
	protected static final int SOCKET_TIMEOUT = 5000;
	protected static final int BUFFER_SIZE = 100000;
	private static final int RETRY_MAX = 5;
	private StatusLine mStatusLine = null;
	private Header[] mHeaders;
	private Long mContentLength;
	private String mContentType;

	protected DownloadLoader(Context context) {
		super(context);
	}

	protected HttpClient getClient() {
		HttpClient httpClient = new DefaultHttpClient();
		HttpParams httpParams = httpClient.getParams();

		HttpConnectionParams.setConnectionTimeout(httpParams, CONNECTION_TIMEOUT);
		HttpConnectionParams.setSoTimeout(httpParams, SOCKET_TIMEOUT);

		return httpClient;
	}

	protected boolean download(String url, OutputStream outputStream) {
		return download(new HttpGet(url), new SizeCountOutputStream(outputStream), 0, 0);
	}

	protected boolean download(HttpUriRequest request, SizeCountOutputStream outputStream, long downloadedBytes, int retry) {
		if (downloadedBytes > 0)
			request.setHeader("Range", "bytes=" + downloadedBytes + "-");

		try {
			return download(request, outputStream);
		} catch (IOException e) {
			if (retry + 1 < RETRY_MAX) {
				return download(request, outputStream, outputStream.getSize(), retry + 1);
			} else {
				return false;
			}
		}
	}

	protected boolean download(HttpUriRequest request, OutputStream outputStream) throws IOException {
		mStatusLine = null;
		mHeaders = null;
		mContentLength = -1L;
		mContentType = null;

		HttpResponse response = getClient().execute(request);
		mStatusLine = response.getStatusLine();
		mHeaders = response.getAllHeaders();
		if (mStatusLine.getStatusCode() == 200 || mStatusLine.getStatusCode() == 206) {
			HttpEntity entity = new BufferedHttpEntity(response.getEntity());
			mContentLength = entity.getContentLength();
			mContentType = entity.getContentType().getValue();

			BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
			try {
				entity.writeTo(bufferedOutputStream);
				return true;
			} finally {
				bufferedOutputStream.flush();
			}
		}
		return false;
	}

	protected StatusLine getLastRequestStatusLine() {
		return mStatusLine;
	}

	protected Header[] getLastRequestHeaders() {
		return mHeaders;
	}

	protected Long getLastRequestContentLength() {
		return mContentLength;
	}

	protected String getLastRequestContentType() {
		return mContentType;
	}
}
