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

package org.netbeans.core.startup.layers;

import java.beans.PropertyVetoException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.Stamps;
import org.netbeans.core.startup.Main;
import org.netbeans.core.startup.NbRepository;
import org.netbeans.core.startup.StartLog;
import org.netbeans.core.startup.base.LayerFactory;
import org.netbeans.core.startup.impl.BinaryLayerFactoryProvider;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileSystem.AtomicAction;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.XMLFileSystem;
import org.openide.util.NbBundle;

/** Interface for a manager which can handle XML layer caching.
 * @see <a href="https://bz.apache.org/netbeans/show_bug.cgi?id=20168">20168</a>
 * @author Jesse Glick
 */
public abstract class LayerCacheManager implements LayerFactory {

    /** Local error manager for in-package use.
     */
    static final Logger err = Logger.getLogger("org.netbeans.core.projects.cache"); // NOI18N
    
    private static final LayerCacheManager mgr = new BinaryCacheManager();
    private static final LayerCacheManager non = new NonCacheManager();
    /**
     * Get a cache manager which does nothing.
     */
    public static LayerCacheManager manager(boolean real) {
        return real ? mgr : non;
    }

    /** Creates new cache manager for given cache location. The
     * location shall be unique file name.
     *
     * @param cacheLocation unique file name
     * @return cache manager
     */
    public static LayerCacheManager create(String cacheLocation) {
        return new BinaryCacheManager(cacheLocation);
    }

    /** Loads the filesystem from cache if cache is available.
     *
     * @return the file system read or null, if the cache is out of date
     * @throws IOException if an I/O error occurs
     */
    
    /*
    Can't use @Messages, as the package is shared with startup.base; generated Bundle
    files would overwrite each other.
    @Messages({
        "MSG_start_load_cache=Loading cached objects...",
        "MSG_end_load_cache=Loading cached objects...done."
    })
    */
    public final FileSystem loadCache() throws IOException {
        String location = cacheLocation();
        FileSystem fs = null;

        if (location != null) {
            Main.setStatusText(NbBundle.getMessage(BinaryLayerFactoryProvider.class, "MSG_start_load_cache"));
            ByteBuffer bb = Stamps.getModulesJARs().asMappedByteBuffer(location);
            if (bb != null) {
                try {
                    StartLog.logStart("Loading layers"); // NOI18N
                    fs = load(createEmptyFileSystem(), bb);
                    
                    Main.setStatusText(NbBundle.getMessage(BinaryLayerFactoryProvider.class, "MSG_end_load_cache"));
                    StartLog.logEnd("Loading layers"); // NOI18N
                } catch (IOException ex) {
                    err.log(Level.WARNING, "Ignoring cache of layers");
                    if (err.isLoggable(Level.FINE)) {
                        err.log(Level.WARNING, "Ignoring cache of layers", ex);
                    }
                }
            }
        }
        return fs;
    }

    public final FileSystem store(final FileSystem fs, final List<URL> urls) throws IOException {
        class Updater implements AtomicAction, Stamps.Updater {
            private FileSystem toRet;
            private byte[] data;

            public void flushCaches(DataOutputStream os) throws IOException {
                err.log(Level.FINEST, "flushing layers");
                os.write(data);
                err.log(Level.FINEST, "layers flushed");
            }

            public void cacheReady() {
                /*
                try {
                err.log(Level.FINEST, "cache is ready");
                cacheLayer = loadCache(manager);
                err.log(Level.FINEST, "update delegates for userdir:" + addLookup + " manager: " + manager);
                setDelegates(appendLayers(writableLayer, addLookup, otherLayers, cacheLayer));
                err.log(Level.FINEST, "delegates updated");
                } catch (IOException ex) {
                err.log(Level.INFO, "Cannot re-read cache", ex); // NOI18N
                }
                 */
            }

            public void run() throws IOException {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                synchronized (LayerCacheManager.this) {
                    try {
                        err.log(Level.FINEST, "storing to memory {0}", urls);
                        store(fs, urls, os);
                        data = os.toByteArray();
                        ByteBuffer bb = ByteBuffer.wrap(data);
                        err.log(Level.FINEST, "reading from memory, size {0}", bb.limit());
                        toRet = load(fs, bb.order(ByteOrder.LITTLE_ENDIAN));
                    } catch (IOException ioe) {
                        err.log(Level.WARNING, null, ioe);
                        XMLFileSystem fallback = new XMLFileSystem();
                        try {
                            fallback.setXmlUrls(urls.toArray(new URL[0]));
                        } catch (PropertyVetoException ex) {
                            err.log(Level.WARNING, null, ex);
                        }
                        toRet = fallback;
                    }
                }
                Stamps.getModulesJARs().scheduleSave(this, cacheLocation(), false);
            }
        }
        Updater u = new Updater();
        FileUtil.runAtomicAction(u);
        return u.toRet;
    }

    /**
     * Provides additiona layers to be configured for the target FileSystem
     * @param urls initial set of URLs
     * @return modified set of URLs to create a filesystem with.
     * 
     * @since 1.60
     */
    @Override
    public List<URL> additionalLayers(List<URL> urls) {
        return ((NbRepository)NbRepository.getDefault()).additionalLayers(urls);
    }

    /** Create a cache manager (for subclass use).
     */
    LayerCacheManager() {
    }
    
    /** Create an empty cache filesystem, i.e. with no initial layers.
     * Should only be called when the cache directory is clean.
     * Should not be overridden if the manager does not support loading;
     * otherwise must be overridden.
     */
    public abstract FileSystem createEmptyFileSystem() throws IOException;
    
    /** Load the cache from disk.
     * Should only be called when the cache directory is prepared.
     * The filesystem's contents should be modified.
     * The filesystem must have been originally produced by
     * {@link #createEmptyFileSystem}.
     * Not called if the manager does not support loading;
     * otherwise must be overridden.
     */
    public abstract FileSystem load(FileSystem previous, ByteBuffer bb) throws IOException;
    
    /**
     * Save a new cache to disk, load it, and return that filesystem.
     * @param urls list of type URL; earlier layers can override later layers
     * Not called if the manager supports loading;
     * otherwise must be overridden.
     */
    public abstract void store(FileSystem fs, List<URL> urls, OutputStream os) throws IOException;
    
    /** Location of cache.
     * 
     * @return path to cache
     */
    abstract String cacheLocation();
    
    private static final class NonCacheManager extends LayerCacheManager {
        @Override
        public FileSystem createEmptyFileSystem() throws IOException {
            return new XMLFileSystem();
        }

        @Override
        public FileSystem load(FileSystem previous, ByteBuffer bb) throws IOException {
            byte[] arr = new byte[bb.limit()];
            bb.get(arr);
            DataInputStream is = new DataInputStream(new ByteArrayInputStream(arr));
            List<URL> urls = new ArrayList<URL>();
            while (is.available() > 0) {
                String u = is.readUTF();
                urls.add(new URL(u));
            }
            try {
                XMLFileSystem fs = (XMLFileSystem)previous;
                fs.setXmlUrls(urls.toArray(new URL[0]));
                return fs;
            } catch (PropertyVetoException pve) {
                throw (IOException) new IOException(pve.toString()).initCause(pve);
            }
        }

        @Override
        public void store(FileSystem fs, List<URL> urls, OutputStream os) throws IOException {
            DataOutputStream data = new DataOutputStream(os);
            for (URL u : urls) {
                data.writeUTF(u.toExternalForm());
            }
            data.close();
        }

        @Override
        public String cacheLocation() {
            return "all-local-layers.dat"; // NOI18N
        }
    } // end of NonCacheManager
}
