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
package org.netbeans.modules.java.lsp.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

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
                File cacheDir = getCacheDir();
                cacheDir.mkdirs();
                File segments = new File(cacheDir, "segments"); // NOI18N
                Properties props = new Properties();

                try (InputStream in = new FileInputStream(segments)) {
                    props.load(in);
                } catch (IOException ex) {
                    //OK, may not exist yet
                }
                FileObject archive = FileUtil.getArchiveFile(file);
                String archiveString = archive.toURL().toString();
                File foundSegment = null;
                for (String segment : props.stringPropertyNames()) {
                    if (archiveString.equals(props.getProperty(segment))) {
                        foundSegment = new File(cacheDir, segment);
                        break;
                    }
                }
                if (foundSegment == null) {
                    int i = 0;
                    while (props.getProperty("s" + i) != null) {    // NOI18N
                        i++;
                    }
                    foundSegment = new File(cacheDir, "s" + i);     // NOI18N
                    props.put("s" + i, archiveString);              // NOI18N
                    try (OutputStream in = new FileOutputStream(segments)) {
                        props.store(in, "");
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                File cache = new File(foundSegment, FileUtil.getRelativePath(FileUtil.getArchiveRoot(archive), file));
                cache.getParentFile().mkdirs();
                if (file.isFolder()) {
                    if (cache.exists() && cache.isFile()) {
                        if (!cache.delete()) {
                            return uri;
                        }
                    }
                    cache.mkdir();
                    return cache.toURI().toString();
                } else if (file.isData()) {
                    try {
                        if (cache.exists() && cache.isDirectory()) {
                            FileUtil.toFileObject(cache).delete();
                        }
                        try (OutputStream out = new FileOutputStream(cache)) {
                            out.write(file.asBytes());
                            return cache.toURI().toString();
                        }
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
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
            URI uriUri = URI.create(uri);
            File cacheDir = getCacheDir();
            URI relative = cacheDir.toURI().relativize(uriUri);
            if (relative != null && new File(cacheDir, relative.toString()).canRead()) {
                String segmentAndPath = relative.toString();
                int slash = segmentAndPath.indexOf('/');
                String segment = segmentAndPath.substring(0, slash);
                String path = segmentAndPath.substring(slash + 1);
                File segments = new File(cacheDir, "segments");     // NOI18N
                Properties props = new Properties();

                try (InputStream in = new FileInputStream(segments)) {
                    props.load(in);
                    String archiveUri = props.getProperty(segment);
                    FileObject archive = URLMapper.findFileObject(URI.create(archiveUri).toURL());
                    archive = archive != null ? FileUtil.getArchiveRoot(archive) : null;
                    FileObject file = archive != null ? archive.getFileObject(path) : null;
                    if (file != null) {
                        return file.toURI().toString();
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
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
