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

package org.netbeans.modules.parsing.impl.indexing;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.classpath.GlobalPathRegistryEvent;
import org.netbeans.api.java.classpath.GlobalPathRegistryListener;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;
import org.openide.util.BaseUtilities;
import org.openide.util.WeakListeners;

/**
 *
 * @author Tomas Zezula
 */
public final class PathRegistry implements Runnable {

    private static final boolean FIRE_UNKNOWN_ALWAYS = false;

    // property set/field used in tests
    static final int FIRER_EVT_COLLAPSE_WINDOW = Integer.getInteger(PathRegistry.class.getName()+".FIRER_EVT_COLLAPSE_WINDOW", 500);

    private static PathRegistry instance;
    private static final RequestProcessor firer = new RequestProcessor ("Path Registry Request Processor"); //NOI18N
    private static final RequestProcessor openProjectChange = new RequestProcessor("Waiting for project loading");  //NOI18N

    // -J-Dorg.netbeans.modules.parsing.impl.indexing.PathRegistry.level=FINE
    private static final Logger LOGGER = Logger.getLogger(PathRegistry.class.getName());
    private static final Set<String> FAST_HOST_PROTOCOLS = new HashSet<>(Arrays.asList("file", "nbfs", "rfs", "memory"));   //NOI18N
    private static final String DIGEST_ALGORITHM = "SHA1";  //NOI18N
    private static final boolean[] BOOLOUT = new boolean[1];

    private final RequestProcessor.Task firerTask;
    private final RequestProcessor.Task openProjectChangeTask;
    private final GlobalPathRegistry regs;
    private final OpenProjects openProjects;
    private final List<PathRegistryEvent.Change> changes = new LinkedList<>();

    private Map<ClassPath,byte[]> activeCps;
    @org.netbeans.api.annotations.common.SuppressWarnings(
        value="DMI_COLLECTION_OF_URLS",
        justification="URLs have never host part")
    private Map<URL, SourceForBinaryQuery.Result2> sourceResults;
    @org.netbeans.api.annotations.common.SuppressWarnings(
        value="DMI_COLLECTION_OF_URLS",
        justification="URLs have never host part")
    private Map<URL, URL[]> translatedRoots;
    @org.netbeans.api.annotations.common.SuppressWarnings(
        value="DMI_COLLECTION_OF_URLS",
        justification="URLs have never host part")
    private Map<URL, WeakValue> unknownRoots;
    private long timeStamp;             //Lamport event ordering
    private volatile Runnable debugCallBack;
    private Collection<URL>  sourcePaths;
    private Collection<URL> libraryPath;
    private Collection<URL> binaryLibraryPath;
    private Collection<URL> unknownSourcePath;
    @org.netbeans.api.annotations.common.SuppressWarnings(
        value="DMI_COLLECTION_OF_URLS",
        justification="URLs have never host part")
    private Map<URL, PathIds> rootPathIds;
    @org.netbeans.api.annotations.common.SuppressWarnings(
        value="DMI_COLLECTION_OF_URLS",
        justification="URLs have never host part")
    private Map<String, Set<URL>> pathIdToRoots;

    private final Listener listener;
    private final List<PathRegistryListener> listeners;

    // @GuardedBy(this);
    private LogContext logCtx;

    private volatile boolean firstProjectOpened;

    @SuppressWarnings("LeakingThisInConstructor")
    private  PathRegistry () {
        regs = GlobalPathRegistry.getDefault();
        assert regs != null;
        this.listener = new Listener ();
        this.firerTask = firer.create(this, true);
        this.openProjectChangeTask = openProjectChange.create(listener);
        this.timeStamp = -1;
        this.activeCps = Collections.emptyMap();
        this.sourceResults = Collections.emptyMap();
        this.unknownRoots = new HashMap<>();
        this.translatedRoots = new HashMap<>();
        this.listeners = new CopyOnWriteArrayList<>();
        this.regs.addGlobalPathRegistryListener (WeakListeners.create(GlobalPathRegistryListener.class,this.listener,this.regs));
        openProjects = OpenProjects.getDefault();
        openProjects.addPropertyChangeListener(WeakListeners.propertyChange(listener, openProjects));
    }

    public static synchronized PathRegistry getDefault () {
        if (instance == null) {
            instance = new PathRegistry();
        }
        return instance;
    }

    void setDebugCallBack (final Runnable r) {
        this.debugCallBack = r;
    }

    public void addPathRegistryListener (final PathRegistryListener listener) {
        assert listener != null;
        this.listeners.add(listener);
    }

    public void removePathRegistryListener (final PathRegistryListener listener) {
        assert listener != null;
        this.listeners.remove(listener);
    }

    @org.netbeans.api.annotations.common.SuppressWarnings(
        value="DMI_COLLECTION_OF_URLS",
        justification="URLs have never host part")
    public URL[] sourceForBinaryQuery (final URL binaryRoot, final ClassPath definingClassPath, final boolean fire) {
        return sourceForBinaryQuery(binaryRoot, definingClassPath, fire, BOOLOUT);
    }

    @SuppressWarnings("ValueOfIncrementOrDecrementUsed")
    URL[] sourceForBinaryQuery (
        @NonNull final URL binaryRoot,
        @NullAllowed final ClassPath definingClassPath,
        final boolean fire,
        /*@Out*/ @NonNull final boolean[] newRoot) {
        assert noHostPart(binaryRoot) : binaryRoot;
        newRoot[0] = false;
        URL[] result = this.translatedRoots.get(binaryRoot);
        if (result != null) {
            if (result.length > 0) {
                return result;
            }
            else {
                return null;
            }
        } else if (definingClassPath != null) {
            List<URL> cacheRoots = new ArrayList<>();
            Collection<? extends URL> unknownRes;
            try {
                unknownRes = getSources(SourceForBinaryQuery.findSourceRoots2(binaryRoot),cacheRoots,null);
            } catch (IllegalArgumentException iae) {
                throw new IllegalArgumentException("Defining cp URLs: " + definingClassPath.entries(), iae); //NOI18N
            }
            if (unknownRes.isEmpty()) {
                return null;
            } else {
                result = new URL[unknownRes.size()];
                synchronized (this) {
                    int i = 0;
                    for (URL u : unknownRes) {
                        result[i++] = u;
                        if (!unknownRoots.containsKey(u)) {
                            unknownRoots.put(u,new WeakValue(definingClassPath,u));
                            newRoot[0] = true;
                        }
                    }
                }
                if (FIRE_UNKNOWN_ALWAYS && fire) {
                    this.resetCacheAndFire(EventKind.PATHS_CHANGED, PathKind.UNKNOWN_SOURCE, null, Collections.singleton(definingClassPath));
                }
                return result;
            }
        } else {
            return null;
        }
    }

