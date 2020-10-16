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
package org.netbeans.modules.java.lsp.server.debugging;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4j.debug.Source;
import org.netbeans.api.java.classpath.ClassPath;
import org.openide.filesystems.FileObject;

/**
 *
 * @author martin
 */
public final class NbSourceProvider {

    private static final Logger LOG = Logger.getLogger(NbSourceProvider.class.getName());

    private final Map<String, String> fqnToURI = Collections.synchronizedMap(new CacheMap());
    private final Map<String, Source> uriToSource = Collections.synchronizedMap(new CacheMap());
    private final DebugAdapterContext context;
    private ClassPath sources = ClassPath.EMPTY;

    NbSourceProvider(DebugAdapterContext context) {
        this.context = context;
    }

    public void setSourcePath(ClassPath sources) {
        this.sources = sources;
    }

    private String getSourceFileURI(String fqn, String relativePathName) {
        return fqnToURI.computeIfAbsent(fqn, name -> {
            FileObject source = sources.findResource(relativePathName);
            if (source != null) {
                return source.toURI().toString();
            }
            if (new File(relativePathName).exists()) {
                return relativePathName;
            }
            return "";
        });
    }

    public String getSourceContents(String arg0) {
        LOG.log(Level.INFO, "SourceContent {0}", arg0);
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public Source getSource(String sourceName, String debuggerURI) {
        return uriToSource.computeIfAbsent(debuggerURI, uri -> {
            Source source = new Source();
            source.setName(sourceName);
            source.setSourceReference(0);
            if (uri.startsWith("file:")) {
                String clientPath = context.getClientPath(uri);
                source.setPath(clientPath);
            } else {
                source.setPath(uri);
            }
            return source;
        });
    }

    /**
     * Find the source mapping for the specified source file name.
     */
    public static Source convertDebuggerSourceToClient(String fullyQualifiedName, String sourceName, String relativeSourcePath,
            DebugAdapterContext context) throws URISyntaxException {
        NbSourceProvider sourceProvider = context.getSourceProvider();
        String uri = sourceProvider.getSourceFileURI(fullyQualifiedName, relativeSourcePath);

        if (uri == null || uri.isEmpty()) {
            for (String path : context.getSourcePaths()) {
                Path fullpath = Paths.get(path, relativeSourcePath);
                if (Files.isRegularFile(fullpath)) {
                    uri = fullpath.toString();
                    break;
                }
            }
        }
        if (uri != null && !uri.isEmpty()) {
            return sourceProvider.getSource(sourceName, uri);
        } else {
            return null;
        }
    }

    private static final class CacheMap<K,V> extends LinkedHashMap<K, V> {

        private static final int SIZE_LIMIT = 5000;

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > SIZE_LIMIT;
        }
        
    }
}
