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

package org.netbeans.modules.java.source.parsing;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Pair;
import org.openide.util.Parameters;
import java.nio.file.FileStore;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Tomas Zezula
 */
public final class CachingArchiveClassLoader extends ClassLoader {
    private static final String RES_PROCESSORS = "META-INF/services/javax.annotation.processing.Processor";    //NOI18N
    private static final int INI_SIZE = 16384;
    private static final Logger LOG = Logger.getLogger(CachingArchiveClassLoader.class.getName());
    //Todo: Performance Trie<File,ReentrantReadWriteLock>
    private static final ReentrantReadWriteLock LOCK = new ReentrantReadWriteLock();

    private final Map<URL, ProtectionDomain> codeDomains = new HashMap<>();
    private final List<Pair<URL,Archive>> archives;
    private final Optional<Consumer<? super URL>> usedRoots;
    private byte[] buffer;

    private CachingArchiveClassLoader(
            @NonNull final List<Pair<URL,Archive>> archives,
            @NullAllowed final ClassLoader parent,
            @NullAllowed final Consumer<? super URL> usedRoots) {
        super (parent);
        assert archives != null;
        this.archives = archives;
        this.usedRoots = Optional.<Consumer<? super URL>>ofNullable(usedRoots);
    }
    
    /**
     * Creates a ProtectionDomain for the location. Caches instances since more classes
     * is likely to be loaded.
     * @param location location
     * @return ProtectionDomain object.
     */
    private ProtectionDomain createCodeDomain(URL location) {
        return codeDomains.computeIfAbsent(location, 
            (l) -> new ProtectionDomain(
                new CodeSource(l, (CodeSigner[])null), null)
        );
    }

