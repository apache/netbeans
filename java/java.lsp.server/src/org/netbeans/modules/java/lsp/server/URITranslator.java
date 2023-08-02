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
package org.netbeans.modules.java.lsp.server;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.modules.Places;
import org.openide.util.Exceptions;

/**
 * Translation between NetBeans URIs and LSP URIs. LSP clients might not like
 * archive URIs. The archive URIs are translated to plain file URIs from a cache
 * directory.
 */
public final class URITranslator {

    private static final URITranslator INSTANCE = new URITranslator();

    private final Map<String, String> uriFromCacheMap = new LRUCacheMap();
    private final Map<String, String> uriToCacheMap = new LRUCacheMap();

    public static URITranslator getDefault() {
        return INSTANCE;
    }

    public synchronized String uriToLSP(String lspUri) {
        return uriFromCacheMap.computeIfAbsent(lspUri, uri -> {
            URI uriUri = URI.create(uri);
            URL url;
            try {
                url = uriUri.toURL();
            } catch (MalformedURLException ex) {
                return uri;
            }
            if (FileUtil.isArchiveArtifact(url)) {
                FileObject file = URLMapper.findFileObject(url);
                if (file == null) {
                    return uri;
                }
                try {
                    return URLMapper.findURL(file, URLMapper.EXTERNAL).toURI().toString();
                } catch (URISyntaxException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            if (uriUri.getScheme().equals("nbfs")) {            // NOI18N
                FileObject file = URLMapper.findFileObject(url);
                if (file == null) {
                    return uri;
                }
                try {
                    String txt = file.asText("UTF-8");          // NOI18N
                    try (OutputStream os = file.getOutputStream()) {
                        os.write(txt.getBytes(StandardCharsets.UTF_8));
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
                try {
                    uri = URLMapper.findURL(file, URLMapper.EXTERNAL).toURI().toString();
                } catch (URISyntaxException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            return uri;
        });
    }

    public synchronized String uriFromLSP(String nbUri) {
        return uriToCacheMap.computeIfAbsent(nbUri, uri -> {
            try {
                return URLDecoder.decode(nbUri, StandardCharsets.UTF_8.name());
            } catch (UnsupportedEncodingException ex) {}
            return uri;
        });
    }

    /**
     * Root of caches for NBLS server.
     * @return cache root.
     */
    public static File getCacheDir() {
        return Places.getCacheSubfile("java-server");   // NOI18N
    }

    private static class LRUCacheMap extends LinkedHashMap<String, String> {

        private static final int MAX_SIZE = 2048;

        LRUCacheMap() {
            super(32, 0.75f, true);
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
            return size() > MAX_SIZE;
        }

    }
}