    /**
     * Registers unknown source path, for example non open project visited by favorities.
     */
    public void registerUnknownSourceRoots (final ClassPath owner, final Iterable<? extends URL> roots) {
        assert owner != null;
        assert roots != null;
        synchronized (this) {
            for (URL root : roots) {
                unknownRoots.put(root,new WeakValue(owner,root));
            }
            unknownSourcePath = new HashSet<>(unknownRoots.keySet());
            changes.add(new PathRegistryEvent.Change(EventKind.PATHS_ADDED,
                    PathKind.UNKNOWN_SOURCE,
                    null,
                    Collections.singleton(owner)));
        }
        if (LOGGER.isLoggable(Level.FINE)) {
            List<URL> l = new LinkedList<>();
            for(URL r : roots) {
                l.add(r);
            }
            LOGGER.log(Level.FINE, "registerUnknownSourceRoots: {0}", l); // NOI18N
        }
        scheduleFirer(roots);
    }

    public void unregisterUnknownSourceRoots(@NonNull Iterable<? extends URL> roots) {
        Parameters.notNull("roots", roots); //NOI18N
        synchronized (this) {
            for (URL root : roots) {
                unknownRoots.remove(root);
                if (unknownSourcePath != null) {
                    unknownSourcePath.remove(root);
                }
            }
        }
    }

    public Collection<? extends URL> getSources () {
        Request request;
        synchronized (this) {
            if (this.sourcePaths != null) {
                return Collections.unmodifiableCollection(this.sourcePaths);
            }
            LOGGER.fine("Computing data due to getSources"); //NOI18N
            request = new Request (
                getTimeStamp(),
                getSourcePaths(),
                getLibraryPaths(),
                getBinaryLibraryPaths(),
                new HashMap<>(this.activeCps),
                new HashMap<>(this.sourceResults),
                new HashMap<>(this.unknownRoots),
                this.listener,
                this.listener);
        }
        final Result res = createResources (request);
        if (this.debugCallBack != null) {
            this.debugCallBack.run();
        }
        synchronized (this) {
            if (getTimeStamp() == res.timeStamp) {
                if (this.sourcePaths == null) {
                    this.sourcePaths = res.sourcePath;
                    this.libraryPath = res.libraryPath;
                    this.binaryLibraryPath = res.binaryLibraryPath;
                    this.unknownSourcePath = res.unknownSourcePath;
                    this.activeCps = res.newCps;
                    this.sourceResults = res.newSR;
                    this.translatedRoots = res.translatedRoots;
                    this.unknownRoots = res.unknownRoots;
                    this.rootPathIds = res.rootPathIds;
                    this.pathIdToRoots = res.pathIdToRoots;
                }
                return Collections.unmodifiableCollection(this.sourcePaths);
            } else {
                return res.sourcePath;
            }
        }
    }

    public Collection<? extends URL> getLibraries () {
        Request request;
        synchronized (this) {
            if (this.libraryPath != null) {
                return Collections.unmodifiableCollection(this.libraryPath);
            }
            LOGGER.fine("Computing data due to getLibraries"); //NOI18N
            request = new Request (
                this.getTimeStamp(),
                getSourcePaths(),
                getLibraryPaths(),
                getBinaryLibraryPaths(),
                new HashMap<>(this.activeCps),
                new HashMap<>(this.sourceResults),
                new HashMap<>(this.unknownRoots),
                this.listener,
                this.listener);
        }
        final Result res = createResources (request);
        if (this.debugCallBack != null) {
            this.debugCallBack.run();
        }
        synchronized (this) {
            if (this.getTimeStamp() == res.timeStamp) {
                if (this.libraryPath == null) {
                    this.sourcePaths = res.sourcePath;
                    this.libraryPath = res.libraryPath;
                    this.binaryLibraryPath = res.binaryLibraryPath;
                    this.unknownSourcePath = res.unknownSourcePath;
                    this.activeCps = res.newCps;
                    this.sourceResults = res.newSR;
                    this.translatedRoots = res.translatedRoots;
                    this.unknownRoots = res.unknownRoots;
                    this.rootPathIds = res.rootPathIds;
                    this.pathIdToRoots = res.pathIdToRoots;
                }
                return Collections.unmodifiableCollection(this.libraryPath);
            } else {
                return res.libraryPath;
            }
        }
    }

    public Collection<? extends URL> getBinaryLibraries () {
        Request request;
        synchronized (this) {
            if (this.binaryLibraryPath != null) {
                return Collections.unmodifiableCollection(this.binaryLibraryPath);
            }
            LOGGER.fine("Computing data due to getBinaryLibraries"); //NOI18N
            request = new Request (
                this.getTimeStamp(),
                getSourcePaths(),
                getLibraryPaths(),
                getBinaryLibraryPaths(),
                new HashMap<>(this.activeCps),
                new HashMap<>(this.sourceResults),
                new HashMap<>(this.unknownRoots),
                this.listener,
                this.listener);
        }
        final Result res = createResources (request);
        if (this.debugCallBack != null) {
            this.debugCallBack.run();
        }
        synchronized (this) {
            if (this.getTimeStamp() == res.timeStamp) {
                if (this.binaryLibraryPath == null) {
                    this.sourcePaths = res.sourcePath;
                    this.libraryPath = res.libraryPath;
                    this.binaryLibraryPath = res.binaryLibraryPath;
                    this.unknownSourcePath = res.unknownSourcePath;
                    this.activeCps = res.newCps;
                    this.sourceResults = res.newSR;
                    this.translatedRoots = res.translatedRoots;
                    this.unknownRoots = res.unknownRoots;
                    this.rootPathIds = res.rootPathIds;
                    this.pathIdToRoots = res.pathIdToRoots;
                }
                return Collections.unmodifiableCollection(this.binaryLibraryPath);
            } else {
                return res.binaryLibraryPath;
            }
        }
    }

