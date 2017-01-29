package com.downloadtool.arielmoreno.downloadtool;

import android.os.AsyncTask;
import android.support.v4.util.LruCache;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Created by Ariel.Moreno on 26/01/2017.
 */

/**
 * This class lets you use a json taht contains a distributed content. It has a LRU cache to increase performance on download of most frecuently used items
 */
public class DistributedContentDownloader {

    ArrayList<String> distributedContent;
    LruCache<String, byte[]> cache;
    ArrayList<FileDownloader> workingDownloads;

    /**
     * Constructor for the Distributed ContentDownloader
     *
     * @param sizeMB size in mega bytes for the cache
     */
    public DistributedContentDownloader(int sizeMB) {

        distributedContent = new ArrayList<String>();
        workingDownloads = new ArrayList<FileDownloader>();
        cache = new LruCache<String, byte[]>(sizeMB);
    }

    /**
     * This method is used to register a link that has a JSON mapping distributed resources
     *
     * @param jsonLink link to the json file
     * @param path     path to find the keys in the json array
     * @return number of json objects in the array
     */
    public int SubscribeDistributedContent(String jsonLink, String[] path) {
        FileDownloader fd = new FileDownloader(null, 40 * 1024 * 1024);
        try {
            fd.execute(jsonLink).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        new JSONAdapterAction(path).PerformOperation(fd.GetData());

        return distributedContent.size();
    }

    /**
     * This method is used to download a range of resources
     *
     * @param positionA starting position
     * @param positionB ending position
     * @param adapter   the adapter will define how to convert hte byte[] that is going to be downloaded and to define operations on it
     */
    public void DownloadFilesRange(int positionA, int positionB, ActionDataAdapter adapter) {
        if (positionA < 0 || positionB >= distributedContent.size() || positionA > positionB) {
            throw new IllegalArgumentException("Invalid range.");
        }

        for (int i = positionA; i <= positionB; i++) {
            boolean exists = false;
            synchronized (cache) {
                byte[] byteData = cache.get(distributedContent.get(i));
                exists = byteData != null;
                if (exists) {
                    adapter.PerformOperation(byteData);
                }
            }
            if (!exists) {
                FileDownloader fd = new FileDownloader(new StoreDownloaderAdapterAction(adapter, distributedContent.get(i)), 1 * 1024 * 1024);
                fd.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, distributedContent.get(i));
                System.out.print("execute" + distributedContent.get(i));
                workingDownloads.add(fd);
            }
        }

        for (int i = 0; i < workingDownloads.size(); i++) {
            if (workingDownloads.get(i).getStatus() == AsyncTask.Status.FINISHED) {
                workingDownloads.remove(i);
            }
        }
    }

    /**
     * This method is used to cancel a range of downloads
     *
     * @param positionA starint position
     * @param positionB ending position
     */
    public void CancelFilesRangeDownload(int positionA, int positionB) {
        if (positionA < 0 || positionB >= distributedContent.size() || positionA > positionB) {
            throw new IllegalArgumentException("Invalid range.");
        }

        for (int i = positionA; i <= positionB; i++) {
            if (workingDownloads.get(i).getStatus() == AsyncTask.Status.RUNNING || workingDownloads.get(i).getStatus() == AsyncTask.Status.PENDING) {
                workingDownloads.get(i).cancel(true);
            }
        }
    }

    /**
     * This is an internal class used to convert the byte[] into a json array and to store the links that it contains
     */
    class JSONAdapterAction implements ActionDataAdapter {

        ArrayList<String> links;
        String[] path;

        /**
         * Constructor for hte JSONAdapterAction class
         *
         * @param path this is the path to the desired key into the json objects
         */
        public JSONAdapterAction(String[] path) {
            this.path = path;
            links = new ArrayList<String>();
        }

        /**
         * This method read the desired key on each json object of the array
         *
         * @param data this is going to be the downloaded data for the json array
         */
        @Override
        public void PerformOperation(byte[] data) {
            try {
                JSONArray ja = new JSONArray(new String(data));

                for (int i = 0; i < ja.length(); i++) {
                    JSONObject json = (JSONObject) ja.get(i);

                    for (int j = 0; j < path.length - 1; j++) {
                        json = json.getJSONObject(path[j]);
                    }

                    String currentLink = json.getString(path[path.length - 1]);
                    links.add(currentLink);
                }

                distributedContent = links;
            } catch (JSONException e) {
                System.out.print(e.getMessage());
            }
        }

        public ArrayList<String> GetLinks() {
            return links;
        }
    }

    /**
     * This class is used as adapter for the range of resources downloaded
     */
    class StoreDownloaderAdapterAction implements ActionDataAdapter {
        ActionDataAdapter client;
        String link;

        /**
         * @param adapter this is the client adapter passed for the client at the DownloadFilesRange call
         * @param link    link of the downloaded resource, to be used to store data on the cache
         */
        public StoreDownloaderAdapterAction(ActionDataAdapter adapter, String link) {
            client = adapter;
            this.link = link;
        }

        /**
         * Data is saved on the cache and performed client operation on the data
         *
         * @param data data downloaded
         */
        @Override
        public void PerformOperation(byte[] data) {
            synchronized (cache) {
                cache.put(link, data);
            }
            client.PerformOperation(data);
        }
    }
}
