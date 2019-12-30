package com.itachi1706.appupdater.Util;

import android.util.Log;

import androidx.annotation.WorkerThread;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLException;

import static com.itachi1706.appupdater.Util.UpdaterHelper.HTTP_QUERY_TIMEOUT;

/**
 * Created by Kenneth on 24/7/2019.
 * for com.itachi1706.appupdater.Util in CheesecakeUtilities
 *
 * Standard class for helping out with HttpURLConnection or HttpsURLConnections
 * @migrated
 */
public class URLHelper {

    private static final int HTTP_CONN = 0, HTTPS_CONN = 1;

    private URL url;
    private int mode;
    private boolean fallbackHttp = true;
    private int timeout = -1;

    /**
     * Construct URLHelper with a String URL
     * @param url String of URL to parse
     */
    public URLHelper(String url) {
        try {
            this.url = new URL(url);
            this.mode = (this.url.getProtocol().equalsIgnoreCase("https")) ? HTTPS_CONN : HTTP_CONN;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Construct URLHelper with a URL object
     * @param url URL Object
     */
    public URLHelper(URL url) {
        this.url = url;
        this.mode = (url.getProtocol().equalsIgnoreCase("https")) ? HTTPS_CONN : HTTP_CONN;
    }

    /**
     * Enable this if you wish to fallback to HTTP if any HTTPS connection fails
     *
     * This is enabled by default
     * @param fallbackHttp true to fallback, false otherwise
     * @return object instance
     */
    public URLHelper setFallbackToHttp(boolean fallbackHttp) {
        this.fallbackHttp = fallbackHttp;
        return this;
    }

    /**
     * Sets a custom timeout value for the URL Connection
     *
     * Defaults to {@link UpdaterHelper#HTTP_QUERY_TIMEOUT}
     * @param timeout Custom timeout value in int
     * @return object instance
     */
    public URLHelper setTimeoutValues(int timeout) {
        this.timeout = timeout;
        return this;
    }

    /**
     * Executes the HTTP request and obtain a String from it
     *
     * This can only be accomplished on a worker thread as it is blocking
     * @return String of the request
     * @throws IOException If an error occurrs
     */
    @WorkerThread
    public String executeString() throws IOException {
        return (this.mode == HTTPS_CONN) ? processHttpsConnection() : processHttpConnection();
    }

    @WorkerThread
    private String processHttpConnection() throws IOException {
        HttpURLConnection conn = (HttpURLConnection) this.url.openConnection();
        conn.setConnectTimeout((this.timeout == -1) ? HTTP_QUERY_TIMEOUT : timeout);
        conn.setReadTimeout((this.timeout == -1) ? HTTP_QUERY_TIMEOUT : timeout);
        InputStream in = conn.getInputStream();

        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder str = new StringBuilder();
        String line;
        while((line = reader.readLine()) != null)
        {
            str.append(line);
        }
        in.close();
        return str.toString();
    }

    @WorkerThread
    private String processHttpsConnection() throws IOException {
        try {
        HttpsURLConnection conn = (HttpsURLConnection) this.url.openConnection();
        conn.setConnectTimeout((this.timeout == -1) ? HTTP_QUERY_TIMEOUT : timeout);
        conn.setReadTimeout((this.timeout == -1) ? HTTP_QUERY_TIMEOUT : timeout);
        conn.connect();

        // We will do a check for HTTPS error if there is a fallback support enabled
        if (fallbackHttp) if (conn.getResponseCode() >= 300) return doFallback(); // Fallback to HTTP
        InputStream in = conn.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder str = new StringBuilder();
        String line;
        while((line = reader.readLine()) != null)
        {
            str.append(line);
        }
        in.close();
        return str.toString();
        } catch (SSLException ignored) {
            Log.d("HttpsUrlConn", "SSLException has occurred!");
            if (fallbackHttp) return doFallback();
        }
        return "";
    }

    private String doFallback() throws IOException {
        Log.i("HttpsUrlConn", "Error detected in HTTPS Connection. Doing fallback to HTTP");
        this.url = new URL("http", this.url.getHost(), this.url.getPort(), this.url.getFile());
        return processHttpConnection();
    }
}
