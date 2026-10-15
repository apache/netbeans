/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.versioning.core;

import javax.swing.event.ChangeListener;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.event.ChangeEvent;
import org.netbeans.modules.versioning.core.util.VCSSystemProvider.VersioningSystem;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.spi.VCSVisibilityQuery;
import org.netbeans.spi.queries.VisibilityQueryChangeEvent;
import org.netbeans.spi.queries.VisibilityQueryImplementation2;
import org.openide.filesystems.FileObject;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 * Delegates the work to the owner of files in query.
 * 
 * @author Tomas Stupka
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.queries.VisibilityQueryImplementation.class)
public class VcsVisibilityQueryImplementation implements VisibilityQueryImplementation2 {

    private final InvisibleFiles cache = new InvisibleFiles(25);
    private List<ChangeListener> listeners = new ArrayList<ChangeListener>();
    private static VcsVisibilityQueryImplementation instance;
    private static RequestProcessor rp = new RequestProcessor(VcsVisibilityQueryImplementation.class.getName(), 1, false, false);
    private RequestProcessor.Task refreshTask = rp.create(new RefreshTask());
    private VisibilityChangedTask vsChangedTask = new VisibilityChangedTask();
    private final HashMap<VCSFileProxy, Boolean> refreshedFiles = new LinkedHashMap<VCSFileProxy, Boolean>(20);
    private static final int MAX_CACHE_SIZE = 500;

    private static final Logger LOG = Logger.getLogger(VcsVisibilityQueryImplementation.class.getName());
    
    public VcsVisibilityQueryImplementation() {
        instance = this;
    }

    public static VcsVisibilityQueryImplementation getInstance() {
        return instance;
    }

    public static void visibilityChanged() {
        if(instance != null) {
            // was touched from outside - lets fire the change
            instance.fireVisibilityChanged((FileObject[])null);
        }
    }
    
    public static void visibilityChanged(VCSFileProxy... files) {
        if(instance != null) {
            // was touched from outside - lets fire the change
            instance.fireVisibilityChanged(files);
        }
    }

    @Override
    public boolean isVisible(File file) {
        boolean ret = true;
        try {
            ret = isVisible(VCSFileProxy.createFileProxy(file));
            return ret;
        } finally {
            if((!ret && LOG.isLoggable(Level.FINER)) ||
                LOG.isLoggable(Level.FINEST)) 
            { 
                LOG.log(Level.FINE, "VCS returned visibility {0} for {1}", new Object[] {ret, file}); // NOI18N
            }
        }
    }
    
    public boolean isVisible(VCSFileProxy file) {
        VersioningSystem[] systems = VersioningManager.getInstance().getVersioningSystems();
        for (VersioningSystem versioningSystem : systems) {
            if(versioningSystem.isMetadataFile(file)) {
                return false;
            }
        }
        if(isHiddenMetadata(file)) {
            return false;
        }
        
        boolean visible;
        synchronized (cache) {
            cache.clearOldValues();
            visible = !cache.containsKey(file); // get cached value
        }
        boolean refresh;
        synchronized (refreshedFiles) {
            refresh = refreshedFiles.isEmpty();
            refreshedFiles.put(file, visible);
        }
        if (refresh) {
            refreshTask.schedule(100);
        }
        return visible;
    }

    @Override
    public boolean isVisible(FileObject fileObject) {
        boolean ret = true;
        try {
            VCSFileProxy file = VCSFileProxy.createFileProxy(fileObject);
            if(file == null) {
                return true;
            }
            ret = isVisible(file);
            return ret;
        } finally {
            if((!ret && LOG.isLoggable(Level.FINER)) ||
                LOG.isLoggable(Level.FINEST)) 
            { 
                LOG.log(Level.FINE, "VCS returned visibility {0} for {1}", new Object[] {ret, fileObject}); // NOI18N
            }
        }
    }

    @Override
    public synchronized void addChangeListener(ChangeListener l) {
        ArrayList<ChangeListener> newList = new ArrayList<ChangeListener>(listeners);
        newList.add(l);
        listeners = newList;
    }

    @Override
    public synchronized void removeChangeListener(ChangeListener l) {
        ArrayList<ChangeListener> newList = new ArrayList<ChangeListener>(listeners);
        newList.remove(l);
        listeners = newList;
    }

    private void fireVisibilityChanged(VCSFileProxy[] proxies) {
        Set<FileObject> fileObjects = new LinkedHashSet<FileObject>(proxies.length);
        for (VCSFileProxy proxy : proxies) {
            FileObject fo = findExistingFileObject(proxy);
            if (fo != null) {
                fileObjects.add(fo);
            } else {
                LOG.log(Level.WARNING, "VCS visibility did not fire because of {0} which returns no FileObject", proxy); // NOI18N
            }
        }
        fireVisibilityChanged(fileObjects.toArray(new FileObject[0]));
    }
    
