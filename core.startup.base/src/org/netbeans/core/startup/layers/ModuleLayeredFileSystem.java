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

import org.netbeans.core.startup.base.LayerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MultiFileSystem;
import org.openide.filesystems.Repository.LayerProvider;
import org.openide.filesystems.XMLFileSystem;
import org.openide.modules.ModuleInfo;
import org.openide.util.*;

/** Layered file system serving itself as either the user or installation layer.
 * Holds one layer of a writable system directory, and some number
 * of module layers.
 * @author Jesse Glick, Jaroslav Tulach
 */
public class ModuleLayeredFileSystem extends MultiFileSystem 
implements LookupListener {
    /** serial version UID */
    private static final long serialVersionUID = 782910986724201983L;
    
    static final Logger err = Logger.getLogger("org.netbeans.core.projects"); // NOI18N

    /** lookup result for registered filesystems */
    private static Lookup.Result<FileSystem> fsResult = Lookup.getDefault().lookupResult(FileSystem.class);
    private static Lookup.Result<LayerProvider> layerResult = Lookup.getDefault().lookupResult(LayerProvider.class);
    /** a mutex to verify setURL calls are safe */
    private static Mutex mutex;

    /** current list of URLs - r/o; or null if not yet set */
    private List<URL> urls;
    private List<URL> prevs;
    /** cache manager */
    private LayerFactory manager;
    /** writable layer */
    private final FileSystem writableLayer;
    /** cache layer */
    private FileSystem cacheLayer;
    /** other layers */
    private final FileSystem[] otherLayers;
    /** addLookup */
    private final boolean addLookupBefore;
    
    private final boolean user;
    
    ModuleLayeredFileSystem(FileSystem writableLayer, boolean userDir, FileSystem[] extras, LayerFactory factory) throws IOException {
        this(writableLayer, userDir, extras, factory, factory.loadCache());
    }

    /** Create layered filesystem based on a supplied writable layer.
     * @param userDir is this layer for modules from userdir or not?
     * @param writableLayer the writable layer to use, typically a LocalFileSystem
     * @param otherLayers some other layers to use, e.g. LocalFileSystem[]
     * @param cacheDir a directory in which to store a cache, or null for no caching
    ModuleLayeredFileSystem (FileSystem writableLayer, boolean userDir, FileSystem[] otherLayers, boolean mgr) throws IOException {
        this(writableLayer, userDir, otherLayers, LayerCacheManager.manager(mgr));
    }
    
    private ModuleLayeredFileSystem(FileSystem writableLayer, boolean addLookup, FileSystem[] otherLayers, LayerFactory mgr) throws IOException {
        this(writableLayer, addLookup, otherLayers, mgr, mgr.loadCache());
    }
     */
    
    private ModuleLayeredFileSystem(FileSystem writableLayer, boolean addLookup, FileSystem[] otherLayers, LayerFactory mgr, FileSystem cacheLayer) throws IOException {
        super(
            appendLayers(
                writableLayer, addLookup, otherLayers,
                cacheLayer == null ? mgr.createEmptyFileSystem() : cacheLayer,
                addLookup
            )
        );
        this.manager = mgr;
        this.writableLayer = writableLayer;
        this.otherLayers = otherLayers;
        this.cacheLayer = cacheLayer;
        this.addLookupBefore = addLookup;
        
        // Wish to permit e.g. a user-installed module to mask files from a
        // root-installed module, so propagate masks up this high.
        // SystemFileSystem leaves this off, so that the final file system
        // will not show them if there are some left over.
        setPropagateMasks (true);
        
        urls = null;

        fsResult.addLookupListener(this);
        layerResult.addLookupListener(this);
        
        user = addLookup;
    }
    
    private static FileSystem[] appendLayers(FileSystem fs1, boolean addLookupBefore, FileSystem[] fs2s, FileSystem fs3, boolean addClasspathLayers) {
        List<FileSystem> l = new ArrayList<FileSystem>(fs2s.length + 2);
        l.add(fs1);
        if (addLookupBefore) {
            for (FileSystem f : fsResult.allInstances()) {
                if (Boolean.TRUE.equals(f.getRoot().getAttribute("fallback"))) { // NOI18N
                    continue;
                }
                l.add(f);
            }
        }
        l.addAll(Arrays.asList(fs2s));
        l.add(fs3);
        if (addClasspathLayers) { // #129583
            // Basic impl copied from ExternalUtil.MainFS:
            List<URL> layerUrls = null;
            try {
                layerUrls = collectLayers(ModuleInfo.class.getClassLoader());
                if (!layerUrls.isEmpty()) {
                    XMLFileSystem xmlfs = new XMLFileSystem();
                    xmlfs.setXmlUrls(layerUrls.toArray(new URL[layerUrls.size()]));
                    l.add(xmlfs);
                }
                err.log(Level.FINE, "Loading classpath layers: {0}", layerUrls);
            } catch (Exception x) {
                err.log(Level.WARNING, "Setting layer URLs: " + layerUrls, x);
            }
        }
        if (!addLookupBefore) {
            for (FileSystem f : fsResult.allInstances()) {
                if (Boolean.TRUE.equals(f.getRoot().getAttribute("fallback"))) { // NOI18N
                    l.add(f);
                }
            }
        }
        return l.toArray(new FileSystem[l.size()]);
    }

    /** Get all layers.
     * @return all filesystems making layers
     */
    public/*but just for debugging*/ final FileSystem[] getLayers () {
        return getDelegates ();
    }

    /** Get the writable layer.
     * @return the writable layer
     */
    final FileSystem getWritableLayer () {
        return writableLayer;
    }
    
    /** Get the installation layer.
     * You can take advantage of the specialized return type
     * if working within the core.
     */
    public static ModuleLayeredFileSystem getInstallationModuleLayer () {
        SystemFileSystem sfs;
        try {
            sfs = (SystemFileSystem) FileUtil.getConfigRoot().getFileSystem();
        } catch (FileStateInvalidException ex) {
            throw new AssertionError(ex);
        }
        ModuleLayeredFileSystem home = sfs.getInstallationLayer ();
        if (home != null) {
            return home;
        } else {
            return sfs.getUserLayer ();
        }
    }    
    
    /** Get the user layer.
     * You can take advantage of the specialized return type
     * if working within the core.
     */
    public static ModuleLayeredFileSystem getUserModuleLayer () {
        SystemFileSystem sfs;
        try {
            sfs = (SystemFileSystem) FileUtil.getConfigRoot().getFileSystem();
        } catch (FileStateInvalidException ex) {
            throw new AssertionError(ex);
        }
        return sfs.getUserLayer ();
    }

    /** Change the list of module layers URLs.
     * @param urls the urls describing module layers to use. List<URL>
     */
    public void setURLs (List<URL> urls) throws Exception {
        assert mutex == null || mutex.isWriteAccess();
        if (urls == null) {
            urls = this.prevs;
        }
        if (urls == null) {
            return;
        }
        if (urls.contains(null)) {
            throw new NullPointerException("urls=" + urls);
        } // NOI18N
        if (err.isLoggable(Level.FINE)) {
            err.log(Level.FINE, "setURLs: {0}", urls);
        }
        List<URL> orig = urls;
        if (this == ModuleLayeredFileSystem.getInstallationModuleLayer()) {
            urls = manager.additionalLayers(urls);
        }
        if (this.urls != null && urls.equals(this.urls)) {
            err.fine("no-op");
            return;
        }
        
        if (this.urls == null && cacheLayer != null) {
            // start where the BinaryFS was used to initialize the content
        } else {
            if (cacheLayer == null) {
                cacheLayer = manager.createEmptyFileSystem();
            }
            cacheLayer = manager.store(cacheLayer, urls);
            err.log(Level.FINEST, "changing delegates");
            setDelegates(appendLayers(writableLayer, addLookupBefore, otherLayers, cacheLayer, addLookupBefore));
            err.log(Level.FINEST, "delegates changed");
        }
        
        this.urls = urls;
        this.prevs = orig;
        firePropertyChange ("layers", null, null); // NOI18N
    }
    
    /** Adds few URLs.
     */
    public void addURLs(Collection<URL> urls) throws Exception {
        if (urls.contains(null)) {
            throw new NullPointerException("urls=" + urls);
        }
        // Add to the front: #23609.
        ArrayList<URL> arr = new ArrayList<URL>(urls);
        if (this.prevs != null) {
            arr.addAll(this.prevs);
        }
        setURLs(arr);
    }
    
    /** Removes few URLs.
     */
    public void removeURLs(Collection<URL> urls) throws Exception {
        if (urls.contains(null)) {
            throw new NullPointerException("urls=" + urls);
        }
        ArrayList<URL> arr = new ArrayList<URL>();
        if (this.prevs != null) {
            arr.addAll(this.prevs);
        }
        arr.removeAll(urls);
        setURLs(arr);
    }
    
    /** Refresh layers */
    @Override public void resultChanged(final LookupEvent ev) {
        class ProcessEv implements Mutex.Action<Void> {
            @Override
            public Void run() {
                if (ev.getSource() == fsResult) {
                    setDelegates(appendLayers(writableLayer, addLookupBefore, otherLayers, cacheLayer, addLookupBefore));
                    return null;
                }
                if (ev.getSource() == layerResult) {
                    if (prevs != null) {
                        try {
                            setURLs(prevs);
                        } catch (Exception ex) {
                            err.log(Level.INFO, null, ex);
                        }
                    }
                    return null;
                }
                throw new IllegalStateException("Unknown source: " + ev.getSource());
            }
        }
        ProcessEv pev = new ProcessEv();
        if (mutex != null) {
            mutex.writeAccess(pev);
        } else {
            pev.run();
        }
    }
    
    public static List<URL> collectLayers(ClassLoader loader) throws IOException {
        List<URL> layerUrls = new ArrayList<URL>();
        for (URL manifest : NbCollections.iterable(loader.getResources("META-INF/MANIFEST.MF"))) { // NOI18N
            InputStream is = manifest.openStream();
            try {
                Manifest mani = new Manifest(is);
                String layerLoc = mani.getMainAttributes().getValue("OpenIDE-Module-Layer"); // NOI18N
                if (layerLoc != null) {
                    URL layer = loader.getResource(layerLoc);
                    if (layer != null) {
                        layerUrls.add(layer);
                    } else {
                        err.log(Level.WARNING, "No such layer: {0}", layerLoc);
                    }
                }
            } finally {
                is.close();
            }
        }
        for (URL generatedLayer : NbCollections.iterable(loader.getResources("META-INF/generated-layer.xml"))) { // NOI18N
            layerUrls.add(generatedLayer);
        }
        return layerUrls;
    }
    
    static void registerMutex(Mutex m) {
        mutex = m;
    }
}
