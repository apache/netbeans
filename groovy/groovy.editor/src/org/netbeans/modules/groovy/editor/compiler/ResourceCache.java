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
package org.netbeans.modules.groovy.editor.compiler;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Enumerations;
import org.openide.util.Pair;

/**
 * Simple resource cache. Stores URLs of resources already asked for, caches folder contents for
 * faster exists/does not exist lookups.
 * 
 * @author sdedic
 */
public class ResourceCache {
    private static final Logger LOG = Logger.getLogger(ResourceCache.class.getName());
    
    private static final int DEFAULT_NON_EXISTENT_CACHE_SIZE = 10000;

    private static final int NON_EXISTENT_CACHE_SIZE = Integer.getInteger(
            "groovy.editor.ClassNodeCache.nonExistent.size",  // NOI18N
            DEFAULT_NON_EXISTENT_CACHE_SIZE);

    /**
     * Indicates the resource is just one, there are not multiple resources. Will be
     * replaced by URL in the cache on first reference, so next accesses do not need to
     * use URLMappers. Note: this tag value is compared by identity
     */
    private static final URL PLACEHOLDER; 

    /**
     * Multiple resources of the same name; use slow method. Note: this tag value is compared by identity
     */
    private static final URL MULTIPLE; 
    
    /**
     * Classpath where to load the resources from.
     */
    private final ClassPath path;

    /**
     * Map of folder contents. Indexed by folder path, values are file => URL/placeholder
     */
    private final Map<String, Map<String, URL>> folderContents = new HashMap<>();
    
    /**
     * Cached entries that are known not to exist.
     */
    private final Map<CharSequence,Void> nonExistent = new LinkedHashMap<CharSequence, Void>(16,0.75f,true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<CharSequence, Void> eldest) {
            if (size() > NON_EXISTENT_CACHE_SIZE) {
                LOG.log(
                    Level.FINE,
                    "Non existent cache full, removing : {0}",
                    eldest.getKey());
                return true;
            }
            return false;
        }
    };

    static {
        try {
            PLACEHOLDER = new URL("file:///"); // NOI18N
            MULTIPLE = new URL("file:///"); // NOI18N
        } catch (IOException ex) {
            throw new IllegalStateException();
        }
    }
    
    public ResourceCache(ClassPath path) {
        this.path = path;
    }

    public ClassPath getPath() {
        return path;
    }
    
    protected boolean isNonExistentResource(String name) {
        return nonExistent.containsKey(name);
    }

    protected boolean isNonExistent(String name) {
        return false;
    }
    
    protected void addNonExistentResource(String name) {
        nonExistent.putIfAbsent(name, null);
    }

    /**
     * Will load folder from {@link #path} and return its contents. The 1st pair element
     * is the filename, the second is a Map of folder's contents.
     * @param resourceName full resource name to search for
     * @return filename and folder contents.
     */
    private Pair<String, Map<String, URL>> loadFolder(String resourceName) {
        int lastSlash = resourceName.lastIndexOf('/'); // NOI18N
        String folderName = lastSlash == -1 ? "" : resourceName.substring(0, lastSlash);  // NOI18N
        Map<String, URL> contents = folderContents.get(folderName);
        String rest = resourceName.substring(lastSlash + 1);
        if (isNonExistentResource(folderName)) {
            return Pair.of(rest, Collections.emptyMap());
        }
        if (contents != null) {
            return Pair.of(rest, contents);
        }
        Map<String, URL> lhm = new LinkedHashMap<>();
        boolean empty = true;
        for (FileObject parent: path.findAllResources(folderName)) {
            for (FileObject f : parent.getChildren()) {
                if (lhm.putIfAbsent(f.getNameExt(), PLACEHOLDER) != null) {
                    lhm.put(f.getNameExt(), MULTIPLE);
                }
                empty = false;
            }
        }
        folderContents.put(folderName, lhm);
        if (empty) {
            addNonExistentResource(folderName);
        }
        return Pair.of(rest, lhm);
    }

    public Enumeration<URL> getResources(String name) throws IOException {
        if (isNonExistentResource(name)) {
            return Enumerations.empty();
        }
        Pair<String, Map<String, URL>> fl = loadFolder(name);
        URL res = fl.second().get(fl.first());
        if (res == null) {
            return Collections.emptyEnumeration();
        } else if (res == MULTIPLE) {
            List<FileObject> l = path.findAllResources(name);
            List<URL> converted = new ArrayList<>(l.size());
            for (FileObject f : l) {
                URL u = URLMapper.findURL(f, URLMapper.INTERNAL);
                if (u != null) {
                    converted.add(u);
                }
            }
            return Collections.enumeration(converted);
        } else if (res == PLACEHOLDER) {
            List<FileObject> l = path.findAllResources(name);
            if (l.isEmpty()) {
                addNonExistentResource(name);
                return null;
            }
            res = URLMapper.findURL(l.get(0), URLMapper.INTERNAL);
            if (res == null) {
                addNonExistentResource(name);
                return null;
            }
            fl.second().put(fl.first(), res);
        }
        return Enumerations.singleton(res);
    }

    private URL doGetResource(String name) {
        Pair<String, Map<String, URL>> fl = loadFolder(name);
        URL res = fl.second().get(fl.first());
        if (res == null) {
            return null;
        }

        if (res == MULTIPLE) {
            FileObject f = path.findResource(name);
            return URLMapper.findURL(f, URLMapper.INTERNAL);
        } else if (res == PLACEHOLDER) {
            FileObject f = path.findResource(name);
            res = URLMapper.findURL(f, URLMapper.INTERNAL);
            fl.second().put(fl.first(), res);
            return res;
        } else {
            return res;
        }
    }

    public URL getResource(String name) {
        if (isNonExistentResource(name)) {
            return null;
        }
        URL u = doGetResource(name);
        return u;
    }
}
