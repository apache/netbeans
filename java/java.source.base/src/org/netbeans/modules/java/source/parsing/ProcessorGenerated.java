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
package org.netbeans.modules.java.source.parsing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.modules.java.source.classpath.AptCacheForSourceQuery;
import org.netbeans.modules.java.source.indexing.JavaIndex;
import org.netbeans.modules.java.source.indexing.TransactionContext;
import org.openide.util.Exceptions;
import org.openide.util.Pair;
import org.openide.util.Parameters;
import org.openide.util.BaseUtilities;

/**
 *
 * @author Tomas Zezula
 */
//@NotThreadSafe
public final class ProcessorGenerated extends TransactionContext.Service {

    private static final Logger LOG = Logger.getLogger(ProcessorGenerated.class.getName());
    
    public enum Type {
        SOURCE,
        RESOURCE
    }
    
    private final URL root;
    private final URL aptRoot;
    private final Map<URL,Pair<Set<javax.tools.FileObject>,Set<javax.tools.FileObject>>> generated = new HashMap<>();
    private File cachedFile;
    private StringBuilder cachedValue;
    private Set<String> cachedResources;
    private boolean cacheChanged;
    private boolean closedTx;
    
    
    private ProcessorGenerated(@NullAllowed final URL root) {
        this.root = root;
        this.aptRoot = root == null ? null : AptCacheForSourceQuery.getAptFolder(root);
    }
    
    public Set<javax.tools.FileObject> getGeneratedSources(final URL forSource) {
        Pair<Set<javax.tools.FileObject>,Set<javax.tools.FileObject>> res = 
            generated.get(forSource);
        return res == null ? null : res.first();
    }
    
    public boolean canWrite() {
        return root != null;
    }