    private void fireVisibilityChanged(FileObject[] files) {
        ChangeListener[] ls;
        synchronized(this) {
            ls = listeners.toArray(new ChangeListener[0]);
        }
        ChangeEvent event = files == null ? new ChangeEvent(this) : new VisibilityQueryChangeEvent(this, files);
        for (ChangeListener l : ls) {
            l.stateChanged(event);
        }
        if(files != null && LOG.isLoggable(Level.FINE)) { 
            StringBuilder sb = new StringBuilder();
            for (FileObject fo : files) {
                sb.append("\n\t");
                sb.append(fo.toString());
            }
            LOG.log(Level.FINE, "VCS fired visibility change for: {0}", sb.toString()); // NOI18N
        }
    }

    private static final Pattern hgmetadataPattern = Pattern.compile(".*\\" + File.separatorChar + "(\\.)hg(\\" + File.separatorChar + ".*|$)"); // NOI18N
    private static final Pattern cvsmetadataPattern = Pattern.compile(".*\\" + File.separatorChar + "CVS(\\" + File.separatorChar + ".*|$)"); // NOI18N     
    private static final Pattern gitmetadatapattern = Pattern.compile(".*\\" + File.separatorChar + "(\\.)git(\\" + File.separatorChar + ".*|$)"); // NOI18N
    
    // temporary hack to fix issue #195985
    // should be replaced by a change in VCSVisibilityQuery
    private boolean isHiddenMetadata(VCSFileProxy file) {
        return hgmetadataPattern.matcher(file.getPath()).matches()  ||
               cvsmetadataPattern.matcher(file.getPath()).matches() ||
               gitmetadatapattern.matcher(file.getPath()).matches();
    }

    private FileObject findExistingFileObject (VCSFileProxy proxy) {
        FileObject fo = null;
        while (proxy != null) {
            fo = proxy.toFileObject();
            if (fo != null) {
                break;
            }
            proxy = proxy.getParentFile();
        }
        return fo;
    }

    private class VisibilityChangedTask implements Runnable {
        
        private final List<VCSFileProxy> files = new LinkedList<VCSFileProxy>();
        private Task task;

        public VisibilityChangedTask() {}
        
        @Override
        public void run() {
            VCSFileProxy[] filesArray;
            synchronized(files) {
                filesArray = files.toArray(new VCSFileProxy[0]);
                files.clear();
            }
            fireVisibilityChanged(filesArray);            
        }
        
        void schedule(VCSFileProxy file) {
            synchronized(files) {
                files.add(file);
            }
            getTask().schedule(1000);
        }
        
        private synchronized Task getTask() {
            if(task == null) {
                task = rp.create(this);
            }
            return task;
        }
        
    }
    
    private class RefreshTask implements Runnable {
        @Override
        public void run() {
            VCSFileProxy file = null;
            boolean originalValue = false;
            // get another file
            synchronized (refreshedFiles) {
                Iterator<Entry<VCSFileProxy, Boolean>> it = refreshedFiles.entrySet().iterator();
                if (it.hasNext()) {
                    Entry<VCSFileProxy, Boolean> e = it.next();
                    file = e.getKey();
                    originalValue = e.getValue();
                    it.remove();
                }
            }
            if (file == null) {
                return; // no files to refresh, finish
            }
            boolean visible = true;
            VersioningSystem system = VersioningManager.getInstance().getOwner(file, !file.isDirectory());
            if (system != null) {
                VCSVisibilityQuery vqi = system.getVisibilityQuery();
                if(vqi == null) {
                    visible = true;
                } else {
                    visible = vqi.isVisible(file);
                }
            }
            synchronized (cache) {
                cache.remove(file);
                if (!visible) {
                    cache.put(file, System.currentTimeMillis());
                }
            }
            if (originalValue != visible) {
                vsChangedTask.schedule(file);
            }
            refreshTask.schedule(0);
        }
    }

    private static class InvisibleFiles extends LinkedHashMap<VCSFileProxy, Long> {

        public InvisibleFiles (int initialCapacity) {
            super(initialCapacity);
        }

        @Override
        protected boolean removeEldestEntry(Entry<VCSFileProxy, Long> eldest) {
            return size() >= MAX_CACHE_SIZE;
        }

        public void clearOldValues () {
            if (size() > MAX_CACHE_SIZE >> 2) { // remove old entries only for the size being 1/4 it's max capacity
                Iterator<Entry<VCSFileProxy, Long>> it = entrySet().iterator();
                long threshold = System.currentTimeMillis() - 30 * 60 * 1000; // default max age is 30 minutes
                while (it.hasNext() && it.next().getValue() < threshold) {
                    it.remove();
                }
            }
        }
    }
}
