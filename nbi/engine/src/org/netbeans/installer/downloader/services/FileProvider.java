/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
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
