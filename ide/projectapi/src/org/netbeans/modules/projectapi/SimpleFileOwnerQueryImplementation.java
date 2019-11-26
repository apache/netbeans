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

package org.netbeans.modules.projectapi;

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.FileOwnerQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.BaseUtilities;
import org.openide.util.NbPreferences;
import org.openide.util.WeakSet;

/**
 * Finds a project by searching the directory tree.
 * @author Jesse Glick
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.project.FileOwnerQueryImplementation.class, position=100)
public class SimpleFileOwnerQueryImplementation implements FileOwnerQueryImplementation {
    private static final Logger LOG = Logger.getLogger(SimpleFileOwnerQueryImplementation.class.getName());
    private static final URI UNOWNED_URI = URI.create("http:unowned");
    private static final Set<String> forbiddenFolders;
    static {
        Set<String> files = new HashSet<String>();
        try {
            String forbidden = System.getProperty("project.forbiddenFolders", System.getProperty("versioning.forbiddenFolders", "")); //NOI18N
            files.addAll(Arrays.asList(forbidden.split("\\;"))); //NOI18N
            files.remove(""); //NOI18N
        } catch (Exception e) {
            LOG.log(Level.INFO, e.getMessage(), e);
        }
        forbiddenFolders = files;
    }
    
    /** Do nothing */
    public SimpleFileOwnerQueryImplementation() {}
    
    public Project getOwner(URI fileURI) {
        // Try to find a FileObject for it.
        URI test = fileURI;
        FileObject file;
        do {
            file = uri2FileObject(test);
            test = goUp(test);
        } while (file == null && test != null);
        if (file == null) {
            return null;
        }
        return getOwner(file);
    }
    
    private final Set<FileObject> warnedAboutBrokenProjects = new WeakSet<FileObject>();
        
    private Map<FileObject, Reference<Project>> projectCache = new WeakHashMap();    
    /**
     * 
     * #111892
     */
    public void resetLastFoundReferences() {
        synchronized (cacheLock) {
            projectCache.clear();
            cacheInvalid = false;
        }
    }
    
    
    public Project getOwner(FileObject f) {
        List<FileObject> folders = new ArrayList<>();
        
        deserialize();
        while (f != null) {
            boolean folder = f.isFolder();
            final URI[] furi = new URI[1];
            if (folder) {
                synchronized (cacheLock) {
                    if (cacheInvalid) { 
                        projectCache.clear();
                        cacheInvalid = false;
                    }
                    Reference<Project> rp = projectCache.get(f);
                    if (rp != null) {
                        Project p = rp.get();
                        if (p != null) {
                            for (FileObject fldr : folders) {
                                projectCache.put(fldr, rp);
                            }
                            return p;
                        }
                    }
                }
                folders.add(f);
                if (!forbiddenFolders.contains(f.getPath()) &&
                    !hasRoot(externalOwners.keySet(), f, folder, furi) &&
                    !hasRoot(deserializedExternalOwners.keySet(), f, folder, furi)) {
                    Project p;
                    try {
                        p = ProjectManager.getDefault().findProject(f);
                    } catch (IOException e) {
                        // There is a project here, but we cannot load it...
                        if (warnedAboutBrokenProjects.add(f)) { // #60416
                            LOG.log(Level.FINE, "Cannot load project.", e); //NOI18N
                        }
                        return null;
                    }
                    if (p != null) {
                        synchronized (cacheLock) {
                            WeakReference<Project> rp = new WeakReference(p);
                            for (FileObject fldr : folders) {
                                projectCache.put(fldr, rp);
                            }
                        }
                        return p;
                    }
                }
            }
            if (hasRoot(externalOwners.keySet(), f, folder, furi)) {
                URI externalOwnersURI = externalOwners.get(furi[0]);

                if (externalOwnersURI != null) {
                    if (externalOwnersURI == UNOWNED_URI) {
                        return FileOwnerQuery.UNOWNED;
                    }
                    FileObject externalOwner = uri2FileObject(externalOwnersURI);

                    if (externalOwner != null && externalOwner.isValid()) {
                        try {
                            // Note: will be null if there is no such project.
                            Project p = ProjectManager.getDefault().findProject(externalOwner);
                            if (p != null) {
                                synchronized (cacheLock) {
                                    projectCache.put(f, new WeakReference<Project>(p));
                                }
                            }
                            return p;
                        } catch (IOException e) {
                            // There is a project there, but we cannot load it...
                            LOG.log(Level.FINE, "Cannot load project.", e); //NOI18N
                            return null;
                        }
                    }
                }
            }
            if (hasRoot(deserializedExternalOwners.keySet(), f, folder, furi)) {
                FileObject externalOwner = deserializedExternalOwners.get(furi[0]);
                if (externalOwner != null && externalOwner.isValid()) {
                    try {
                        // Note: will be null if there is no such project.
                        Project p = ProjectManager.getDefault().findProject(externalOwner);
                        if (p != null) {
                            synchronized (cacheLock) {
                                projectCache.put(f, new WeakReference<Project>(p));
                            }
                        }
                        return p;
                    } catch (IOException e) {
                        // There is a project there, but we cannot load it...
                        LOG.log(Level.FINE, "Cannot load project.", e); //NOI18N
                        return null;
                    }
                }
            }

            f = f.getParent();
        }
        return null;
    }


    private static boolean hasRoot(
            @NonNull final Set<URI> extRoots,
            @NonNull final FileObject file,
            final boolean folder,
            @NonNull final URI[] furi) {
        if (extRoots.isEmpty() || !(folder || externalRootsIncludeNonFolders)) {
            return false;
        }
        furi[0] = file.toURI();
        return extRoots.contains(furi[0]);
    }
    /**
     * Map from external source roots to the owning project directories.
     */
    private static final Map<URI,URI> externalOwners =
        Collections.synchronizedMap(new HashMap<URI,URI>());
    
    private static final Map<URI,FileObject> deserializedExternalOwners =
        Collections.synchronizedMap(new HashMap<URI,FileObject>());

    private static boolean externalRootsIncludeNonFolders = false;

    private static final Object cacheLock = new Object();
    private static volatile boolean cacheInvalid = false;

    private static enum ExternalRootsState {
        NEW,
        LOADING,
        LOADED
    }

    //@GuardedBy("SimpleFileOwnerQueryImplementation.class")
    private static ExternalRootsState externalRootsState = ExternalRootsState.NEW;
    /**
     * Deserializes stored cross-reference of external files to their projects.
     * It is called from @OnStart, which runs asynchronously/in parallel, but 
     * getOwner() queries require that the cross-ref is laoded so the answers are
     * consistent in time.
     */
    static void deserialize() {
        boolean needsToLoad = false;
        synchronized (SimpleFileOwnerQueryImplementation.class) {
            LOG.log(Level.FINEST, "External Roots State: {0}", externalRootsState); //NOI18N    - unit tests
            switch (externalRootsState) {
                case NEW:
                    externalRootsState = ExternalRootsState.LOADING;
                    needsToLoad = true;
                    break;
                case LOADING:
                    while (externalRootsState == ExternalRootsState.LOADING) {
                        try {
                            SimpleFileOwnerQueryImplementation.class.wait();
                        } catch (InterruptedException ie) {
                            LOG.log(Level.INFO, null, ie);
                            break;
                        }
                    }
                    break;
                case LOADED:
                    break;
                default:
                    throw new IllegalStateException(String.format(
                        "Unknown external roots state: %s",    //NOI18N
                        externalRootsState));
            }
        }
        if (needsToLoad) {
            try {
                deserializeImpl();
                LOG.log(Level.FINEST, "External Roots Deserialized"); //NOI18N    - unit tests
            } finally {
                synchronized (SimpleFileOwnerQueryImplementation.class) {
                    assert externalRootsState == ExternalRootsState.LOADING;
                    externalRootsState = ExternalRootsState.LOADED;
                    SimpleFileOwnerQueryImplementation.class.notifyAll();
                }
            }
        }
    }

    private static void deserializeImpl() {
        try {
            Preferences p = NbPreferences.forModule(SimpleFileOwnerQueryImplementation.class).node("externalOwners");
            for (String name : p.keys()) {
                URL u = new URL(p.get(name, null));
                URI i = new URI(name);
                deserializedExternalOwners.put(i, URLMapper.findFileObject(u));
            }
        } catch (Exception ex) {
            LOG.log(Level.INFO, null, ex);
        }
        try {
            NbPreferences.forModule(SimpleFileOwnerQueryImplementation.class).node("externalOwners").removeNode();
        } catch (BackingStoreException ex) {
            LOG.log(Level.INFO, null, ex);
        }
    }

    static void serialize() {
        try {
            Preferences p = NbPreferences.forModule(SimpleFileOwnerQueryImplementation.class).node("externalOwners");
            for (URI uri : externalOwners.keySet()) {
                URI ownerURI = externalOwners.get(uri);
                if (ownerURI != UNOWNED_URI) {
                p.put(uri.toString(), ownerURI.toString());
            }
            }
            p.sync(); // #184310
        } catch (Exception ex) {
            LOG.log(Level.WARNING, null, ex);
        }
        
    }
    
    /** @see FileOwnerQuery#reset */
    public static void reset() {
        synchronized (cacheLock) {
            cacheInvalid = true;
            externalOwners.clear();
        }
    }
    
    /** @see FileOwnerQuery#markExternalOwner */
    public static void markExternalOwnerTransient(FileObject root, Project owner) {
        markExternalOwnerTransient(root.toURI(), owner);
    }
    
    /** @see FileOwnerQuery#markExternalOwner */
    public static void markExternalOwnerTransient(URI root, Project owner) {
        externalRootsIncludeNonFolders |= !root.getPath().endsWith("/");
        if (owner != null) {
            FileObject fo = owner.getProjectDirectory();
            URI foUri = owner == FileOwnerQuery.UNOWNED ? UNOWNED_URI : fo.toURI();
            synchronized (cacheLock) {
                cacheInvalid = true;
                externalOwners.put(root, foUri);
                deserializedExternalOwners.remove(root);
            }
        } else {
            synchronized (cacheLock) {
                cacheInvalid = true;
                externalOwners.remove(root);
            }
        }
    }
    
    private static FileObject uri2FileObject(URI u) {
        URL url;
        try {
            url = u.toURL();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            assert false : u;
            return null;
        }
        return URLMapper.findFileObject(url);
    }
    
    private static URI goUp(URI u) {
        assert u.isAbsolute() : u;
        assert u.getFragment() == null : u;
        assert u.getQuery() == null : u;
        // XXX isn't there any easier way to do this?
        // Using getPath in the new path does not work; nbfs: URLs break. (#39613)
        // On the other hand, nbfs: URLs are not really used any more, so do we care?
        String path = u.getPath();
        if (path == null || path.equals("/")) { // NOI18N
            return null;
        }
        String us = u.toString();
        if (us.endsWith("/")) { // NOI18N
            us = us.substring(0, us.length() - 1);
            assert path.endsWith("/"); // NOI18N
            path = path.substring(0, path.length() - 1);
        }
        int idx = us.lastIndexOf('/');
        assert idx != -1 : path;
        if (path.lastIndexOf('/') == 0) {
            us = us.substring(0, idx + 1);
        } else {
            us = us.substring(0, idx);
        }
        URI nue;
        try {
            nue = new URI(us);
        } catch (URISyntaxException e) {
            throw new AssertionError(e);
        }
        if (WINDOWS) {
            String pth = nue.getPath();
            // check that path is not "/C:" or "/"
            if ((pth.length() == 3 && pth.endsWith(":")) ||
                (pth.length() == 1 && pth.endsWith("/"))) {
                return null;
            }
        }
        assert nue.isAbsolute() : nue;
        assert u.toString().startsWith(nue.toString()) : "not a parent: " + nue + " of " + u;
        return nue;
    }
    private static final boolean WINDOWS = BaseUtilities.isWindows();
    
}
