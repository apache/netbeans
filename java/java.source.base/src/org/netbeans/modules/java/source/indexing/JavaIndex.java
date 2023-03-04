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

package org.netbeans.modules.java.source.indexing;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Properties;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.java.source.parsing.CachingArchiveProvider;
import org.netbeans.modules.java.source.usages.ClassIndexImpl;
import org.netbeans.modules.java.source.usages.ClassIndexManager;
import org.netbeans.modules.parsing.impl.indexing.CacheFolder;
import org.netbeans.modules.parsing.impl.indexing.SPIAccessor;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;
import org.openide.util.BaseUtilities;

/**
 *
 * @author Jan Lahoda, Dusan Balek
 */
public final class JavaIndex {

    public static final String NAME = "java"; //NOI18N
    public static final int VERSION = 15;
    public static final String ATTR_MODULE_NAME = "moduleName"; //NOI18N
    public static final Logger LOG = Logger.getLogger(JavaIndex.class.getName());
    private static final String CLASSES = "classes"; //NOI18N
    private static final String APT_SOURCES = "sources";    //NOI18N
    private static final String ATTR_FILE_NAME = "attributes.properties"; //NOI18N
    private static final int SLIDING_WINDOW = 1000;
    private static final RequestProcessor RP = new RequestProcessor(JavaIndex.class);
    private static final RequestProcessor.Task SAVER = RP.create(new Saver());
    
    //Single line cache for index properties
    private static final Object cacheLock = new Object();
    //@GuardedBy("cacheLock")
    private static URL cacheRoot;
    //@GuardedBy("cacheLock")
    private static Reference<Properties> cacheValue;
    //@GuardedBy("cacheLock")
    private static final Queue<Pair<URL,Properties>> savePending = new ArrayDeque<>();
    

    public static File getIndex(Context c) {
        return FileUtil.toFile(c.getIndexFolder());
    }

    public static File getIndex(URL url) throws IOException {
        url = CachingArchiveProvider.getDefault().mapCtSymToJar(url);
        FileObject indexBaseFolder = CacheFolder.getDataFolder(url);
        String path = SPIAccessor.getInstance().getIndexerPath(NAME, VERSION);
        FileObject indexFolder = FileUtil.createFolder(indexBaseFolder, path);
        return FileUtil.toFile(indexFolder);
    }

    public static File getClassFolder(Context c) {
        return getClassFolder(c, false);
    }
    
    public static File getClassFolder(Context c, boolean onlyIfExists) {
        return processCandidate(new File(getIndex(c), CLASSES), onlyIfExists, true);
    }

    public static File getClassFolder(File root) throws IOException {
        return getClassFolder(BaseUtilities.toURI(root).toURL()); //XXX
    }

    public static File getClassFolder(URL url) throws IOException {
        return getClassFolder(url, false, true);
    }
    
    public static File getClassFolder(URL url, boolean onlyIfExists) throws IOException {
        return getClassFolder(url, onlyIfExists, true);
    }

    public static File getClassFolder(URL url, boolean onlyIfExists, boolean create) throws IOException {
        return processCandidate(new File(getIndex(url), CLASSES), onlyIfExists, create);
    }

    public static File getAptFolder(final URL sourceRoot, final boolean create) throws IOException {
        final File aptSources = new File (getIndex(sourceRoot), APT_SOURCES);
        if (create) {
            aptSources.mkdirs();
        }
        return aptSources;
    }

    public static URL getSourceRootForClassFolder(URL binaryRoot) {
        FileObject folder = URLMapper.findFileObject(binaryRoot);
        if (folder == null || !CLASSES.equals(folder.getName()))
            return null;
        folder = folder.getParent();
        if (folder == null || !String.valueOf(VERSION).equals(folder.getName()))
            return null;
        folder = folder.getParent();
        if (folder == null || !NAME.equals(folder.getName()))
            return null;
        folder = folder.getParent();
        if (folder == null)
            return null;
        return CacheFolder.getSourceRootForDataFolder(folder);
    }

    public static boolean ensureAttributeValue(final URL root, final String attributeName, final String attributeValue) throws IOException {
        return ensureAttributeValue(root, attributeName, attributeValue, false);
    }

