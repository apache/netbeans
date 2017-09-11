/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
 * @see "#20168"
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
                            fallback.setXmlUrls(urls.toArray(new URL[urls.size()]));
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
     * {@link #createEmptyFileSystem} or {@link #createLoadedFileSystem}.
     * Not called if the manager does not support loading;
     * otherwise must be overridden.
     */
    public abstract FileSystem load(FileSystem previous, ByteBuffer bb) throws IOException;
    
    /**
     * Save a new cache to disk, load it, and return that filesystem.
     * @param urls list of type URL; earlier layers can override later layers
     * @return a new filesystem with the specified contents
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
                fs.setXmlUrls(urls.toArray(new URL[urls.size()]));
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
