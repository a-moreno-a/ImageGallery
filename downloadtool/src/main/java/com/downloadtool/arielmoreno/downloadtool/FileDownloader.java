package com.downloadtool.arielmoreno.downloadtool;

import android.os.AsyncTask;
import android.support.v4.util.LruCache;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Ariel.Moreno on 26/01/2017.
 */

/**
 * Class that extends AsyncTask, used to perform async downloads
 */
public class FileDownloader extends AsyncTask<String, String, byte[]> {

    byte[] data;
    ActionDataAdapter adapter;
    LruCache<String, byte[]> cache;

    /**
     * @param adapter client action to be peformed on downloaded data
     * @param sizeMB  size for cache
     */
    public FileDownloader(ActionDataAdapter adapter, int sizeMB) {
        int cacheSize = sizeMB;
        this.adapter = adapter;
        this.cache = new LruCache<String, byte[]>(cacheSize);
    }

    /**
     * Methode where the data is downloaded
     *
     * @param f_url
     * @return
     */
    @Override
    protected byte[] doInBackground(String... f_url) {
        int count;
        byte[] byteData = null;
        Boolean exists = false;
        synchronized (cache) {
            byteData = cache.get(f_url[0]);
            exists = byteData != null;
        }

        if (!exists) {
            try {
                URL url = new URL(f_url[0]);

                URLConnection urlConnection = url.openConnection();
                urlConnection.connect();
                int file_size = urlConnection.getContentLength();

                // input stream to read file - with 8k buffer
                InputStream input = new BufferedInputStream(url.openStream(), 8192);

                // Output stream to write file

                ByteArrayOutputStream output = new ByteArrayOutputStream();
                byte data[] = new byte[1024];

                long total = 0;
                while ((count = input.read(data)) != -1) {
                    total += count;

                    // writing data to file
                    output.write(data, 0, count);
                }

                byteData = output.toByteArray();

                // closing streams
                output.close();
                input.close();
                synchronized (cache) {
                    cache.put(f_url[0], byteData);
                    this.data = byteData;
                }

            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

        return byteData;
    }

    /**
     * Here the client action is performed on the downloaded data
     *
     * @param data
     */
    @Override
    protected void onPostExecute(byte[] data) {
        this.data = data;
        if (this.adapter != null) {
            this.adapter.PerformOperation(data);
        }
    }

    public byte[] GetData() {
        return this.data;
    }

}
