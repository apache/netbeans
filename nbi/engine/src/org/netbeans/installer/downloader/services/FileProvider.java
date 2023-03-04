/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.installer.downloader.services;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.installer.downloader.DownloadListener;
import org.netbeans.installer.downloader.DownloadManager;
import org.netbeans.installer.downloader.Pumping;
import org.netbeans.installer.downloader.Pumping.State;
import org.netbeans.installer.utils.exceptions.DownloadException;
/**
 *
 * @author Danila_Dugurov
 */
//todo: may be very general synchronization - optimize!
public class FileProvider {
  
  /////////////////////////////////////////////////////////////////////////////////
  // Static
  private static final FileProvider fileProvider = new FileProvider();
  
  public static FileProvider getProvider() {
    return fileProvider;
  }
  
  /////////////////////////////////////////////////////////////////////////////////
  // Instance
  private final DownloadManager downloadManager = DownloadManager.instance;
  
  private final DownloadListener listener = new MyListener();
  
  private final PersistentCache cache = new PersistentCache();
  
  private final Map<URL, State> scheduledURL2State = new HashMap<URL, State>();
  
  protected FileProvider() {
    downloadManager.registerListener(listener);
  }
  
  public synchronized void clearCaches() {
    for (URL url: cache.keys()) {
      cache.delete(url);
    }
  }
  
  public synchronized boolean isInCache(URL url) {
    return cache.isIn(url);
  }
  
  public synchronized void asynchDownload(URL url, File folder) {
    if (isInCache(url)) return;
    if (scheduledURL2State.containsKey(url)) return;
    if (!downloadManager.isActive()) downloadManager.invoke();
    scheduledURL2State.put(url, State.NOT_PROCESSED);
    downloadManager.queue().add(url, folder != null ? folder: downloadManager.defaultFolder());
  }
  
  public synchronized File get(URL url) throws DownloadException {
    return get(url, null, true);
  }
  
  public synchronized File get(URL url, File folder) throws DownloadException {
    return get(url, folder, true);
  }
  
  public synchronized File get(URL url, boolean useCache) throws DownloadException {
    return get(url, null, useCache);
  }
  
  public synchronized File get(URL url, File folder, boolean useCache) throws DownloadException {
    while (true) {
      final File file = tryGet(url);
      if (file != null) {
        if (useCache) return file;
        cache.delete(url);
        useCache = true;
      }
      synchronized (url) {
        asynchDownload(url, folder);
        try {
          url.wait();
        } catch (InterruptedException interrupt) {
          throw new DownloadException("download faild " + url, interrupt);
        }
      }
      switch(scheduledURL2State.get(url)) {
        case FAILED: {
          scheduledURL2State.remove(url);
          throw new DownloadException("download faild " + url);
        }
        case DELETED: {
          scheduledURL2State.remove(url);
          throw new DownloadException("download faild - externaly deleted " + url);
        }
        case FINISHED: scheduledURL2State.remove(url);
      }
    }
  }
  
  public synchronized File tryGet(URL url) {
    if (cache.isIn(url)) return cache.getByURL(url);
    return null;
  }
  
  public synchronized void manuallyDelete(URL url) {
    downloadManager.queue().delete(url);
    cache.delete(url);
  }
  
  /////////////////////////////////////////////////////////////////////////////////
  // Inner Classes
  private class MyListener extends EmptyQueueListener {
    public void pumpingStateChange(String id) {
      final Pumping pumping = downloadManager.queue().getById(id);
      final URL url = pumping.declaredURL();
      scheduledURL2State.put(url, pumping.state());
      switch(pumping.state()) {
        case FINISHED:
          cache.put(url, pumping.outputFile());
        case DELETED:
        case FAILED:
          synchronized(url) {
            url.notifyAll();
          }
      }
    }
  }
}
