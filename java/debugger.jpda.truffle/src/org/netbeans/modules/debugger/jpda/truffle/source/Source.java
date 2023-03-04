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

package org.netbeans.modules.debugger.jpda.truffle.source;

import com.sun.jdi.StringReference;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.StringReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;

import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 * Virtual guest language script source.
 */
public final class Source {
    
    public static final String URL_PROTOCOL = "truffle-scripts"; // NOI18N
    static final String ATTR_URI = "com.oracle.truffle InternalURI"; // NOI18N
    
    private static final Map<JPDADebugger, Map<Long, Source>> KNOWN_SOURCES = new WeakHashMap<>();

    private final StringReference codeRef;
    private final String name;
    private final String hostMethodName;
    private final String path;
    private final URI uri;          // The original source URI
    private final URL url;          // The source
    private final String mimeType;
    private final long hash;
    private String content;
    
    private Source(JPDADebugger jpda, String name, String hostMethodName, String path, URI uri, String mimeType, long hash, StringReference codeRef) {
        this.name = name;
        this.hostMethodName = hostMethodName;
        this.path = path;
        this.codeRef = codeRef;
        URL url = null;
        if (hostMethodName == null) {
            if (uri == null || !"file".equalsIgnoreCase(uri.getScheme()) || !fileIsReadable(uri)) { // NOI18N
                try {
                    url = SourceFilesCache.get(jpda).getSourceFile(name, hash, uri, getContent());
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else {
                uri = SourceBinaryTranslator.binary2Source(uri);
            }
            if (url == null) {
                try {
                    url = uri.toURL();
                } catch (MalformedURLException muex) {
                    Exceptions.printStackTrace(muex);
                }
            }
        }
        this.url = url;
        this.uri = uri;
        this.mimeType = mimeType;
        this.hash = hash;
    }

    private static boolean fileIsReadable(URI uri) {
        try {
            return new File(uri).canRead();
        } catch (IllegalArgumentException | SecurityException ex) {
            return false;
        }
    }

    public static Source getExistingSource(JPDADebugger debugger, long id) {
        synchronized (KNOWN_SOURCES) {
            Map<Long, Source> dbgSources = KNOWN_SOURCES.get(debugger);
            if (dbgSources != null) {
                Source src = dbgSources.get(id);
                if (src != null) {
                    return src;
                }
            }
        }
        return null;
    }
    
    /**
     * Find an existing Source instance for the given FileObject.
     * Currently, this method returns sources only for non-file sources.
     * @param fo
     * @return a Source, or <code>null</code>.
     *
    public static Source get(JPDADebugger debugger, FileObject fo) {
        URI uri = fo.toURI();
        if (!URL_PROTOCOL.equals(uri.getScheme())) {
            return null;
        }
        String path = uri.getPath();
        int hashEnd = path.indexOf('/');
        String hashStr = path.substring(0, hashEnd);
        long id = Long.parseUnsignedLong(hashStr, 16);
        return getExistingSource(debugger, id);
    }*/
    
    public static URI getTruffleInternalURI(FileObject fo) {
        return (URI) fo.getAttribute(ATTR_URI);
    }
    
    public static Source getSource(JPDADebugger debugger, long id,
                                   String name,
                                   String path,
                                   URI uri,
                                   StringReference codeRef) {
        return getSource(debugger, id, name, path, uri, null, codeRef);
    }

    public static Source getSource(JPDADebugger debugger, long id,
                                   String name,
                                   String path,
                                   URI uri,
                                   String mimeType,
                                   StringReference codeRef) {
        return getSource(debugger, id, name, null, path, uri, null, codeRef);
    }

    public static Source getSource(JPDADebugger debugger, long id,
                                   String name,
                                   String hostMethodName,
                                   String path,
                                   URI uri,
                                   String mimeType,
                                   StringReference codeRef) {
        if (uri == null && codeRef == null) {
            return null;
        }
        synchronized (KNOWN_SOURCES) {
            Map<Long, Source> dbgSources = KNOWN_SOURCES.get(debugger);
            if (dbgSources != null) {
                Source src = dbgSources.get(id);
                if (src != null) {
                    return src;
                }
            }
        }
        return getTheSource(debugger, id, name, hostMethodName, path, uri, mimeType, codeRef);
    }
    
    private static Source getTheSource(JPDADebugger debugger, long id,
                                       String name,
                                       String hostMethodName,
                                       String path,
                                       URI uri,
                                       String mimeType,
                                       StringReference codeRef) {
        
        Source src = new Source(debugger, name, hostMethodName, path, uri, mimeType, id, codeRef);
        if (id >= 0) {
            synchronized (KNOWN_SOURCES) {
                Map<Long, Source> dbgSources = KNOWN_SOURCES.get(debugger);
                if (dbgSources == null) {
                    dbgSources = new HashMap<>();
                    KNOWN_SOURCES.put(debugger, dbgSources);
                }
                dbgSources.put(id, src);
            }
        }
        return src;
    }
    
    public String getName() {
        return name;
    }

    public String getHostMethodName() {
        return hostMethodName;
    }

    public String getPath() {
        return path;
    }

    public URL getUrl() {
        return url;
    }
    
    public URI getURI() {
        return uri;
    }
    
    public String getMimeType() {
        return mimeType;
    }
    
    public long getHash() {
        return hash;
    }

    public String getContent() {
        if (codeRef == null) {
            return null;
        }
        synchronized (this) {
            if (content == null) {
                try {
                    content = StringReferenceWrapper.value(codeRef);
                } catch (InternalExceptionWrapper |
                         VMDisconnectedExceptionWrapper |
                         ObjectCollectedExceptionWrapper ex) {
                    content = ex.getLocalizedMessage();
                }
            }
            return content;
        }
    }
    
}