    @CheckForNull
    public URL findSibling(@NonNull final Collection<? extends URL> candidates) {
        URL res = null;
        for (URL candiate : candidates) {
            if (root == null || FileObjects.isParentOf(root, candiate)) {
                res = candiate;
                break;
            }
        }
        return res;
    }
    
    
    public void register(
        @NonNull final URL forSource,
        @NonNull final javax.tools.FileObject file,
        @NonNull final Type type) {
        if (!canWrite()) {
            return;
        }
        LOG.log(
            Level.FINE,
            "Generated: {0} from: {1} type: {2}",   //NOI18N
            new Object[]{
                file.toUri(),
                forSource,
                type
        });
        Pair<Set<javax.tools.FileObject>,Set<javax.tools.FileObject>> insertInto =
                generated.get(forSource);
        if (insertInto == null) {
            insertInto = Pair.<Set<javax.tools.FileObject>,Set<javax.tools.FileObject>>of(
                    new HashSet<javax.tools.FileObject>(),
                    new HashSet<javax.tools.FileObject>());
            generated.put(forSource, insertInto);
        }
        switch (type) {
            case SOURCE:
                insertInto.first().add(file);
                break;
            case RESOURCE:
                insertInto.second().add(file);
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    protected void commit() throws IOException {
        closeTx();
        if (!canWrite()) {
            assert generated.isEmpty();
            return;
        }
        try {
            if (!generated.isEmpty()) {
                for (Map.Entry<URL,Pair<Set<javax.tools.FileObject>,Set<javax.tools.FileObject>>> entry : generated.entrySet()) {
                    final URL source = entry.getKey();
                    final Pair<Set<javax.tools.FileObject>,Set<javax.tools.FileObject>> gen = entry.getValue();
                    final Set<javax.tools.FileObject> genSources = gen.first();
                    final Set<javax.tools.FileObject> genResources =  gen.second();
                    commitSource(source, genSources, genResources);
                }
                writeResources();
            }
        } finally {
            clear();
        }
    }

    @Override
    protected void rollBack() throws IOException {
        closeTx();
        if (!canWrite()) {
            assert generated.isEmpty();
            return;
        }
        clear();
    }
    
    private void clear() {
        generated.clear();
        cachedFile = null;
        cachedResources = null;
        cachedValue = null;
        cacheChanged = false;
    }

    private void closeTx() {
        if (closedTx) {
            throw new IllegalStateException("Already commited or rolled back transaction.");    //NOI18N
        }
        closedTx = true;
    }
    
    private void commitSource(
        @NonNull final URL forSource,
        @NonNull final Set<javax.tools.FileObject> genSources,
        @NonNull final Set<javax.tools.FileObject> genResources) {
        try {
            boolean apt = false;
            URL sourceRootURL = getOwnerRoot(forSource, root);
            if (sourceRootURL == null) {
                sourceRootURL = aptRoot != null ? getOwnerRoot(forSource, aptRoot) : null;
                if (sourceRootURL == null) {
                    return;
                }
                apt = true;
            }
            final File sourceRoot = BaseUtilities.toFile(sourceRootURL.toURI());
            final File classCache = apt ?
                BaseUtilities.toFile(AptCacheForSourceQuery.getClassFolder(sourceRootURL).toURI()):
                JavaIndex.getClassFolder(sourceRoot);
            if (!genSources.isEmpty()) {
                final File sourceFile = BaseUtilities.toFile(forSource.toURI());
                final String relativePath = FileObjects.stripExtension(FileObjects.getRelativePath(sourceRoot, sourceFile));
                final File cacheFile = new File (classCache, relativePath+'.'+FileObjects.RAPT);
                if (!cacheFile.getParentFile().exists()) {
                    cacheFile.getParentFile().mkdirs();
                }
                final URL aptRootURL = AptCacheForSourceQuery.getAptFolder(sourceRootURL);
                final StringBuilder sb = new StringBuilder();
                for (javax.tools.FileObject file : genSources) {
                    sb.append(FileObjects.getRelativePath(aptRootURL, file.toUri().toURL()));
                    sb.append('\n');    //NOI18N
                }
                writeFile(cacheFile, sb);
            }
            if (!genResources.isEmpty()) {
                final File resFile = new File (classCache,FileObjects.RESOURCES);
                final Set<String> currentResources = new HashSet<String>();
                final StringBuilder sb = readResources(resFile, currentResources);
                boolean changed = false;
                for (javax.tools.FileObject file : genResources) {
                    String resPath = FileObjects.getRelativePath(BaseUtilities.toURI(classCache).toURL(), file.toUri().toURL());
                    if (currentResources.add(resPath)) {
                        sb.append(resPath);
                        sb.append('\n');    //NOI18N
                        changed = true;
                    }
                }
                if (changed) {
                    updateCache(sb, currentResources);
                }
            }
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        } catch (URISyntaxException use) {
            Exceptions.printStackTrace(use);
        }
    }
    
    private StringBuilder readResources(
            @NonNull final File file,
            @NonNull Set<? super String> currentResources) {
        if (cachedFile == null) {
            cachedValue = readFile(file);
            cachedResources = new HashSet<String>(Arrays.asList(cachedValue.toString().split("\n")));  //NOI18N
            cachedFile = file;
        }        
        assert cachedValue != null;
        assert cachedResources != null;
        assert cachedFile.equals(file);
        currentResources.addAll(cachedResources);
        return cachedValue;
    }
    
    private StringBuilder readFile(final File file) {        
        StringBuilder sb = new StringBuilder();
        try {
            final Reader in = new InputStreamReader (new FileInputStream (file), StandardCharsets.UTF_8);
            try {
                char[] buffer = new char[1024];
                int len;
                while ((len=in.read(buffer))>0) {
                    sb.append(buffer, 0, len);
                }
            } finally {
                in.close();
            }
        } catch (IOException ioe) {
            if (sb.length() != 0) {
                sb = new StringBuilder();
            }
        }
        return sb;
    }
    
    
    private void writeResources() throws IOException {
        if (cacheChanged) {
            assert cachedFile != null;
            assert cachedValue != null;
            writeFile(cachedFile, cachedValue);
        }
    }
    
    private void writeFile (@NonNull final File file, @NonNull final StringBuilder data) throws IOException {        
        final Writer out = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
        try {
            out.write(data.toString());
        } finally {
            out.close();
        }
    }

    private void updateCache(
            @NonNull final StringBuilder data,
            @NonNull final Set<String> currentResources) {
        assert data != null;
        assert currentResources != null;
            cachedValue = data;
            cachedResources = currentResources;
            cacheChanged = true;
    }

    @CheckForNull
    private static URL getOwnerRoot (@NonNull final URL source, @NonNull final URL root) throws URISyntaxException {
        assert source != null;
        assert root != null;
        if (FileObjects.isParentOf(root, source)) {
            return root;
        } else {
            return null;
        }
    }

    @NonNull
    public static ProcessorGenerated create(@NonNull final URL root) {
        return new ProcessorGenerated(root);
    }

    @NonNull
    public static ProcessorGenerated nullWrite() {
        return new ProcessorGenerated(null);
    }
}
