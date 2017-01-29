package com.downloadtool.arielmoreno.downloadtool;

import android.os.AsyncTask;

import java.util.HashMap;

/**
 * Created by Ariel on 1/28/2017.
 */

/**
 * Class that has functionallity to download and cache downloaded data
 */
public class SingleFileDownloader {

    Long ticket = new Long(0);
    HashMap<Long, FileDownloader> workingDownloads = new HashMap<Long, FileDownloader>();
    FileDownloader fd;
    int size;

    /**
     * Constructor
     *
     * @param sizeMB  size in MB for the cache
     * @param adapter client action on downloaded data
     */
    public SingleFileDownloader(int sizeMB, ActionDataAdapter adapter) {
        size = sizeMB;
        fd = new FileDownloader(adapter, sizeMB);
    }

    /**
     * Method to download data
     *
     * @param link ling to resource
     * @return id for the download, for later use on a cancel operation
     */
    public Long DownloadFile(String link) {
        fd.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, link);
        Long currentTicket = new Long(0);

        synchronized (ticket) {
            currentTicket = ticket;
            ticket++;
        }

        synchronized (workingDownloads) {
            Long[] set = (Long[]) workingDownloads.keySet().toArray();
            for (int i = 0; i < set.length; i++) {
                if (workingDownloads.get(set[i]).getStatus() == AsyncTask.Status.FINISHED) {
                    workingDownloads.remove(set[i]);
                }
            }
        }

        workingDownloads.put(currentTicket, fd);
        return currentTicket;
    }

    /**
     * Method to cancel download
     *
     * @param ticket
     */
    public void CancelDownload(Long ticket) {
        if (workingDownloads.containsKey(ticket) && workingDownloads.get(ticket).getStatus() != AsyncTask.Status.FINISHED) {
            workingDownloads.get(ticket).cancel(true);
        }
    }
}
