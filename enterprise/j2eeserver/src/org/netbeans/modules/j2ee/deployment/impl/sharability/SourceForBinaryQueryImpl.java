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

package org.netbeans.modules.j2ee.deployment.impl.sharability;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation2;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.ChangeSupport;
import org.openide.util.WeakListeners;

/**
 * Finds the locations of sources for various libraries.
 * @author Tomas Zezula
 */
@org.netbeans.api.annotations.common.SuppressWarnings(value="DMI_COLLECTION_OF_URLS", justification="File URLs only")
//@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation.class)
public class SourceForBinaryQueryImpl implements SourceForBinaryQueryImplementation2 {

    private static final String[] CLASSPATH_VOLUMES = new String[] {
        ServerLibraryTypeProvider.VOLUME_CLASSPATH,
        ServerLibraryTypeProvider.VOLUME_WS_COMPILE_CLASSPATH
    };
    
    private final Map<URL,SourceForBinaryQueryImplementation2.Result> cache = new ConcurrentHashMap<URL,SourceForBinaryQueryImplementation2.Result>();
    private final Map<URL,URL> normalizedURLCache = new ConcurrentHashMap<URL,URL>();

    /** Default constructor for lookup. */
    public SourceForBinaryQueryImpl() {}

    @org.netbeans.api.annotations.common.SuppressWarnings(value="DMI_BLOCKING_METHODS_ON_URL", justification="File URLs only")
    public SourceForBinaryQueryImplementation2.Result findSourceRoots2(URL binaryRoot) {
        SourceForBinaryQueryImplementation2.Result res = cache.get(binaryRoot);
        if (res != null) {
            return res;
        }
        boolean isNormalizedURL = isNormalizedURL(binaryRoot);
        for (LibraryManager mgr : LibraryManager.getOpenManagers()) {
            for (Library lib : mgr.getLibraries()) {
                if (lib.getType().equals(ServerLibraryTypeProvider.LIBRARY_TYPE)) {
                    for (String type : CLASSPATH_VOLUMES) {
                        for (URL entry : lib.getContent(type)) {
                            URL normalizedEntry = entry;
                            if (isNormalizedURL) {
                                normalizedEntry = getNormalizedURL(normalizedEntry);
                            }
                            if (binaryRoot.equals(normalizedEntry)) {
                                res = new Result(entry, lib);
                                cache.put(binaryRoot, res);
                                return res;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
    
    public SourceForBinaryQuery.Result findSourceRoots (final URL binaryRoot) {
        return this.findSourceRoots2(binaryRoot);
    }
    
    
    private URL getNormalizedURL (URL url) {
        //URL is already nornalized, return it
        if (isNormalizedURL(url)) {
            return url;
        }
        //Todo: Should listen on the LibrariesManager and cleanup cache        
        // in this case the search can use the cache onle and can be faster 
        // from O(n) to O(ln(n))
        URL normalizedURL = normalizedURLCache.get(url);
        if (normalizedURL == null) {
            FileObject fo = URLMapper.findFileObject(url);
            if (fo != null) {
                normalizedURL = fo.toURL();
                this.normalizedURLCache.put (url, normalizedURL);
            }
        }
        return normalizedURL;
    }
    
    /**
     * Returns true if the given URL is file based, it is already
     * resolved either into file URL or jar URL with file path.
     * @param URL url
     * @return true if  the URL is normal
     */
    private static boolean isNormalizedURL (URL url) {
        if ("jar".equals(url.getProtocol())) { //NOI18N
            url = FileUtil.getArchiveFile(url);
        }
        return "file".equals(url.getProtocol());    //NOI18N
    }
    
    
    private static class Result implements SourceForBinaryQueryImplementation2.Result, PropertyChangeListener {
        
        private Library lib;
        private URL entry;
        private final ChangeSupport cs = new ChangeSupport(this);
        private FileObject[] cache;
        
        public Result (URL queryFor, Library lib) {
            this.entry = queryFor;
            this.lib = lib;
            this.lib.addPropertyChangeListener(WeakListeners.propertyChange(this, this.lib));
        }
        
        public synchronized FileObject[] getRoots () {
            if (this.cache == null) {
                // entry is not resolved so directly volume content can be searched for it:
                boolean contains = false;
                for (String type : CLASSPATH_VOLUMES) {
                    if (this.lib.getContent(type).contains(entry)) {
                        contains = true;
                        break;
                    }
                }

                if (contains) {
                    List<FileObject> result = new ArrayList<FileObject>();
                    for (URL u : lib.getContent(ServerLibraryTypeProvider.VOLUME_SOURCE)) {
                        FileObject sourceRootURL = URLMapper.findFileObject(u);
                        if (sourceRootURL != null) {
                            result.add(sourceRootURL);
                        }
                    }
                    this.cache = result.toArray(new FileObject[0]);
                } else {
                    this.cache = new FileObject[0];
                }
            }
            return this.cache;
        }
        
        public synchronized void addChangeListener (ChangeListener l) {
            assert l != null : "Listener cannot be null"; // NOI18N
            cs.addChangeListener(l);
        }
        
        public synchronized void removeChangeListener (ChangeListener l) {
            assert l != null : "Listener cannot be null"; // NOI18N
            cs.removeChangeListener(l);
        }
        
        public void propertyChange (PropertyChangeEvent event) {
            if (Library.PROP_CONTENT.equals(event.getPropertyName())) {
                synchronized (this) {                    
                    this.cache = null;
                }
                cs.fireChange();
            }
        }

        public boolean preferSources() {
            return false;
        }
        
    }
    
}