    public Collection<? extends URL> getUnknownRoots () {
        Request request;
        synchronized (this) {
            if (this.unknownSourcePath != null) {
                return Collections.unmodifiableCollection(unknownSourcePath);
            }
            LOGGER.fine("Computing data due to getUnknownRoots"); //NOI18N
            request = new Request (
                getTimeStamp(),
                getSourcePaths(),
                getLibraryPaths(),
                getBinaryLibraryPaths(),
                new HashMap<>(this.activeCps),
                new HashMap<>(this.sourceResults),
                new HashMap<>(this.unknownRoots),
                this.listener,
                this.listener);
        }
        final Result res = createResources (request);
        if (this.debugCallBack != null) {
            this.debugCallBack.run();
        }
        synchronized (this) {
            if (getTimeStamp() == res.timeStamp) {
                if (unknownSourcePath == null) {
                    this.sourcePaths = res.sourcePath;
                    this.libraryPath = res.libraryPath;
                    this.binaryLibraryPath = res.binaryLibraryPath;
                    this.unknownSourcePath = res.unknownSourcePath;
                    this.activeCps = res.newCps;
                    this.sourceResults = res.newSR;
                    this.translatedRoots = res.translatedRoots;
                    this.unknownRoots = res.unknownRoots;
                    this.rootPathIds = res.rootPathIds;
                    this.pathIdToRoots = res.pathIdToRoots;
                }
                return Collections.unmodifiableCollection(this.unknownSourcePath);
            } else {
                return res.unknownSourcePath;
            }
        }
    }

    public boolean isKnownRoot(URL root) {
        synchronized(this) {
            return (rootPathIds != null && rootPathIds.containsKey(root)) ||
                    (unknownSourcePath != null && unknownSourcePath.contains(root));
        }
    }

    @org.netbeans.api.annotations.common.SuppressWarnings(
        value="DMI_COLLECTION_OF_URLS",
        justification="URLs have never host part")
    public Set<String> getSourceIdsFor(URL root) {
        assert noHostPart(root) : root;
        PathIds pathIds = getRootPathIds().get(root);
        return pathIds != null ? pathIds.getSids() : null;
    }

    @org.netbeans.api.annotations.common.SuppressWarnings(
        value="DMI_COLLECTION_OF_URLS",
        justification="URLs have never host part")
    public Set<String> getLibraryIdsFor(URL root) {
        assert noHostPart(root) : root;
        PathIds pathIds = getRootPathIds().get(root);
        return pathIds != null ? pathIds.getLids() : null;
    }

    @org.netbeans.api.annotations.common.SuppressWarnings(
        value="DMI_COLLECTION_OF_URLS",
        justification="URLs have never host part")
    public Set<URL> getRootsMarkedAs(String... pathIds) {
        final Map<String, Set<URL>> rootsByPathIds = getPathIdToRoots();
        final Set<URL> roots = new HashSet<>();
        for(String id : pathIds) {
            Set<URL> idRoots = rootsByPathIds.get(id);
            if (idRoots != null) {
                roots.addAll(idRoots);
            }
        }
        return roots;
    }

    @org.netbeans.api.annotations.common.SuppressWarnings(
        value="DMI_COLLECTION_OF_URLS",
        justification="URLs have never host part")
    public Set<String> getMimeTypesFor(final URL root) {
        assert noHostPart(root) : root;
        PathIds pathIds = getRootPathIds().get(root);
        return pathIds != null ? pathIds.getMimeTypes() : null;
    }

    /**
     * If all the source path events where fired (task is finished) return true,
     * otherwise false. (Means there is waiting event to be fired.)
     *
     * @return true when there is no waiting event, otherwise false
     */
    public boolean isFinished() {
       return firerTask.isFinished();
    }

    @Override
    public void run () {
        assert firer.isRequestProcessorThread();
        awaitProjectsOpen();
        Iterable<? extends PathRegistryEvent.Change> ch;
        LogContext ctx;

        synchronized (this) {
            ch = new ArrayList<>(this.changes);
            ctx = this.logCtx;
            this.logCtx = null;
            this.changes.clear();
        }
        fire(ch, ctx);
        LOGGER.log(Level.FINE, "resetCacheAndFire, firing done"); // NOI18N
    }

    public static boolean noHostPart(@NonNull final URL url) {
        return url.getHost() == null || url.getHost().isEmpty() ?
            true:
            FAST_HOST_PROTOCOLS.contains(
                "jar".equals(url.getProtocol()) ?   //NOI18N
                FileUtil.getArchiveFile(url).getProtocol():
                url.getProtocol());
    }

    static boolean isIncompleteClassPath(@NonNull final ClassPath cp) {
        return cp.getFlags().contains(ClassPath.Flag.INCOMPLETE);
    }

    @SuppressWarnings("NestedAssignment")
    boolean isIncompleteRoot(@NonNull final FileObject root) {
        Set<String> sourceIds;
        Set<String> libIds;
        Set<String> binLibIds;

        if ((sourceIds = getSourceIdsFor(root.toURL())) != null && !sourceIds.isEmpty()) {
            libIds = new HashSet<>();
            binLibIds = new HashSet<>();
            for(String id : sourceIds) {
                libIds.addAll(PathRecognizerRegistry.getDefault().getLibraryIdsForSourceId(id));
                binLibIds.addAll(PathRecognizerRegistry.getDefault().getBinaryLibraryIdsForSourceId(id));
            }
        } else if ((libIds = getLibraryIdsFor(root.toURL())) != null && !libIds.isEmpty()) {
            sourceIds = Collections.emptySet();
            binLibIds = new HashSet<>();
            for(String id : libIds) {
                binLibIds.addAll(PathRecognizerRegistry.getDefault().getBinaryLibraryIdsForLibraryId(id));
            }
        } else {
            sourceIds = PathRecognizerRegistry.getDefault().getSourceIds();
            libIds = new HashSet<>();
            binLibIds = new HashSet<>();
            for(String id : sourceIds) {
                libIds.addAll(PathRecognizerRegistry.getDefault().getLibraryIdsForSourceId(id));
                binLibIds.addAll(PathRecognizerRegistry.getDefault().getBinaryLibraryIdsForSourceId(id));
            }
        }
        assert sourceIds != null;
        assert libIds != null;
        assert binLibIds != null;
        return isIncompleteClassPath(root, binLibIds) ||
           isIncompleteClassPath(root, libIds) ||
           isIncompleteClassPath(root, sourceIds);
    }

    boolean isIncompleteRoot(@NonNull final URL root) {
        final FileObject rootFo = URLMapper.findFileObject(root);
        return rootFo == null ?
            false :
            isIncompleteRoot(rootFo);
    }

