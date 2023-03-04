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
package org.netbeans.modules.parsing.impl.indexing;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.BaseUtilities;

/**
 * Cache for {@link URLMapper#findFileObject} for source roots.
 * @author Tomas Zezula
 * @author Vita Stejskal
 */
public final class URLCache {

    private static final Logger LOG = Logger.getLogger(URLCache.class.getName());

    public static synchronized URLCache getInstance() {
        if (instance == null) {
            instance = new URLCache();
        }
        return instance;
    }

    @CheckForNull
    public FileObject findFileObject(
            final @NonNull URL url,
            final boolean validate) {
        URI uri = null;
        try {
            uri  = url.toURI();
        } catch (URISyntaxException e) {
            Exceptions.printStackTrace(e);
        }
        FileObject f = null;
        if (uri != null) {
            Reference<FileObject> ref = cache.get(uri);
            if (ref != null) {
                f = ref.get();
            }
            if (f != null && f.isValid() && (!validate || f.toURI().equals(uri))) {
                if (LOG.isLoggable(Level.FINEST)) {
                    LOG.log(
                        Level.FINEST,
                        "Found: {0} in cache for: {1}", //NOI18N
                        new Object[]{
                            f,
                            url
                        });
                }
                return f;
            }
        }

        f = URLMapper.findFileObject(url);

        if (uri != null && f != null && f.isValid()) {
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.log(
                   Level.FINEST,
                   "Caching: {0} for: {1}", //NOI18N
                   new Object[]{
                       f,
                       url
                   });
            }
            cache.put(uri, new CleanReference(f,uri));
        }

        return f;
    }

    private static URLCache instance = null;
    private final Map<URI, Reference<FileObject>> cache = Collections.synchronizedMap(
            new HashMap<URI, Reference<FileObject>>());

    private URLCache() {
    }
    
    private final class CleanReference extends WeakReference<FileObject> implements Runnable {        
        
        private final URI uri;
        
        CleanReference(
                @NonNull FileObject referent,
                @NonNull URI uri) {            
            super(referent, BaseUtilities.activeReferenceQueue());
            assert referent != null;
            assert uri != null;
            this.uri = uri;
        }

        @Override
        public void run() {
            cache.remove(uri);
        }
    }
}