    public static boolean ensureAttributeValue(final URL root, final String attributeName, final String attributeValue, boolean checkOnly) throws IOException {
        Properties p = loadProperties(root);
        final String current = p.getProperty(attributeName);
        if (current == null) {
            if (attributeValue != null) {
                if (!checkOnly) {
                    p.setProperty(attributeName, attributeValue);
                    storeProperties(root, p, false);
                }
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log (
                        Level.FINE,
                        "ensureAttributeValue attr: {0} current: {1} new: {2} checkOnly: {3}",  //NOI18N
                        new Object[]{
                            attributeName,
                            current,
                            attributeValue,
                            checkOnly
                        }
                    );
                }
                return true;
            } else {
                return false;
            }
        }
        if (current.equals(attributeValue)) {
            return false;
        }
        if (!checkOnly) {
            if (attributeValue != null) {
                p.setProperty(attributeName, attributeValue);
            } else {
                p.remove(attributeName);
            }
            storeProperties(root, p, false);
        }
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log (
                Level.FINE,
                "ensureAttributeValue attr: {0} current: {1} new: {2} checkOnly: {3}", //NOI18N
                new Object[]{
                    attributeName,
                    current,
                    attributeValue,
                    checkOnly
                });
        }
        return true;
    }

    public static void setAttribute(URL root, String attributeName, String attributeValue) throws IOException {
        boolean store;
        Properties p = loadProperties(root);
        if (attributeValue != null) {
            store = !attributeValue.equals(p.setProperty(attributeName, attributeValue));
        } else {
            store = p.remove(attributeName) != null;
        }
        if (store) {
            storeProperties(root, p, true);
        }
    }

    public static String getAttribute(URL root, String attributeName, String defaultValue) throws IOException {
        Properties p = loadProperties(root);
        return p.getProperty(attributeName, defaultValue);
    }

    private static Properties loadProperties(URL root) throws IOException {
        synchronized (cacheLock) {
            Properties result;
            for (Pair<URL,Properties> active : savePending) {
                if (active.first().equals(root)) {
                    return active.second();
                }
            }
            if (cacheRoot != null && cacheRoot.equals(root)) {
                result = cacheValue == null ? null : cacheValue.get();
                if (result != null) {
                    return result;
                }
            }
            final File f = getAttributeFile(root);
            result = new Properties();
            if (!f.exists())
                return result;
            final InputStream in = new BufferedInputStream(new FileInputStream(f));
            try {
                result.load(in);
            } catch (IllegalArgumentException iae) {
                //Issue #138704: Invalid unicode encoding in attribute file.
                //Return newly constructed Properties, the result
                //may already contain some pairs.
                LOG.log(
                    Level.WARNING,
                    "Broken attribute file: {0}",   //NOI18N
                    f.getAbsolutePath());
                result = new Properties();
            } finally {
                in.close();
            }
            cacheRoot = root;
            cacheValue = new SoftReference<Properties>(result);        
            return result;
        }
    }

    private static void storeProperties(URL root, Properties p, boolean barrier) throws IOException {
        synchronized (cacheLock) {
            if (barrier) {
                for (Iterator<Pair<URL,Properties>> it = savePending.iterator();
                    it.hasNext();) {
                    final Pair<URL,Properties> pending = it.next();
                    if (pending.first().equals(root)) {
                        it.remove();
                        break;
                    }
                }
                storeImpl(root, p);
            } else {
                boolean alreadyStoring = false;
                for (Pair<URL,Properties> pending : savePending) {
                    if (pending.first().equals(root)) {
                        alreadyStoring = true;
                        break;
                    }
                }
                if (!alreadyStoring) {
                    savePending.offer(Pair.<URL,Properties>of(root,p));
                }
                SAVER.schedule(SLIDING_WINDOW);
            }
        }
    }

    private static void storeImpl(
            @NonNull final URL root,
            @NonNull final Properties p) throws IOException {
        if (!Thread.holdsLock(cacheLock)) {
            throw new IllegalStateException("Requires cacheLock");  //NOI18N
        }
        final File f = getAttributeFile(root);
        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(f))) {
            p.store(out, ""); //NOI18N
        }
        if (cacheRoot == null || cacheRoot.equals(root)) {
            cacheRoot = root;
            cacheValue = new SoftReference<>(p);
        }
    }

    private static File getAttributeFile(URL root) throws IOException {
        return new File(JavaIndex.getIndex(root), ATTR_FILE_NAME);
    }

    private static File processCandidate(File result, boolean onlyIfExists, boolean create) {
        if (onlyIfExists) {
            if (!result.exists()) {
                return null;
            } else {
                return result;
            }
        }
        if (create) {
            result.mkdirs();
        }
        return result;
    }


    public static boolean hasSourceCache (
            @NonNull final URL root,
            final boolean testForInitialized) {
        assert root != null;
        final ClassIndexImpl uq = ClassIndexManager.getDefault().getUsagesQuery(root, !testForInitialized);
        return uq != null &&
            (!testForInitialized || uq.getState() == ClassIndexImpl.State.INITIALIZED) &&
            uq.getType() == ClassIndexImpl.Type.SOURCE;
    }

    public static boolean hasBinaryCache(
            @NonNull final URL root,
            final boolean testForInitialized) {
        assert root != null;
        final ClassIndexImpl uq = ClassIndexManager.getDefault().getUsagesQuery(root, !testForInitialized);
        return uq != null &&
               (!testForInitialized || uq.getState() == ClassIndexImpl.State.INITIALIZED) &&
               uq.getType() == ClassIndexImpl.Type.BINARY;
    }
    
    /**
     * Convenience method to check whether a root has been indexed.
     * Use in preference to direct call {@link #getUsagesQuery} for this check.
     * 
     * @param root root URL
     * @return true, if the class/usage index has been already created for this root.
     */
    public static boolean isIndexed(@NonNull final URL root) {
        return ClassIndexManager.getDefault().getUsagesQuery(root, false) != null;
    }

    private JavaIndex() {}

    private static final class Saver implements Runnable {
        @Override
        public void run() {
            synchronized (cacheLock) {
                final Pair<URL,Properties> car = savePending.peek();
                if (car != null) {
                    try {
                        storeImpl(car.first(), car.second());
                    }catch (IOException ioe) {
                        Exceptions.printStackTrace(ioe);
                    } finally {
                        savePending.remove();
                        if (!savePending.isEmpty()) {
                            SAVER.setPriority(SLIDING_WINDOW);
                        }
                    }
                }
            }
        }
    }

    public static boolean isCacheFolder(File dir) {
        File cacheFolder = FileUtil.toFile(CacheFolder.getCacheFolder());
        return dir.toURI().toASCIIString().startsWith(cacheFolder.toURI().toASCIIString());
    }
}