    @org.netbeans.api.annotations.common.SuppressWarnings(
        value="DMI_COLLECTION_OF_URLS",
        justification="URLs have never host part")
    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    private Map<URL, PathIds> getRootPathIds () {
        Request request;
        synchronized (this) {
            if (this.rootPathIds != null) {
                return rootPathIds;
            }
            LOGGER.fine("Computing data due to getRootPathIds"); //NOI18N
            request = new Request (
                getTimeStamp(),
                getSourcePaths(),
                getLibraryPaths(),
                getBinaryLibraryPaths(),
                new HashMap<>(this.activeCps),
                new HashMap<>(this.sourceResults),
                new HashMap<>(this.unknownRoots),
                this.listener,
                this.listener);
        }
        final Result res = createResources (request);
        if (this.debugCallBack != null) {
            this.debugCallBack.run();
        }
        synchronized (this) {
            if (getTimeStamp() == res.timeStamp) {
                if (rootPathIds == null) {
                    this.sourcePaths = res.sourcePath;
                    this.libraryPath = res.libraryPath;
                    this.binaryLibraryPath = res.binaryLibraryPath;
                    this.unknownSourcePath = res.unknownSourcePath;
                    this.activeCps = res.newCps;
                    this.sourceResults = res.newSR;
                    this.translatedRoots = res.translatedRoots;
                    this.unknownRoots = res.unknownRoots;
                    this.rootPathIds = res.rootPathIds;
                    this.pathIdToRoots = res.pathIdToRoots;
                }
                return this.rootPathIds;
            }
            else {
                return res.rootPathIds;
            }
        }
    }

    @org.netbeans.api.annotations.common.SuppressWarnings(
        value="DMI_COLLECTION_OF_URLS",
        justification="URLs have never host part")
    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    private Map<String, Set<URL>> getPathIdToRoots () {
        Request request;
        synchronized (this) {
            if (this.pathIdToRoots != null) {
                return pathIdToRoots;
            }
            LOGGER.fine("Computing data due to getPathIdToRoots"); //NOI18N
            request = new Request (
                getTimeStamp(),
                getSourcePaths(),
                getLibraryPaths(),
                getBinaryLibraryPaths(),
                new HashMap<>(this.activeCps),
                new HashMap<>(this.sourceResults),
                new HashMap<>(this.unknownRoots),
                this.listener,
                this.listener);
        }
        final Result res = createResources (request);
        if (this.debugCallBack != null) {
            this.debugCallBack.run();
        }
        synchronized (this) {
            if (getTimeStamp() == res.timeStamp) {
                if (pathIdToRoots == null) {
                    this.sourcePaths = res.sourcePath;
                    this.libraryPath = res.libraryPath;
                    this.binaryLibraryPath = res.binaryLibraryPath;
                    this.unknownSourcePath = res.unknownSourcePath;
                    this.activeCps = res.newCps;
                    this.sourceResults = res.newSR;
                    this.translatedRoots = res.translatedRoots;
                    this.unknownRoots = res.unknownRoots;
                    this.rootPathIds = res.rootPathIds;
                    this.pathIdToRoots = res.pathIdToRoots;
                }
                return this.pathIdToRoots;
            }
            else {
                return res.pathIdToRoots;
            }
        }
    }

    private boolean classPathChanged(@NonNull final ClassPath cp) {
        final byte[] oldHash;
        synchronized (this) {
            oldHash = this.activeCps.get(cp);
        }
        if (oldHash == null) {
            return true;
        }
        return !Arrays.equals(
            oldHash,
            toDigest(cp, createDigest()));
    }