    @Override
    protected Class<?> findClass(final String name) throws ClassNotFoundException {
        final StringBuilder sb = new StringBuilder(FileObjects.convertPackage2Folder(name, '/'));
        sb.append(JavaFileObject.Kind.CLASS.extension);
        Class<?> c = null;
        try {
            c = readAction(new Callable<Class<?>>() {
                @Override
                public Class<?> call() throws Exception {
                    final Pair<URL, FileObject> locFile = findFileObject(sb.toString());
                    if (locFile != null) {
                        final FileObject file = locFile.second();
                        try {
                            final int len = readJavaFileObject(file);
                            int lastDot = name.lastIndexOf('.');
                            if (lastDot != (-1)) {
                                String pack = name.substring(0, lastDot);
                                if (getPackage(pack) == null) {
                                    definePackage(pack, null, null, null, null, null, null, null);
                                }
                            }
                            return defineClass(
                                    name,
                                    buffer,
                                    //Todo:
                                    //-buffer
                                    //+com.sun.tools.hc.LambdaMetafactory.translateClassFile(buffer,0,len),
                                    0,
                                    len, 
                                    createCodeDomain(locFile.first()));
                        } catch (FileNotFoundException fnf) {
                            LOG.log(Level.FINE, "Resource: {0} does not exist.", file.toUri()); //NOI18N
                        } catch (IOException ioe) {
                            LOG.log(Level.INFO, "Resource: {0} cannot be read.", file.toUri()); //NOI18N
                        }
                    }
                    return null;
                }
            });
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
        return c != null ?
            c :
            super.findClass(name);
    }

    @Override
    protected URL findResource(final String name) {
        FileObject file = null;
        try {
            file = readAction(new Callable<FileObject>() {
                @Override
                public FileObject call() throws Exception {
                    Pair<URL, FileObject> p = findFileObject(name);
                    return p == null ? null : p.second();
                }
            });
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
        if (file != null) {
            try {
                return file.toUri().toURL();
            } catch (MalformedURLException ex) {
                LOG.log(Level.INFO, ex.getMessage(), ex);
            }
        }
        return super.findResource(name);
    }

    @Override
    protected Enumeration<URL> findResources(final String name) throws IOException {
        try {
            return readAction(new Callable<Enumeration<URL>>(){
                @Override
                public Enumeration<URL> call() throws Exception {
                    @SuppressWarnings("UseOfObsoleteCollectionType")
                    final Vector<URL> v = new Vector<URL>();
                    for (final Pair<URL,Archive> p : archives) {
                        final Archive archive = p.second();
                        final FileObject file = archive.getFile(name);
                        if (file != null) {
                            v.add(file.toUri().toURL());
                            usedRoots
                                    .map((c) -> RES_PROCESSORS.equals(name) ? null : c)
                                    .ifPresent((c) -> c.accept(p.first()));
                        } else {
                            URI dirURI = archive.getDirectory(name);

                            if (dirURI != null) {
                                v.add(dirURI.toURL());
                                usedRoots
                                        .map((c) -> RES_PROCESSORS.equals(name) ? null : c)
                                        .ifPresent((c) -> c.accept(p.first()));
                            }
                        }
                    }
                    return v.elements();
                }
            });
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    private int readJavaFileObject(final FileObject jfo) throws IOException {
        assert LOCK.getReadLockCount() > 0;
        if (buffer == null) {
            buffer = new byte[INI_SIZE];
        }
        int len = 0;
        final InputStream in = jfo.openInputStream();
        try {
            while (true) {
                if (buffer.length == len) {
                    byte[] nb = new byte[2*buffer.length];
                    System.arraycopy(buffer, 0, nb, 0, len);
                    buffer = nb;
                }
                int l = in.read(buffer,len,buffer.length-len);
                if (l<=0) {
                    break;
                }
                len+=l;
            }

        } finally {
            in.close();
        }
        return len;
    }

    private Pair<URL, FileObject> findFileObject(final String resName) {
        assert LOCK.getReadLockCount() > 0;
        for (final Pair<URL,Archive> p : archives) {
            final Archive archive = p.second();
            try {
                final FileObject file = archive.getFile(resName);
                if (file != null) {
                    usedRoots.ifPresent((c) -> c.accept(p.first()));
                    URL u = FileUtil.getArchiveFile(p.first());
                    if (u == null) {
                        u = p.first();
                    }
                    return Pair.of(u, file);
                }
            } catch (IOException ex) {
                LOG.log(
                    Level.INFO,
                    "Cannot read: " + archive,  //NOI18N
                    ex);
            }
        }
        return null;
    }

    public static ClassLoader forClassPath(
            @NonNull final ClassPath classPath,
            @NullAllowed final ClassLoader parent,
            @NullAllowed final Consumer<? super URL> usedRoots) {
        Parameters.notNull("classPath", classPath); //NOI18N
        final List<ClassPath.Entry> entries = classPath.entries();
        final URL[] urls = new URL[entries.size()];
        final Iterator<ClassPath.Entry> eit = entries.iterator();
        for (int i=0; eit.hasNext(); i++) {
            urls[i] = eit.next().getURL();
        }
        return forURLs(urls, parent, usedRoots);
    }

    public static ClassLoader forURLs(
            @NonNull final URL[] urls,
            @NullAllowed final ClassLoader parent,
            @NullAllowed final Consumer<? super URL> usedRoots) {
        Parameters.notNull("urls", urls);       //NOI18N
        final List<Pair<URL,Archive>> archives = new ArrayList<>(urls.length);
        for (URL url : urls) {
            final Archive arch = CachingArchiveProvider.getDefault().getArchive(url, false);
            if (arch != null) {
                archives.add(Pair.of(url,arch));
            }
        }
        return new CachingArchiveClassLoader(
                archives,
                parent,
                usedRoots);
    }

    public static <T> T readAction(@NonNull final Callable<T> action) throws Exception {
        Parameters.notNull("action", action);   //NOI18N
        LOCK.readLock().lock();
        try {
            LOG.log(Level.FINE, "Read locked by {0}", Thread.currentThread());  //NOI18N
            return action.call();
        } finally {
            LOCK.readLock().unlock();
        }
    }

    public static <T> T writeAction(@NonNull final Callable<T> action) throws Exception {
        Parameters.notNull("action", action);   //NOI18N
        LOCK.writeLock().lock();
        try {
            LOG.log(Level.FINE, "Write locked by {0}", Thread.currentThread());  //NOI18N
            return action.call();
        } finally {
            LOCK.writeLock().unlock();
        }
    }

}
