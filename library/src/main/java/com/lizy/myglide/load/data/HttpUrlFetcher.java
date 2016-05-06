package com.lizy.myglide.load.data;

import android.text.TextUtils;
import android.util.Log;

import com.lizy.myglide.Priority;
import com.lizy.myglide.load.DataSource;
import com.lizy.myglide.load.HttpException;
import com.lizy.myglide.load.model.GlideUrl;
import com.lizy.myglide.util.ContentLengthInputStream;
import com.lizy.myglide.util.LogTime;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

/**
 * Created by lizy on 16-4-28.
 */
public class HttpUrlFetcher implements DataFetcher<InputStream> {
    private static final String TAG = "HttpUrlFetcher";
    private static final int MAX_REDIRECTS = 3;
    private static final int DEFAULT_TIMEOUT_MS = 2500;

    static final HttpUrlConnectionFactory DEFAULT_CONNECTION_FACTORY =
            new DefaultHttpUrlConnectionFactory();

    private final GlideUrl glideUrl;
    private final HttpUrlConnectionFactory connectionFactory;
    private final int timeout;

    private HttpURLConnection urlConnection;
    private InputStream inputStream;
    private volatile boolean isCanceled;

    public HttpUrlFetcher(GlideUrl url) {
        this(url, DEFAULT_TIMEOUT_MS, DEFAULT_CONNECTION_FACTORY);
    }

    HttpUrlFetcher(GlideUrl glideUrl, int timeout, HttpUrlConnectionFactory factory) {
        this.glideUrl = glideUrl;
        this.timeout = timeout;
        this.connectionFactory = factory;
    }

    @Override
    public void loadData(Priority priority, DataCallback<? super InputStream> callback) {
        long startTime = LogTime.getLogTime();
        final InputStream result;
        try {
            result = loadDataWithRedirects(glideUrl.toURL(), 0, null, glideUrl.getHeaders());
        } catch (IOException e) {
            if(Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Failed to load data for url " + e);
            }
            callback.onLoadFailed(e);
            return;
        }

        if(Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "Finish http url fetch in " + LogTime.getElapsedMillis(startTime) + " ms");
        }
        callback.onDataReady(result);
    }

    private InputStream loadDataWithRedirects(URL url, int redirects,
                      URL lastUrl, Map<String, String> headers) throws IOException {
        if (redirects >= MAX_REDIRECTS) {
            throw new HttpException("Too many(>" + redirects + ") redirects!");
        } else {
            try {
                if (lastUrl != null && url.toURI().equals(lastUrl.toURI())) {
                    throw new HttpException("In re-direct loop");
                }
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }

        urlConnection = connectionFactory.build(url);
        for (Map.Entry<String, String> headerEntry : headers.entrySet()) {
            urlConnection.addRequestProperty(headerEntry.getKey(), headerEntry.getValue());
        }
        urlConnection.setConnectTimeout(timeout);
        urlConnection.setReadTimeout(timeout);
        urlConnection.setUseCaches(false);
        urlConnection.setDoInput(true);

        urlConnection.connect();
        if (isCanceled) {
            return null;
        }

        final int statusCode = urlConnection.getResponseCode();
        if (statusCode / 100 == 2) {
            return getStreamForSuccessfulRequest();
        } else if (statusCode / 100 == 3) {
            String redirectUrlString = urlConnection.getHeaderField("Location");
            if (TextUtils.isEmpty(redirectUrlString)) {
                throw new HttpException("redirects empty or null");
            }
            URL redirectUrl = new URL(url, redirectUrlString);
            return loadDataWithRedirects(redirectUrl, redirects + 1, url, headers);
        } else {
            throw new HttpException(urlConnection.getResponseMessage(), statusCode);
        }
    }

    private InputStream getStreamForSuccessfulRequest() throws IOException {
        if (TextUtils.isEmpty(urlConnection.getContentEncoding())) {
            inputStream = ContentLengthInputStream.obtain(urlConnection.getInputStream(),
                    urlConnection.getContentLength());
        } else {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Got non empty content encoding: " + urlConnection.getContentEncoding());
            }
            inputStream = urlConnection.getInputStream();
        }
        return inputStream;
    }

    @Override
    public void cleanup() {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                // Ignore
            }
        }
        if (urlConnection != null) {
            urlConnection.disconnect();
        }
    }

    @Override
    public void cancel() {
        isCanceled = true;
    }

    @Override
    public Class<InputStream> getDataClass() {
        return InputStream.class;
    }

    @Override
    public DataSource getDataSource() {
        return DataSource.REMOTE;
    }

    interface HttpUrlConnectionFactory {
        HttpURLConnection build(URL url) throws IOException;
    }

    private static class DefaultHttpUrlConnectionFactory implements HttpUrlConnectionFactory {

        @Override
        public HttpURLConnection build(URL url) throws IOException {
            return (HttpURLConnection) url.openConnection();
        }
    }

}