    @org.netbeans.api.annotations.common.SuppressWarnings(
        value="DMI_COLLECTION_OF_URLS",
        justification="URLs have never host part")
    private static Result createResources (final Request request) {
        assert request != null;
        final Set<URL> sourceResult = new HashSet<>();
        final Set<URL> unknownResult = new HashSet<>();
        final Set<URL> libraryResult = new HashSet<>();
        final Set<URL> binaryLibraryResult = new HashSet<>();
        final Map<URL,URL[]> translatedRoots = new HashMap<>();
        final Map<ClassPath,byte[]> newCps = new HashMap<>();
        final Map<URL,SourceForBinaryQuery.Result2> newSR = new HashMap<>();
        final Map<URL, PathIds> pathIdsResult = new HashMap<>();
        final Map<String, Set<URL>> pathIdToRootsResult = new HashMap<>();
        final MessageDigest digest = createDigest();

        for (TaggedClassPath tcp : request.sourceCps) {
            ClassPath cp = tcp.getClasspath();
            boolean isNew = request.oldCps.remove(cp) == null;
            for (ClassPath.Entry entry : cp.entries()) {
                URL root = entry.getURL();
                assert noHostPart(root) : root;
                sourceResult.add(root);
                updatePathIds(root, tcp, pathIdsResult, pathIdToRootsResult);
            }
            boolean notContained = newCps.put (cp, toDigest(cp,digest)) == null;
            if (isNew && notContained) {
               cp.addPropertyChangeListener(request.propertyListener);
            }
        }

        for (TaggedClassPath tcp : request.libraryCps) {
            ClassPath cp = tcp.getClasspath();
            boolean isNew = request.oldCps.remove(cp) == null;
            for (ClassPath.Entry entry : cp.entries()) {
                URL root = entry.getURL();
                assert noHostPart(root) : root;
                libraryResult.add(root);
                updatePathIds(root, tcp, pathIdsResult, pathIdToRootsResult);
            }
            boolean notContained = newCps.put (cp, toDigest(cp, digest)) == null;
            if (isNew && notContained) {
               cp.addPropertyChangeListener(request.propertyListener);
            }
        }

        for (TaggedClassPath tcp : request.binaryLibraryCps) {
            ClassPath cp = tcp.getClasspath();
            boolean isNew = request.oldCps.remove(cp) == null;
            for (ClassPath.Entry entry : cp.entries()) {
                URL binRoot = entry.getURL();
                assert noHostPart(binRoot) : binRoot;
                if (!translatedRoots.containsKey(binRoot)) {
                    updatePathIds(binRoot, tcp, pathIdsResult, pathIdToRootsResult);

                    SourceForBinaryQuery.Result2 sr = request.oldSR.remove (binRoot);
                    boolean isNewSR;
                    if (sr == null) {
                        sr = SourceForBinaryQuery.findSourceRoots2(binRoot);
                        isNewSR = true;
                    }
                    else {
                        isNewSR = false;
                    }
                    assert !newSR.containsKey(binRoot);
                    newSR.put(binRoot,sr);
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.log(Level.FINE, "{0}: preferSources={1}", new Object[] { binRoot, sr.preferSources() }); //NOI18N
                    }
                    final Set<URL> cacheURLs = new LinkedHashSet<>(); //LinkedSet to protect against wrong SFBQ but keep ordering
                    final Collection<? extends URL> srcRoots = getSources(sr, cacheURLs, request.unknownRoots);
                    if (srcRoots.isEmpty()) {
                        binaryLibraryResult.add(binRoot);
                    } else {
                        libraryResult.addAll(srcRoots);
                        updateTranslatedPathIds(srcRoots, tcp, pathIdsResult, pathIdToRootsResult);
                    }
                    translatedRoots.put(binRoot, cacheURLs.toArray(new URL[0]));
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.log(Level.FINE, "T: {0} -> {1}", new Object[]{binRoot, cacheURLs}); //NOI18N
                    }

                    if (isNewSR) {
                        sr.addChangeListener(request.changeListener);
                    }
                }
            }
            boolean notContained = newCps.put (cp, toDigest(cp, digest)) == null;
            if (isNew && notContained) {
                cp.addPropertyChangeListener(request.propertyListener);
            }
        }

        for (ClassPath cp : request.oldCps.keySet()) {
            cp.removePropertyChangeListener(request.propertyListener);
        }

        for (Map.Entry<URL,SourceForBinaryQuery.Result2> entry : request.oldSR.entrySet()) {
            entry.getValue().removeChangeListener(request.changeListener);
        }
        unknownResult.addAll(request.unknownRoots.keySet());

        return new Result (request.timeStamp, sourceResult, libraryResult, binaryLibraryResult, unknownResult,
                newCps, newSR, translatedRoots, request.unknownRoots, pathIdsResult, pathIdToRootsResult);
    }

    @org.netbeans.api.annotations.common.SuppressWarnings(
        value="DMI_COLLECTION_OF_URLS",
        justification="URLs have never host part")
    private static Collection <? extends URL> getSources (final SourceForBinaryQuery.Result2 sr, final Collection<? super URL> cacheDirs, final Map<URL, WeakValue> unknownRoots) {
        assert sr != null;
        if (sr.preferSources()) {
            final FileObject[] roots = sr.getRoots();
            assert roots != null;
            List<URL> result = new ArrayList<>(roots.length);
            for (int i=0; i<roots.length; i++) {
                final URL url = roots[i].toURL();
                assert noHostPart(url) : url;
                if (cacheDirs != null) {
                    cacheDirs.add (url);
                }
                if (unknownRoots != null) {
                    unknownRoots.remove (url);
                }
                result.add(url);
            }
            return result;
        }
        else {
            return Collections.<URL>emptySet();
        }
    }

    @org.netbeans.api.annotations.common.SuppressWarnings(
        value="DMI_COLLECTION_OF_URLS",
        justification="URLs have never host part")
    private static void updatePathIds(URL root, TaggedClassPath tcp, Map<URL, PathIds> pathIdsResult, Map<String, Set<URL>> pathIdToRootsResult) {
        PathIds pathIds = pathIdsResult.get(root);
        if (pathIds == null) {
            pathIds = new PathIds();
            pathIdsResult.put(root, pathIds);
        }
        pathIds.addAll(tcp.getPathIds());

        for(String id : tcp.getPathIds().getAllIds()) {
            Set<URL> rootsWithId = pathIdToRootsResult.get(id);
            if (rootsWithId == null) {
                rootsWithId = new HashSet<>();
                pathIdToRootsResult.put(id, rootsWithId);
            }
            rootsWithId.add(root);
        }

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "Root {0} associated with {1}", new Object [] { root, tcp.getPathIds() });
        }
    }

    @org.netbeans.api.annotations.common.SuppressWarnings(
        value="DMI_COLLECTION_OF_URLS",
        justification="URLs have never host part")
    private static void updateTranslatedPathIds(Collection<? extends URL> roots, TaggedClassPath tcp, Map<URL, PathIds> pathIdsResult, Map<String, Set<URL>> pathIdToRootsResult) {
        Set<String> sids = new HashSet<>();
        Set<String> mimeTypes = new HashSet<>();
        for(String blid : tcp.getPathIds().getBlids()) {
            Set<String> ids = PathRecognizerRegistry.getDefault().getSourceIdsForBinaryLibraryId(blid);
            if (ids != null) {
                sids.addAll(ids);
            }
            Set<String> mts = PathRecognizerRegistry.getDefault().getMimeTypesForBinaryLibraryId(blid);
            if (mts != null) {
                mimeTypes.addAll(mts);
            }
        }
        for(URL root : roots) {
            PathIds pathIds = pathIdsResult.get(root);
            if (pathIds == null) {
                pathIds = new PathIds();
                pathIdsResult.put(root, pathIds);
            }
            pathIds.getSids().addAll(sids);
            pathIds.getMimeTypes().addAll(mimeTypes);

            for(String id : sids) {
                Set<URL> rootsWithId = pathIdToRootsResult.get(id);
                if (rootsWithId == null) {
                    rootsWithId = new HashSet<>();
                    pathIdToRootsResult.put(id, rootsWithId);
                }
                rootsWithId.add(root);
            }

            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "Root {0} associated with {1}", new Object [] { root, tcp.getPathIds() });
            }
        }
    }

    @NonNull
    private static byte[] toDigest(
        @NonNull final ClassPath cp,
        @NonNull final MessageDigest md) {
        final StringBuilder sb = new StringBuilder();
        for (ClassPath.Flag flag : cp.getFlags()) {
            sb.append(flag.name()).
                append(File.pathSeparatorChar);
        }
        for (ClassPath.Entry e : cp.entries()) {
            sb.append(e.getURL().toExternalForm()).
                append(File.pathSeparatorChar);
        }
        try {
            return md.digest(sb.toString().getBytes());
        } finally {
            md.reset();
        }
    }

    private static boolean isIncompleteClassPath(
        @NonNull final FileObject root,
        @NonNull final Set<String> classPathTypes) {
        for (String classPathType : classPathTypes) {
            final ClassPath cp = ClassPath.getClassPath(root, classPathType);
            if (cp != null && isIncompleteClassPath(cp)) {
                return true;
            }
        }
        return false;
    }

    @NonNull
    private static MessageDigest createDigest() {
        try {
            return MessageDigest.getInstance(DIGEST_ALGORITHM);
        } catch (NoSuchAlgorithmException ex) {
            return new IdentityDigest();
        }
    }

    private void resetCacheAndFire (
            @NonNull final EventKind eventKind,
            @NullAllowed final PathKind pathKind,
            @NullAllowed final String pathId,
            @NullAllowed final Set<? extends ClassPath> paths) {
        synchronized (this) {
            this.sourcePaths = null;
            this.libraryPath = null;
            this.binaryLibraryPath = null;
            this.unknownSourcePath = null;
            this.rootPathIds = null;
            this.pathIdToRoots = null;
            this.timeStamp++;
            this.changes.add(new PathRegistryEvent.Change(eventKind, pathKind, pathId, paths));
        }

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "resetCacheAndFire: eventKind={0}, pathKind={1}, pathId={2}, paths={3}",
                new Object [] { eventKind, pathKind, pathId, paths }); // NOI18N
        }
        scheduleFirer(paths);
    }

    private void scheduleFirer(Collection<? extends ClassPath> paths) {
        final LogContext _logCtx;
        synchronized (this) {
            if (logCtx == null) {
                logCtx = LogContext.create(LogContext.EventType.PATH, null);
            }
            _logCtx = logCtx;
        }
        assert _logCtx != null;
        _logCtx.addPaths(paths);
        scheduleImpl();
    }

    private void scheduleFirer(Iterable<? extends URL> roots) {
        final LogContext _logCtx;
        synchronized (this) {
            if (logCtx == null) {
                logCtx = LogContext.create(LogContext.EventType.PATH, null);
            }
            _logCtx = logCtx;
        }
        assert _logCtx != null;
        _logCtx.addRoots(roots);
        scheduleImpl();
    }

    private void scheduleImpl() {
        if (!firstProjectOpened) {
            firstProjectOpened = true;
        }
        firerTask.schedule(FIRER_EVT_COLLAPSE_WINDOW);
    }

    private void fire (final Iterable<? extends PathRegistryEvent.Change> changes, LogContext ctx) {
        final PathRegistryEvent event = new PathRegistryEvent(this, changes, ctx);
        for (PathRegistryListener l : listeners) {
            l.pathsChanged(event);
        }
    }

    @CheckForNull
    private PathKind getPathKind (@NonNull final String pathId) {
        Parameters.notNull("pathId", pathId);   //NOI18N
        final Set<String> sIds = PathRecognizerRegistry.getDefault().getSourceIds();
        if (sIds.contains(pathId)) {
            return PathKind.SOURCE;
        }
        final Set<String> lIds = PathRecognizerRegistry.getDefault().getLibraryIds();
        if (lIds.contains(pathId)) {
            return PathKind.LIBRARY;
        }
        final Set<String> bIds = PathRecognizerRegistry.getDefault().getBinaryLibraryIds();
        if (bIds.contains(pathId)) {
            return PathKind.BINARY_LIBRARY;
        }
        return null;
    }

    private Collection<TaggedClassPath> getSourcePaths () {
        return getPaths(PathKind.SOURCE);
    }

    private Collection<TaggedClassPath> getLibraryPaths () {
        return getPaths(PathKind.LIBRARY);
    }

    private Collection<TaggedClassPath> getBinaryLibraryPaths () {
        return getPaths(PathKind.BINARY_LIBRARY);
    }

    private Collection<TaggedClassPath> getPaths (final PathKind kind) {
        Set<String> ids;
        switch (kind) {
            case SOURCE:
                ids = PathRecognizerRegistry.getDefault().getSourceIds();
                break;
            case LIBRARY:
                ids = PathRecognizerRegistry.getDefault().getLibraryIds();
                break;
            case BINARY_LIBRARY:
                ids = PathRecognizerRegistry.getDefault().getBinaryLibraryIds();
                break;
            default:
                LOGGER.log(Level.WARNING, "Not expecting PathKind of {0}", kind); //NOI18N
                return Collections.<TaggedClassPath>emptySet();
        }

        Map<ClassPath, TaggedClassPath> result = new HashMap<>();   //Maybe caching, but should be called once per change
        for (String id : ids) {
            for(ClassPath cp : this.regs.getPaths(id)) {
                TaggedClassPath tcp = result.get(cp);
                if (tcp == null) {
                    tcp = new TaggedClassPath(cp);
                    result.put(cp, tcp);
                }
                switch (kind) {
                    case SOURCE:
                        tcp.associateWithSourceId(id);
                        tcp.associateWithMimeTypes(PathRecognizerRegistry.getDefault().getMimeTypesForSourceId(id));
                        break;
                    case LIBRARY:
                        tcp.associateWithLibraryId(id);
                        tcp.associateWithMimeTypes(PathRecognizerRegistry.getDefault().getMimeTypesForLibraryId(id));
                        break;
                    case BINARY_LIBRARY:
                        tcp.associateWithBinaryLibraryId(id); break;
                }
            }
        }
        return result.values();
    }

    private long getTimeStamp () {
        return this.timeStamp;
    }

    @SuppressWarnings("UseSpecificCatch")
    private void awaitProjectsOpen() {
        final long now = System.currentTimeMillis();
        try {
            LOGGER.log(Level.FINE, "resetCacheAndFire waiting for projects"); // NOI18N
            openProjects.openProjects().get();
            LOGGER.log(Level.FINE, "resetCacheAndFire blocked for {0} ms", System.currentTimeMillis() - now); // NOI18N
        } catch (Exception ex) {
            LOGGER.log(Level.FINE, "resetCacheAndFire timeout", ex); // NOI18N
        }
    }

    private static class Request {

        final long timeStamp;
        final Collection<TaggedClassPath> sourceCps;
        final Collection<TaggedClassPath> libraryCps;
        final Collection<TaggedClassPath> binaryLibraryCps;
        final Map<ClassPath,byte[]> oldCps;
        @org.netbeans.api.annotations.common.SuppressWarnings(
        value="DMI_COLLECTION_OF_URLS",
        justification="URLs have never host part")
        final Map <URL, SourceForBinaryQuery.Result2> oldSR;
        @org.netbeans.api.annotations.common.SuppressWarnings(
        value="DMI_COLLECTION_OF_URLS",
        justification="URLs have never host part")
        final Map<URL, WeakValue> unknownRoots;
        final PropertyChangeListener propertyListener;
        final ChangeListener changeListener;

        @org.netbeans.api.annotations.common.SuppressWarnings(
        value="DMI_COLLECTION_OF_URLS",
        justification="URLs have never host part")
        public Request (final long timeStamp,
            final Collection<TaggedClassPath> sourceCps, final Collection<TaggedClassPath> libraryCps, final Collection<TaggedClassPath> binaryLibraryCps,
            final Map<ClassPath,byte[]> oldCps, final Map <URL, SourceForBinaryQuery.Result2> oldSR, final Map<URL, WeakValue> unknownRoots,
            final PropertyChangeListener propertyListener, final ChangeListener changeListener
        ) {
            assert sourceCps != null;
            assert libraryCps != null;
            assert binaryLibraryCps != null;
            assert oldCps != null;
            assert oldSR != null;
            assert unknownRoots != null;
            assert propertyListener != null;
            assert changeListener != null;

            this.timeStamp = timeStamp;
            this.sourceCps = sourceCps;
            this.libraryCps = libraryCps;
            this.binaryLibraryCps = binaryLibraryCps;
            this.oldCps = oldCps;
            this.oldSR = oldSR;
            this.unknownRoots = unknownRoots;
            this.propertyListener = propertyListener;
            this.changeListener = changeListener;
        }
    }

    private static class Result {

        final long timeStamp;
        final Collection<URL> sourcePath;
        final Collection<URL> libraryPath;
        final Collection<URL> binaryLibraryPath;
        final Collection<URL> unknownSourcePath;
        final Map<ClassPath,byte[]> newCps;
        @org.netbeans.api.annotations.common.SuppressWarnings(
        value="DMI_COLLECTION_OF_URLS",
        justification="URLs have never host part")
        final Map<URL, SourceForBinaryQuery.Result2> newSR;
        @org.netbeans.api.annotations.common.SuppressWarnings(
        value="DMI_COLLECTION_OF_URLS",
        justification="URLs have never host part")
        final Map<URL, URL[]> translatedRoots;
        @org.netbeans.api.annotations.common.SuppressWarnings(
        value="DMI_COLLECTION_OF_URLS",
        justification="URLs have never host part")
        final Map<URL, WeakValue> unknownRoots;
        @org.netbeans.api.annotations.common.SuppressWarnings(
        value="DMI_COLLECTION_OF_URLS",
        justification="URLs have never host part")
        final Map<URL, PathIds> rootPathIds;
        @org.netbeans.api.annotations.common.SuppressWarnings(
        value="DMI_COLLECTION_OF_URLS",
        justification="URLs have never host part")
        final Map<String, Set<URL>> pathIdToRoots;

        @org.netbeans.api.annotations.common.SuppressWarnings(
        value="DMI_COLLECTION_OF_URLS",
        justification="URLs have never host part")
        public Result (final long timeStamp,
            final Collection<URL> sourcePath,
            final Collection<URL> libraryPath,
            final Collection<URL> binaryLibraryPath,
            final Collection<URL> unknownSourcePath,
            final Map<ClassPath,byte[]> newCps,
            final Map<URL, SourceForBinaryQuery.Result2> newSR, final Map<URL, URL[]> translatedRoots,
            final Map<URL, WeakValue> unknownRoots,
            final Map<URL, PathIds> rootPathIds,
            Map<String, Set<URL>> pathIdToRoots) {
            assert sourcePath != null;
            assert libraryPath != null;
            assert binaryLibraryPath != null;
            assert unknownSourcePath != null;
            assert newCps != null;
            assert newSR  != null;
            assert translatedRoots != null;
            assert rootPathIds != null;
            this.timeStamp = timeStamp;
            this.sourcePath = sourcePath;
            this.libraryPath = libraryPath;
            this.binaryLibraryPath = binaryLibraryPath;
            this.unknownSourcePath = unknownSourcePath;
            this.newCps = newCps;
            this.newSR = newSR;
            this.translatedRoots = translatedRoots;
            this.unknownRoots = unknownRoots;
            this.rootPathIds = rootPathIds;
            this.pathIdToRoots = pathIdToRoots;
        }
    }

    private class WeakValue extends WeakReference<ClassPath> implements Runnable {

        private final URL key;

        public WeakValue (ClassPath ref, URL key) {
            super (ref, BaseUtilities.activeReferenceQueue());
            assert key != null;
            this.key = key;
        }

        public @Override void run () {
            boolean fire;
            synchronized (PathRegistry.this) {
                fire = (unknownRoots.remove (key) != null);
            }
            if (FIRE_UNKNOWN_ALWAYS && fire) {
                resetCacheAndFire(EventKind.PATHS_REMOVED, PathKind.UNKNOWN_SOURCE, null, null);
            }
        }
    }

    private class Listener implements GlobalPathRegistryListener, PropertyChangeListener, ChangeListener, Runnable {

            private WeakReference<Object> lastPropagationId;

            public @Override void pathsAdded(GlobalPathRegistryEvent event) {
                final String pathId = event.getId();
                final PathKind pk = getPathKind (pathId);
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.log(Level.FINE, "pathsAdded: {0}, paths= {1}", new Object[]{event.getId(), event.getChangedPaths()}); //NOI18N
                    LOGGER.log(Level.FINE, "''{0}'' -> ''{1}''", new Object[]{pathId, pk}); //NOI18N
                }
                if (pk != null) {
                    resetCacheAndFire (EventKind.PATHS_ADDED, pk, pathId, event.getChangedPaths());
                }
            }

            public @Override void pathsRemoved(GlobalPathRegistryEvent event) {
                final String pathId = event.getId();
                final PathKind pk = getPathKind (pathId);
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.log(Level.FINE, "pathsRemoved: {0}, paths={1}", new Object[]{event.getId(), event.getChangedPaths()}); //NOI18N
                    LOGGER.log(Level.FINE, "''{0}'' -> ''{1}''", new Object[]{pathId, pk}); //NOI18N
                }
                if (pk != null) {
                    resetCacheAndFire (EventKind.PATHS_REMOVED, pk, pathId, event.getChangedPaths());
                }
            }

            public @Override void propertyChange(PropertyChangeEvent evt) {
                if (LOGGER.isLoggable(Level.FINE)) {
                    String msg = "propertyChange: " + evt.getPropertyName() //NOI18N
                            + ", old=" + evt.getOldValue() //NOI18N
                            + ", new=" + evt.getNewValue() //NOI18N
                            + ", source=" + s2s(evt.getSource()); //NOI18N
//                    LOGGER.log(Level.FINE, null, new Throwable(msg));
                    LOGGER.log(Level.FINE, msg);
                }

                final String propName = evt.getPropertyName();
                if (propName != null) {
                    switch (propName) {
                        case ClassPath.PROP_ENTRIES:
                        case ClassPath.PROP_FLAGS:
                            final Object source = evt.getSource();
                            if ((source instanceof ClassPath) &&
                                    classPathChanged((ClassPath)source)) {
                                resetCacheAndFire (EventKind.PATHS_CHANGED, null, null, Collections.singleton((ClassPath)evt.getSource()));
                            }
                            break;
                        case ClassPath.PROP_INCLUDES:
                            final Object newPropagationId = evt.getPropagationId();
                            boolean fire;
                            synchronized (this) {
                                fire = (newPropagationId == null || lastPropagationId == null || lastPropagationId.get() != newPropagationId);
                                lastPropagationId = new WeakReference<>(newPropagationId);
                            }
                            if (fire) {
                                resetCacheAndFire (EventKind.INCLUDES_CHANGED, PathKind.SOURCE, null, Collections.singleton((ClassPath)evt.getSource()));
                            }
                            break;
                        case OpenProjects.PROPERTY_OPEN_PROJECTS:
                            if (!firstProjectOpened) {
                                openProjectChangeTask.schedule(0);
                            }
                            break;
                    }
                }
            }

            public @Override void stateChanged (final ChangeEvent event) {
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.log(Level.FINE, "stateChanged: {0}", event); //NOI18N
                }
                resetCacheAndFire(EventKind.PATHS_CHANGED, PathKind.BINARY_LIBRARY, null, null);
            }

            @Override
            public void run() {
                try {
                    final int len = openProjects.openProjects().get().length;
                    if (!firstProjectOpened && len > 0) {
                        firstProjectOpened = true;
                        fire(
                          Collections.singleton(
                                new PathRegistryEvent.Change(
                                    EventKind.PATHS_CHANGED,
                                    PathKind.SOURCE,
                                    null,
                                    Collections.<ClassPath>emptySet())),
                                LogContext.create(
                                    LogContext.EventType.PATH,
                                    "Unsupported project(s) opened.")); //NOI18N
                    }
                } catch (InterruptedException ex) {
                    //Pass - not important
                    LOGGER.fine("Interrupted while waiting for projects to be loaded.");  //NOI18N
                } catch (ExecutionException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
    }

    private static String s2s(Object o) {
        return o == null ? "null" : o.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(o)); //NOI18N
    }

    private static final class TaggedClassPath {

        private final ClassPath classpath;
        private final PathIds pathIds = new PathIds();

        public TaggedClassPath(ClassPath classpath) {
            this.classpath = classpath;
        }

        public ClassPath getClasspath() {
            return classpath;
        }

        public PathIds getPathIds() {
            return pathIds;
        }

        public void associateWithSourceId(String id) {
            pathIds.getSids().add(id);
        }

        public void associateWithLibraryId(String id) {
            pathIds.getLids().add(id);
        }

        public void associateWithBinaryLibraryId(String id) {
            pathIds.getBlids().add(id);
        }

        public void associateWithMimeTypes(Set<String> mimeTypes) {
            pathIds.getMimeTypes().addAll(mimeTypes);
        }
    } // End of TaggedClassPath class

    private static final class PathIds {

        private final Set<String> sourcePathIds = new HashSet<>();
        private final Set<String> libraryPathIds = new HashSet<>();
        private final Set<String> binaryLibraryPathIds = new HashSet<>();
        private final Set<String> mimeTypes = new HashSet<>();

        @NonNull
        @SuppressWarnings("ReturnOfCollectionOrArrayField")
        public Set<String> getSids() {
            return sourcePathIds;
        }

        @NonNull
        @SuppressWarnings("ReturnOfCollectionOrArrayField")
        public Set<String> getLids() {
            return libraryPathIds;
        }

        @NonNull
        @SuppressWarnings("ReturnOfCollectionOrArrayField")
        public Set<String> getBlids() {
            return binaryLibraryPathIds;
        }

        @NonNull
        @SuppressWarnings("ReturnOfCollectionOrArrayField")
        public Set<String> getMimeTypes() {
            return mimeTypes;
        }

        public Set<String> getAllIds() {
            Set<String> allIds = new HashSet<>();
            allIds.addAll(sourcePathIds);
            allIds.addAll(libraryPathIds);
            allIds.addAll(binaryLibraryPathIds);
            return allIds;
        }

        public void addAll(PathIds pathIds) {
            sourcePathIds.addAll(pathIds.getSids());
            libraryPathIds.addAll(pathIds.getLids());
            binaryLibraryPathIds.addAll(pathIds.getBlids());
            mimeTypes.addAll(pathIds.getMimeTypes());
        }

        @Override
        public String toString() {
            return super.toString() + ";sids=" + sourcePathIds + ", lids=" + libraryPathIds + ", blids=" + binaryLibraryPathIds; //NOI18N
        }
    } // End of PathIds class

    /*test*/ @SuppressWarnings("PackageVisibleInnerClass")
    static class IdentityDigest extends MessageDigest {

        private byte[] buffer = new byte[1024];
        private int length = 0;

        IdentityDigest() {
            super(IdentityDigest.class.getSimpleName());
        }

        @Override
        @SuppressWarnings("ValueOfIncrementOrDecrementUsed")
        protected void engineUpdate(byte input) {
            ensureSize(1);
            buffer[length++] = input;
        }

        @Override
        protected void engineUpdate(byte[] input, int offset, int len) {
            ensureSize(len);
            System.arraycopy(input, offset, buffer, length, len);
            length+=len;
        }

        @Override
        protected byte[] engineDigest() {
            return Arrays.copyOf(buffer, length);
        }

        @Override
        protected void engineReset() {
            length = 0;
        }

        private void ensureSize(final int needed) {
            int size = buffer.length;
            while (size < (needed+length)) {
                size<<=1;
            }
            if (size != buffer.length) {
                buffer = new byte[size];
            }
        }
    }
}
