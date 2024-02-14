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

package org.netbeans.modules.debugger.jpda.projects;

import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.debugger.Properties;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.ClassPath.Entry;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.classpath.GlobalPathRegistryEvent;
import org.netbeans.api.java.classpath.GlobalPathRegistryListener;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.java.source.BuildArtifactMapper;
import org.netbeans.api.java.source.BuildArtifactMapper.ArtifactsUpdated;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.jpda.SourcePathProvider;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.JarFileSystem;
import org.openide.filesystems.URLMapper;
import org.openide.util.BaseUtilities;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;


/**
 *
 * @author Jan Jancura
 */
@SourcePathProvider.Registration(path="netbeans-JPDASession")
public class SourcePathProviderImpl extends SourcePathProvider {
    
    private static final boolean    verbose =
        System.getProperty ("netbeans.debugger.sourcepathproviderimpl") != null;
    
    private static final Logger logger = Logger.getLogger("org.netbeans.modules.debugger.jpda.projects");
    
    private static final Pattern thisDirectoryPattern = Pattern.compile("(/|\\A)\\./");
    private static final Pattern parentDirectoryPattern = Pattern.compile("(/|\\A)([^/]+?)/\\.\\./");

    /** Contains all known source paths + jdk source path for JPDAStart task + {@link #additionalSourceRoots} */
    private ClassPath               originalSourcePath;
    /** Contains the additional source roots, added at a later time to the original roots. */
    private Set<String>             additionalSourceRoots;
    /** Contains platform (JDK) source roots. */
    private Set<String>             platformSourceRoots;
    /** Contains just the source paths from {@link #originalSourcePath} which are selected for debugging. */
    private ClassPath               smartSteppingSourcePath;
    /** Roots of {@link #originalSourcePath} */
    private String[]                projectSourceRoots;
    /** {@link #originalSourcePath} in the original order. */
    private ClassPath               unorderedOriginalSourcePath;
    /** Permutation that creates {@link #originalSourcePath} from {@link #unorderedOriginalSourcePath}. */
    private int[]                   sourcePathPermutation;
    private PropertyChangeSupport   pcs;
    private PathRegistryListener    pathRegistryListener;
    private File                    baseDir;

    private final Map<String, String> urlCache = new URLCacheMap();
    private final Map<String, String> urlCacheGlobal = new URLCacheMap();
    
    public SourcePathProviderImpl () {
        pcs = new PropertyChangeSupport (this);
    }

    public SourcePathProviderImpl (ContextProvider contextProvider) {
        pcs = new PropertyChangeSupport (this);
        //this.session = (Session) contextProvider.lookupFirst 
        //    (null, Session.class);
        JPDADebugger debugger = (JPDADebugger) contextProvider.lookupFirst(null, JPDADebugger.class);
        Map properties = contextProvider.lookupFirst(null, Map.class);

        Set<FileObject> srcRootsToListenForArtifactsUpdates = null;
        
        // 2) get default allSourceRoots of source roots used for stepping
        if (logger.isLoggable(Level.FINE)) logger.fine("Have properties = "+properties);
        if (properties != null) {
            baseDir = (File) properties.get("baseDir");
            smartSteppingSourcePath = (ClassPath) properties.get ("sourcepath");
            ClassPath jdkCP = (ClassPath) properties.get ("jdksources");
            if ( (jdkCP == null) && (JavaPlatform.getDefault () != null) ) {
                jdkCP = JavaPlatform.getDefault ().getSourceFolders ();
            }
            platformSourceRoots = getSourceRootsSet(jdkCP);
            ClassPath additionalClassPath;
            if (baseDir != null) {
                additionalClassPath = getAdditionalClassPath(baseDir);
            } else {
                additionalClassPath = null;
                Exceptions.printStackTrace(new NullPointerException("No base directory is defined. Properties = "+properties));
            }
            if (additionalClassPath != null) {
                smartSteppingSourcePath = ClassPathSupport.createProxyClassPath (
                        new ClassPath[] {
                            smartSteppingSourcePath,
                            additionalClassPath
                        });
            }
            smartSteppingSourcePath = jdkCP == null ?
                smartSteppingSourcePath :
                ClassPathSupport.createProxyClassPath (
                    new ClassPath[] {
                        jdkCP,
                        smartSteppingSourcePath,
                    }
            );
            unorderedOriginalSourcePath = smartSteppingSourcePath;

            Map<String, Integer> orderIndexes = getSourceRootsOrder(baseDir);
            String[] unorderedOriginalRoots = getSourceRoots(unorderedOriginalSourcePath);
            String[] sortedOriginalRoots = new String[unorderedOriginalRoots.length];
            sourcePathPermutation = createPermutation(unorderedOriginalRoots,
                                                      orderIndexes,
                                                      sortedOriginalRoots);
            smartSteppingSourcePath = createClassPath(sortedOriginalRoots);
            
            originalSourcePath = smartSteppingSourcePath;

            Set<String> disabledRoots;
            if (baseDir != null) {
                disabledRoots = getDisabledSourceRoots(baseDir);
            } else {
                disabledRoots = null;
            }
            if (disabledRoots != null && !disabledRoots.isEmpty()) {
                List<FileObject> enabledSourcePath = new ArrayList<FileObject>(
                        Arrays.asList(smartSteppingSourcePath.getRoots()));
                for (FileObject fo : new HashSet<FileObject>(enabledSourcePath)) {
                    if (disabledRoots.contains(getRoot(fo))) {
                        enabledSourcePath.remove(fo);
                    }
                }
                smartSteppingSourcePath = createClassPath(
                        enabledSourcePath.toArray(new FileObject[0]));
            }

            projectSourceRoots = getSourceRoots(originalSourcePath);
            //Set<FileObject> preferredRoots = new HashSet<FileObject>();
            //preferredRoots.addAll(Arrays.asList(originalSourcePath.getRoots()));
            /*
            Set<FileObject> globalRoots = new TreeSet<FileObject>(new FileObjectComparator());
            globalRoots.addAll(GlobalPathRegistry.getDefault().getSourceRoots());
            globalRoots.removeAll(preferredRoots);
            ClassPath globalCP = createClassPath(globalRoots.toArray(new FileObject[0]));
            originalSourcePath = ClassPathSupport.createProxyClassPath(
                    originalSourcePath,
                    globalCP
            );
             */
            String listeningCP = (String) properties.get("listeningCP");
            if (listeningCP != null) {
                boolean isSourcepath = false;
                if ("sourcepath".equalsIgnoreCase(listeningCP)) {
                    listeningCP = ((ClassPath) properties.get ("sourcepath")).toString(ClassPath.PathConversionMode.SKIP);
                    isSourcepath = true;
                }
                srcRootsToListenForArtifactsUpdates = new HashSet<FileObject>();
                for (String cp : listeningCP.split(File.pathSeparator)) {
                    logger.log(Level.FINE, "Listening cp = ''{0}''", cp);
                    File f = new File(cp);
                    f = FileUtil.normalizeFile(f);
                    URL entry = FileUtil.urlForArchiveOrDir(f);

                    if (entry != null) {
                        if (isSourcepath) {
                            FileObject src = URLMapper.findFileObject(entry);
                            if (src != null) {
                                srcRootsToListenForArtifactsUpdates.add(src);
                            }
                        }
                        for (FileObject src : SourceForBinaryQuery.findSourceRoots(entry).getRoots()) {
                            srcRootsToListenForArtifactsUpdates.add(src);
                        }
                    }
                }
                if (srcRootsToListenForArtifactsUpdates.isEmpty()) {
                    srcRootsToListenForArtifactsUpdates = null;
                }
            }
        } else {
            pathRegistryListener = new PathRegistryListener();
            GlobalPathRegistry.getDefault().addGlobalPathRegistryListener(
                    WeakListeners.create(GlobalPathRegistryListener.class,
                                         pathRegistryListener,
                                         GlobalPathRegistry.getDefault()));
            JavaPlatformManager.getDefault ().addPropertyChangeListener(
                    WeakListeners.propertyChange(pathRegistryListener,
                                                 JavaPlatformManager.getDefault()));
            
            List<FileObject> allSourceRoots = new ArrayList<FileObject>();
            Set<FileObject> preferredRoots = new HashSet<FileObject>();
            Set<FileObject> addedBinaryRoots = new HashSet<FileObject>();
            Project mainProject = OpenProjects.getDefault().getMainProject();
            platformSourceRoots = new HashSet<String>();
            if (mainProject != null) {
                SourceGroup[] sgs = ProjectUtils.getSources(mainProject).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
                for (SourceGroup sg : sgs) {
                    ClassPath ecp = ClassPath.getClassPath(sg.getRootFolder(), ClassPath.EXECUTE);
                    if (ecp == null) {
                        ecp = ClassPath.getClassPath(sg.getRootFolder(), ClassPath.SOURCE);
                    }
                    if (ecp != null) {
                        FileObject[] binaryRoots = ecp.getRoots();
                        for (FileObject fo : binaryRoots) {
                            if (addedBinaryRoots.contains(fo)) {
                                continue;
                            }
                            addedBinaryRoots.add(fo);
                            FileObject[] roots = SourceForBinaryQuery.findSourceRoots(fo.toURL()).getRoots();
                            for (FileObject fr : roots) {
                                if (!preferredRoots.contains(fr)) {
                                    allSourceRoots.add(fr);
                                    preferredRoots.add(fr);
                                }
                            }
                        }
                    }
                    ecp = ClassPath.getClassPath(sg.getRootFolder(), ClassPath.BOOT);
                    if (ecp != null) {
                        platformSourceRoots.addAll(getSourceRootsSet(ecp));
                    }
                }
            }
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("SourcePathProviderImpl: preferred source roots = "+preferredRoots+")");
            }
            Set<FileObject> globalRoots = new TreeSet<FileObject>(new FileObjectComparator());
            globalRoots.addAll(GlobalPathRegistry.getDefault().getSourceRoots());
            for (FileObject fo : globalRoots) {
                if (!preferredRoots.contains(fo)) {
                    allSourceRoots.add(fo);
                }
            }
            // TODO: Add first main project's BOOT path, if not exist, then default platform and then the rest.
            JavaPlatform[] platforms = JavaPlatformManager.getDefault().getInstalledPlatforms();
            for (int i = 0; i < platforms.length; i++) {
                FileObject[] roots = platforms[i].getSourceFolders().getRoots ();
                int j, jj = roots.length;
                for (j = 0; j < jj; j++) {
                    if (!allSourceRoots.contains(roots [j])) {
                        allSourceRoots.add(roots [j]);
                    }
                }
                platformSourceRoots.addAll(getSourceRootsSet(platforms[i].getSourceFolders()));
            }
            List<FileObject> additional = getAdditionalRemoteClassPath();
            if (additional != null) {
                allSourceRoots.addAll(additional);
            }
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("SourcePathProviderImpl: GlobalPathRegistry roots = "+GlobalPathRegistry.getDefault().getSourceRoots()+")");
                logger.fine("Platform roots:");
                for (int i = 0; i < platforms.length; i++) {
                    logger.fine(" "+Arrays.asList(platforms[i].getSourceFolders().getRoots ()).toString());
                }
                logger.fine("SourcePathProviderImpl: all source roots = "+allSourceRoots+")");
            }

            Set<String> disabledRoots = getRemoteDisabledSourceRoots();
            
            synchronized (this) {
                unorderedOriginalSourcePath = createClassPath(allSourceRoots.toArray(new FileObject[0]));
                
                Map<String, Integer> orderIndexes = getRemoteSourceRootsOrder();
                String[] unorderedOriginalRoots = getSourceRoots(unorderedOriginalSourcePath);
                String[] sorterOriginalRoots = new String[unorderedOriginalRoots.length];
                sourcePathPermutation = createPermutation(unorderedOriginalRoots,
                                                          orderIndexes,
                                                          sorterOriginalRoots);
                originalSourcePath = createClassPath(sorterOriginalRoots);

                projectSourceRoots = getSourceRoots(originalSourcePath);

                srcRootsToListenForArtifactsUpdates = new HashSet<FileObject>(allSourceRoots);

                smartSteppingSourcePath = originalSourcePath;

                if (disabledRoots != null && !disabledRoots.isEmpty()) {
                    List<FileObject> enabledSourcePath = new ArrayList<FileObject>(
                            Arrays.asList(smartSteppingSourcePath.getRoots()));
                    for (FileObject fo : new HashSet<FileObject>(enabledSourcePath)) {
                        if (disabledRoots.contains(getRoot(fo))) {
                            enabledSourcePath.remove(fo);
                        }
                    }
                    smartSteppingSourcePath = createClassPath(
                            enabledSourcePath.toArray(new FileObject[0]));
                }
            }
        }
        
        if (verbose) 
            System.out.println 
                ("SPPI: init originalSourcePath " + originalSourcePath);    
        if (verbose) 
            System.out.println (
                "SPPI: init smartSteppingSourcePath " + smartSteppingSourcePath
            );

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("new SourcePathProviderImpl(): contextProvider = "+contextProvider+
                        ", properties = "+properties+
                        ", srcRootsToListenForArtifactsUpdates = "+srcRootsToListenForArtifactsUpdates);
        }

        if (srcRootsToListenForArtifactsUpdates != null) {
            final Set<ArtifactsUpdatedImpl> artifactsListeners = new HashSet<ArtifactsUpdatedImpl>();
            for (FileObject src : srcRootsToListenForArtifactsUpdates) {
                artifactsListeners.add(addArtifactsUpdateListenerFor(debugger, src));
            }
            debugger.addPropertyChangeListener(JPDADebugger.PROP_STATE, new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (JPDADebugger.STATE_DISCONNECTED == ((Integer) evt.getNewValue()).intValue()) {
                        for (ArtifactsUpdatedImpl al : artifactsListeners) {
                            BuildArtifactMapper.removeArtifactsUpdatedListener(al.getURL(), al);
                        }
                    }
                }
            });
        }
    }

    private ClassPath getAdditionalClassPath(File baseDir) {
        try {
            String root = BaseUtilities.toURI(baseDir).toURL().toExternalForm();
            Properties sourcesProperties = Properties.getDefault ().getProperties ("debugger").getProperties ("sources");
            List<String> additionalSourceRoots = (List<String>) sourcesProperties.
                    getProperties("additional_source_roots").
                    getMap("project", Collections.emptyMap()).
                    get(root);
            if (additionalSourceRoots == null || additionalSourceRoots.isEmpty()) {
                return null;
            }
            List<FileObject> additionalSourcePath = new ArrayList<FileObject>(additionalSourceRoots.size());
            for (String ar : additionalSourceRoots) {
                FileObject fo = getFileObject(ar);
                if (fo != null && fo.canRead()) {
                    additionalSourcePath.add(fo);
                }
            }
            this.additionalSourceRoots = new LinkedHashSet<String>(additionalSourceRoots);
            return createClassPath(additionalSourcePath.toArray(new FileObject[0]));
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    private List<FileObject> getAdditionalRemoteClassPath() {
        Properties sourcesProperties = Properties.getDefault ().getProperties ("debugger").getProperties ("sources");
        List<String> additionalSourceRoots = (List<String>) sourcesProperties.
                getProperties("additional_source_roots").
                getCollection("src_roots", Collections.emptyList());
        if (additionalSourceRoots == null || additionalSourceRoots.isEmpty()) {
            return null;
        }
        List<FileObject> additionalSourcePath = new ArrayList<FileObject>(additionalSourceRoots.size());
        for (String ar : additionalSourceRoots) {
            FileObject fo = getFileObject(ar);
            if (fo != null && fo.canRead()) {
                additionalSourcePath.add(fo);
            }
        }
        this.additionalSourceRoots = new LinkedHashSet<String>(additionalSourceRoots);
        return additionalSourcePath;
        //return ClassPathSupport.createClassPath(
        //        additionalSourcePath.toArray(new FileObject[0]));
    }

    private void storeAdditionalSourceRoots() {
        Properties sourcesProperties = Properties.getDefault ().getProperties ("debugger").getProperties ("sources");
        if (baseDir != null) {
            String projectRoot;
            try {
                projectRoot = BaseUtilities.toURI(baseDir).toURL().toExternalForm();
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
                return ;
            }
            Map map = sourcesProperties.getProperties("additional_source_roots").
                getMap("project", new HashMap());
            if (additionalSourceRoots != null) {
                map.put(projectRoot, new ArrayList<String>(additionalSourceRoots));
            } else {
                map.remove(projectRoot);
            }
            sourcesProperties.getProperties("additional_source_roots").
                    setMap("project", map);
        } else {
            if (additionalSourceRoots != null) {
                sourcesProperties.getProperties("additional_source_roots").
                        setCollection("src_roots", new ArrayList<String>(additionalSourceRoots));
            } else {
                sourcesProperties.getProperties("additional_source_roots").
                        setCollection("src_roots", null);
            }
        }
    }

    private Set<String> getDisabledSourceRoots(File baseDir) {
        try {
            String root = BaseUtilities.toURI(baseDir).toURL().toExternalForm();
            Properties sourcesProperties = Properties.getDefault ().getProperties ("debugger").getProperties ("sources");
            return (Set<String>) sourcesProperties.getProperties("source_roots").
                getMap("project_disabled", Collections.emptyMap()).
                get(root);
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    private Set<String> getRemoteDisabledSourceRoots() {
        Properties sourcesProperties = Properties.getDefault ().getProperties ("debugger").getProperties ("sources");
        return (Set<String>) sourcesProperties.getProperties("source_roots").
            getCollection("remote_disabled", Collections.emptySet());
    }

    private void storeDisabledSourceRoots(Set<String> disabledSourceRoots) {
        Properties sourcesProperties = Properties.getDefault ().getProperties ("debugger").getProperties ("sources");
        if (baseDir != null) {
            String projectRoot;
            try {
                projectRoot = BaseUtilities.toURI(baseDir).toURL().toExternalForm();
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
                return ;
            }
            Map map = sourcesProperties.getProperties("source_roots").
                    getMap("project_disabled", new HashMap());
            map.put(projectRoot, disabledSourceRoots);
            sourcesProperties.getProperties("source_roots").
                    setMap("project_disabled", map);
        } else {
            sourcesProperties.getProperties("source_roots").
                    setCollection("remote_disabled", disabledSourceRoots);
        }
    }

    private static Map<String, Integer> getSourceRootsOrder(File baseDir) {
        try {
            String root = BaseUtilities.toURI(baseDir).toURL().toExternalForm();
            return getSourceRootsOrder(root);
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    public static Map<String, Integer> getSourceRootsOrder(String root) {
        Properties sourcesProperties = Properties.getDefault ().getProperties ("debugger").getProperties ("sources");
        return (Map<String, Integer>) sourcesProperties.getProperties("source_roots").
            getMap("project_order", Collections.emptyMap()).
            get(root);
    }

    public static Map<String, Integer> getRemoteSourceRootsOrder() {
        Properties sourcesProperties = Properties.getDefault ().getProperties ("debugger").getProperties ("sources");
        return (Map<String, Integer>) sourcesProperties.getProperties("source_roots").
            getMap("remote_order", Collections.emptyMap());
    }

    private static void storeSourceRootsOrder(File baseDir, String[] roots, int[] permutation) {
        String projectRoot;
        if (baseDir != null) {
            try {
                projectRoot = BaseUtilities.toURI(baseDir).toURL().toExternalForm();
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
                return ;
            }
        } else {
            projectRoot = null;
        }
        storeSourceRootsOrder(projectRoot, roots, permutation);
    }

    public static void storeSourceRootsOrder(String projectRoot, String[] roots, int[] permutation) {
        Map<String, Integer> sourceOrder = new HashMap<String, Integer>();
        if (roots.length != permutation.length) {
            throw new IllegalArgumentException("Incompatible array length: roots = "+roots.length+", permutation = "+permutation.length);
        }
        for (int i = 0; i < roots.length; i++) {
            sourceOrder.put(roots[permutation[i]], i);
        }
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("SourcePathProviderImpl.storeSourceRootsOrder():");
            logger.fine("  sourceOrder = "+sourceOrder);
        }
        storeSourceRootsOrder(projectRoot, sourceOrder);
    }

    private static void storeSourceRootsOrder(String projectRoot, Map<String, Integer> sourceOrder) {
        Properties sourcesProperties = Properties.getDefault ().getProperties ("debugger").getProperties ("sources");
        if (projectRoot != null) {
            Map map = sourcesProperties.getProperties("source_roots").
                    getMap("project_order", new HashMap());
            map.put(projectRoot, sourceOrder);
            sourcesProperties.getProperties("source_roots").
                    setMap("project_order", map);
        } else {
            sourcesProperties.getProperties("source_roots").
                    setMap("remote_order", sourceOrder);
        }
    }

    /**
     * Translates a relative path ("java/lang/Thread.java") to url 
     * ("file:///C:/Sources/java/lang/Thread.java"). Uses GlobalPathRegistry
     * if global == true.
     *
     * @param relativePath a relative path (java/lang/Thread.java)
     * @param global true if global path should be used
     * @return url or <code>null</code>
     */
    @Override
    public String getURL (String relativePath, boolean global) {    if (verbose) System.out.println ("SPPI: getURL " + relativePath + " global " + global);
        relativePath = normalize(relativePath);
        if (global) {
            synchronized (urlCacheGlobal) {
                if (urlCacheGlobal.containsKey(relativePath)) {
                    if (verbose) System.out.println("Have cached global path for '"+relativePath+"' url = "+urlCacheGlobal.get(relativePath));
                    return urlCacheGlobal.get(relativePath);    // URL or null
                }
            }
        } else {
            synchronized (urlCache) {
                if (urlCache.containsKey(relativePath)) {
                    if (verbose) System.out.println("Have cached path for '"+relativePath+"' url = "+urlCache.get(relativePath));
                    return urlCache.get(relativePath);  // URL or null
                }
            }
        }
        FileObject fo;
        ClassPath ss = null;
        ClassPath os = null;
        synchronized (this) {
            if (originalSourcePath != null) {
                ss = smartSteppingSourcePath;
                os = originalSourcePath;
            }
        }
        if (ss != null && os != null) {
            fo = ss.findResource(relativePath);
            if (fo == null && global) {
                fo = os.findResource(relativePath);
            }
            if (fo == null && global) {
                fo = GlobalPathRegistry.getDefault().findResource(relativePath);
            }
        } else {
            fo = GlobalPathRegistry.getDefault().findResource(relativePath);
        }
        if (fo == null && global) {
            Set<ClassPath> cpaths = GlobalPathRegistry.getDefault().getPaths(ClassPath.COMPILE);
            for (ClassPath cp : cpaths) {
                fo = cp.findResource(relativePath);
                if (fo != null) {
                    FileObject[] roots = cp.getRoots();
                    for (FileObject r : roots) {
                        if (FileUtil.isParentOf(r, fo)) {
                            addToSourcePath(r, false);
                            break;
                        }
                    }
                    break;
                }
            }
        }
        
        if (verbose) System.out.println ("SPPI:   fo " + fo);

        String url;
        if (fo == null) {
            url = null;
        } else {
            url = fo.toURL ().toString ();
        }
        if (global) {
            synchronized (urlCacheGlobal) {
                if (verbose) System.out.println("Storing path into global cache for '"+relativePath+"' url = "+url);
                urlCacheGlobal.put(relativePath, url);
                if (verbose) System.out.println("  Global cache ("+urlCacheGlobal.size()+") = "+urlCacheGlobal);
            }
        } else {
            synchronized (urlCache) {
                if (verbose) System.out.println("Storing path into cache for '"+relativePath+"' url = "+url);
                urlCache.put(relativePath, url);
                if (verbose) System.out.println("  Cache = ("+urlCache.size()+") "+urlCache);
            }
        }
        return url;
    }
    
    private void addToSourcePath(FileObject sourceRoot, boolean clearURLCaches) {
        URL newURL = sourceRoot.toURL();
        synchronized (SourcePathProviderImpl.this) {
            if (originalSourcePath == null) {
                return ;
            }
            List<URL> sourcePaths = getURLRoots(originalSourcePath);
            sourcePaths.add(newURL);
            originalSourcePath =
                    SourcePathProviderImpl.createClassPath(
                        sourcePaths.toArray(new URL[0]));

            sourcePaths = getURLRoots(smartSteppingSourcePath);
            sourcePaths.add(newURL);
            smartSteppingSourcePath =
                    SourcePathProviderImpl.createClassPath(
                        sourcePaths.toArray(new URL[0]));
        }
        if (clearURLCaches) {
            synchronized (urlCache) {
                urlCache.clear();
            }
            synchronized (urlCacheGlobal) {
                urlCacheGlobal.clear();
            }
        }
        pcs.firePropertyChange (PROP_SOURCE_ROOTS, null, null);
    }
    
    /**
     * Translates a relative path to all possible URLs.
     * Uses GlobalPathRegistry if global == true.
     *
     * @param relativePath a relative path (java/lang/Thread.java)
     * @param global true if global path should be used
     * @return url
     */
    public String[] getAllURLs (String relativePath, boolean global) {      if (verbose) System.out.println ("SPPI: getURL " + relativePath + " global " + global);
        List<FileObject> fos;
        relativePath = normalize(relativePath);
        if (originalSourcePath == null) {
            fos = new ArrayList<FileObject>();
            for (ClassPath cp : GlobalPathRegistry.getDefault().getPaths(ClassPath.SOURCE)) {
                fos.addAll(cp.findAllResources(relativePath));
            }
        } else {
            synchronized (this) {
                if (!global) {
                    fos = smartSteppingSourcePath.findAllResources(relativePath);
                                                                            if (verbose) System.out.println ("SPPI:   fos " + fos);
                } else {
                    fos = originalSourcePath.findAllResources(relativePath);
                                                                            if (verbose) System.out.println ("SPPI:   fos " + fos);
                }
            }
        }
        List<String> urls = new ArrayList<String>(fos.size());
        for (FileObject fo : fos) {
            urls.add(fo.toURL().toString());
        }
        return urls.toArray(new String[0]);
    }
    
    /**
     * Returns relative path for given url.
     *
     * @param url a url of resource file
     * @param directorySeparator a directory separator character
     * @param includeExtension whether the file extension should be included 
     *        in the result
     *
     * @return relative path
     */
    @Override
    public String getRelativePath (
        String url, 
        char directorySeparator, 
        boolean includeExtension
    ) {
        // 1) url -> FileObject
        FileObject fo;                                              if (verbose) System.out.println ("SPPI: getRelativePath " + url);
        try {
            fo = URLMapper.findFileObject (new URL (url));          if (verbose) System.out.println ("SPPI:   fo " + fo);
        } catch (MalformedURLException e) {
            //e.printStackTrace ();
            return null;
        }
        if (fo == null) {
            return null;
        }
        String relativePath = smartSteppingSourcePath.getResourceName (
            fo, 
            directorySeparator,
            includeExtension
        );
        if (relativePath == null) {
            // fallback to FileObject's class path
            ClassPath cp = ClassPath.getClassPath (fo, ClassPath.SOURCE);
            if (cp == null) {
                cp = ClassPath.getClassPath (fo, ClassPath.COMPILE);
            }
            if (cp == null) {
                return null;
            }
            relativePath = cp.getResourceName (
                fo, 
                directorySeparator,
                includeExtension
            );
        }
        return relativePath;
    }
    
    /**
     * Returns the source root (if any) for given url.
     *
     * @param url a url of resource file
     *
     * @return the source root or <code>null</code> when no source root was found.
     */
    @Override
    public synchronized String getSourceRoot(String url) {
        FileObject fo;
        try {
            fo = URLMapper.findFileObject(new java.net.URL(url));
        } catch (java.net.MalformedURLException ex) {
            fo = null;
        }
        FileObject[] roots = null;
        if (fo != null && fo.canRead()) {
            ClassPath cp = ClassPath.getClassPath(fo, ClassPath.SOURCE);
            if (cp != null) {
                roots = cp.getRoots();
            }
        }
        if (roots == null) {
            roots = originalSourcePath.getRoots();
        }
        for (FileObject fileObject : roots) {
            String rootURL = fileObject.toURL().toString();
            if (url.startsWith(rootURL)) {
                String root = getRoot(fileObject);
                if (root != null) {
                    return root;
                }
            }
        }
        return null; // not found
    }

    private String[] getSourceRoots(ClassPath classPath) {
        FileObject[] sourceRoots = classPath.getRoots();
        List<String> roots = new ArrayList<String>(sourceRoots.length);
        for (FileObject fo : sourceRoots) {
            String root = getRoot(fo);
            if (root != null) {
                roots.add(root);
            }
        }
        return roots.toArray(new String[0]);
    }
    
    private static Set<String> getSourceRootsSet(ClassPath classPath) {
        FileObject[] sourceRoots = classPath.getRoots();
        Set<String> roots = new HashSet<String>(sourceRoots.length);
        for (FileObject fo : sourceRoots) {
            String root = getRoot(fo);
            if (root != null) {
                roots.add(root);
            }
        }
        return roots;
    }

    public synchronized Set<String> getPlatformSourceRoots() {
        return Collections.unmodifiableSet(platformSourceRoots);
    }

    /**
     * Returns allSourceRoots of original source roots.
     *
     * @return allSourceRoots of original source roots
     */
    @Override
    public synchronized String[] getOriginalSourceRoots () {
        return getSourceRoots(originalSourcePath);
    }
    
    /**
     * Returns array of source roots.
     *
     * @return array of source roots
     */
    @Override
    public synchronized String[] getSourceRoots () {
        return getSourceRoots(smartSteppingSourcePath);
    }

    public synchronized Set<FileObject> getSourceRootsFO() {
        return new HashSet<FileObject>(Arrays.asList(smartSteppingSourcePath.getRoots()));
    }
    
    /**
     * Returns the project's source roots.
     * 
     * @return array of source roots belonging to the project
     */
    public String[] getProjectSourceRoots() {
        return projectSourceRoots;
    }

    public synchronized String[] getAdditionalSourceRoots() {
        return (additionalSourceRoots == null) ? new String[] {} : additionalSourceRoots.toArray(new String[]{});
    }

    public void reorderOriginalSourceRoots(int[] permutation) {
        synchronized (this) {
        String[] srcRoots = getOriginalSourceRoots();
        if (permutation == null) {
            // Restting the order to the original
            for (int i = 0; i < sourcePathPermutation.length; i++) {
                sourcePathPermutation[i] = i;
            }
            originalSourcePath = unorderedOriginalSourcePath;
            srcRoots = getSourceRoots(unorderedOriginalSourcePath);
        } else {
            if (srcRoots.length != permutation.length) {
                throw new IllegalArgumentException("Bad length of permutation: "+permutation.length+", have "+srcRoots.length+" source roots.");
            }
            int n = permutation.length;
            String[] unorderedOriginalRoots = getSourceRoots(unorderedOriginalSourcePath);
            String[] sortedOriginalRoots = new String[n];
            // Adding the permutation
            for (int i = 0; i < n; i++) {
                permutation[i] = sourcePathPermutation[permutation[i]];
                sortedOriginalRoots[i] = unorderedOriginalRoots[permutation[i]];
            }
            System.arraycopy(permutation, 0, sourcePathPermutation, 0, n);
            originalSourcePath = createClassPath(sortedOriginalRoots);
            srcRoots = unorderedOriginalRoots;
        }
        projectSourceRoots = getSourceRoots(originalSourcePath);
        Set<String> smartSteppingRoots = new HashSet<String>(Arrays.asList(getSourceRoots(smartSteppingSourcePath)));
        String[] orderedSmartSteppingRoots = new String[smartSteppingRoots.size()];
        int i = 0;
        for (String root : projectSourceRoots) {
            if (smartSteppingRoots.contains(root)) {
                orderedSmartSteppingRoots[i++] = root;
            }
        }
        smartSteppingSourcePath = createClassPath(orderedSmartSteppingRoots);
        storeSourceRootsOrder(baseDir, srcRoots, sourcePathPermutation);
        }
        // Clear caches so that the new order is taken into account
        synchronized (urlCache) {
            urlCache.clear();
        }
        synchronized (urlCacheGlobal) {
            urlCacheGlobal.clear();
        }
    }
    
    /**
     * Sets array of source roots.
     * {@link #setSourceRoots(java.lang.String[])} can not save disabled additionalSourceRoots, since it gets
     * only *enabled* source roots
     *
     * @param sourceRoots a new array of sourceRoots
     * @param additionalRoots complete list of additional source roots (including disabled ones)
     */
    public void setSourceRoots (String[] sourceRoots, String[] additionalRoots) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("SourcePathProviderImpl.setSourceRoots("+java.util.Arrays.asList(sourceRoots)+", "+java.util.Arrays.asList(additionalRoots)+")");
        }
        /*if (sourceRootRename(sourceRoots)) {
            return ;
        }*/
        Set<String> newRoots = new LinkedHashSet<String>(Arrays.asList(sourceRoots));
        ClassPath[] oldCP_ptr = new ClassPath[] { null };
        ClassPath[] newCP_ptr = new ClassPath[] { null };
        synchronized (this) {
            Set<String> allAdditionalSourceRoots = new LinkedHashSet<String>(Arrays.asList(additionalRoots));
            int permLength = sourcePathPermutation.length;
            Set<String> disabledSourceRoots = setSourceRoots(newRoots, oldCP_ptr, newCP_ptr, allAdditionalSourceRoots);

            storeAdditionalSourceRoots();
            storeDisabledSourceRoots(disabledSourceRoots);
            if (permLength != sourcePathPermutation.length) {
                storeSourceRootsOrder(baseDir, getSourceRoots(unorderedOriginalSourcePath), sourcePathPermutation);
            }
        }
        
        // Clear caches so that the new source roots are taken into account
        synchronized (urlCache) {
            urlCache.clear();
        }
        synchronized (urlCacheGlobal) {
            urlCacheGlobal.clear();
        }

        if (oldCP_ptr[0] != null) {
            pcs.firePropertyChange (PROP_SOURCE_ROOTS, oldCP_ptr[0], newCP_ptr[0]);
        }
    }
    
    /**
     * Sets array of source roots.
     *
     * @param sourceRoots a new array of sourceRoots
     */
    @Override
    public void setSourceRoots (String[] sourceRoots) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("SourcePathProviderImpl.setSourceRoots("+java.util.Arrays.asList(sourceRoots)+")");
        }
        Set<String> newRoots = new LinkedHashSet<String>(Arrays.asList(sourceRoots));
        ClassPath[] oldCP_ptr = new ClassPath[] { null };
        ClassPath[] newCP_ptr = new ClassPath[] { null };
        synchronized (this) {
            int permLength = sourcePathPermutation.length;
            Set<String> disabledSourceRoots = setSourceRoots(newRoots, oldCP_ptr, newCP_ptr, null);
            
            storeAdditionalSourceRoots();
            storeDisabledSourceRoots(disabledSourceRoots);
            if (permLength != sourcePathPermutation.length) {
                storeSourceRootsOrder(baseDir, getSourceRoots(unorderedOriginalSourcePath), sourcePathPermutation);
            }
        }
        
        // Clear caches so that the new source roots are taken into account
        synchronized (urlCache) {
            urlCache.clear();
        }
        synchronized (urlCacheGlobal) {
            urlCacheGlobal.clear();
        }

        if (oldCP_ptr[0] != null) {
            pcs.firePropertyChange (PROP_SOURCE_ROOTS, oldCP_ptr[0], newCP_ptr[0]);
        }
    }

    private synchronized Set<String> setSourceRoots(Set<String> newRoots,
                                                    ClassPath[] oldCP_ptr,
                                                    ClassPath[] newCP_ptr,
                                                    Set<String> allAdditionalSourceRoots) {
        List<FileObject> sourcePath = new ArrayList<FileObject>(
                Arrays.asList(smartSteppingSourcePath.getRoots()));
        List<FileObject> sourcePathOriginal = new ArrayList<FileObject>(
                Arrays.asList(originalSourcePath.getRoots()));
        List<FileObject> unorderedSourcePathOriginal = new ArrayList<FileObject>(
                Arrays.asList(unorderedOriginalSourcePath.getRoots()));

        // First check whether there are some new source roots
        Set<String> newOriginalRoots = new LinkedHashSet<String>(newRoots);
        for (FileObject fo : sourcePathOriginal) {
            newOriginalRoots.remove(getRoot(fo));
        }
        if (!newOriginalRoots.isEmpty()) {
            // There are new additional source roots added. We need to update
            // unorderedOriginalSourcePath, originalSourcePath, projectSourceRoots,
            // smartSteppingSourcePath, sourcePathPermutation and additionalSourceRoots
            Set<String> addedOriginalRoots = new LinkedHashSet<String>(newOriginalRoots.size());
            for (String root : newOriginalRoots) {
                FileObject fo = getFileObject(root);
                if (fo != null && fo.canRead()) {
                    sourcePathOriginal.add(fo);
                    unorderedSourcePathOriginal.add(fo);
                    addedOriginalRoots.add(root);
                }
            }
            newOriginalRoots = addedOriginalRoots;
            if (!newOriginalRoots.isEmpty()) {
                if (additionalSourceRoots == null) {
                    additionalSourceRoots = new LinkedHashSet<String>();
                }
                additionalSourceRoots.addAll(newOriginalRoots);
            }
        }

        // Then correct the smart-stepping path
        Set<String> newSteppingRoots = new LinkedHashSet<String>(newRoots);
        for (FileObject fo : sourcePath) {
            newSteppingRoots.remove(getRoot(fo));
        }
        Set<FileObject> removedSteppingRoots = new HashSet<FileObject>();
        Set<FileObject> removedOriginalRoots = new HashSet<FileObject>();
        for (FileObject fo : sourcePath) {
            String spr = getRoot(fo);
            if (!newRoots.contains(spr)) {
                removedSteppingRoots.add(fo);
                if (additionalSourceRoots != null && additionalSourceRoots.contains(spr) &&
                    !(allAdditionalSourceRoots != null && allAdditionalSourceRoots.contains(spr))) {
                    // Remove it only if it's not among all additional source roots
                    removedOriginalRoots.add(fo);
                    additionalSourceRoots.remove(spr);
                    if (additionalSourceRoots.isEmpty()) {
                        additionalSourceRoots = null;
                    }
                }
            }
        }
        if (!removedOriginalRoots.isEmpty()) {
            sourcePathOriginal.removeAll(removedOriginalRoots);
        }
        if (!newOriginalRoots.isEmpty() || !removedOriginalRoots.isEmpty()) {
            for (FileObject fo : removedOriginalRoots) {
                int index = unorderedSourcePathOriginal.indexOf(fo);
                unorderedSourcePathOriginal.remove(index);
                int pi = sourcePathPermutation[index];
                for (int i = 0; i < sourcePathPermutation.length; i++) {
                    if (sourcePathPermutation[i] > pi) {
                        sourcePathPermutation[i]--;
                    }
                }
                for (int i = index; i < (sourcePathPermutation.length - 1); i++) {
                    sourcePathPermutation[i] = sourcePathPermutation[i+1];
                }
            }
            int n = sourcePathPermutation.length - removedOriginalRoots.size() + newOriginalRoots.size();
            int[] newSourcePathPermutation = new int[n];
            System.arraycopy(sourcePathPermutation, 0, newSourcePathPermutation, 0, sourcePathPermutation.length - removedOriginalRoots.size());
            for (int i = sourcePathPermutation.length - removedOriginalRoots.size(); i < n; i++) {
                newSourcePathPermutation[i] = i;
            }
            sourcePathPermutation = newSourcePathPermutation;
            originalSourcePath = createClassPath(sourcePathOriginal.toArray(new FileObject[0]));
            unorderedOriginalSourcePath = createClassPath(unorderedSourcePathOriginal.toArray(new FileObject[0]));
            projectSourceRoots = getSourceRoots(originalSourcePath);
        }
        if (newSteppingRoots.size() > 0 || removedSteppingRoots.size() > 0) {
            for (String root : newSteppingRoots) {
                FileObject fo = getFileObject(root);
                if (fo != null && fo.canRead()) {
                    sourcePath.add(fo);
                }
            }
            sourcePath.removeAll(removedSteppingRoots);
            oldCP_ptr[0] = smartSteppingSourcePath;
            smartSteppingSourcePath = createClassPath(sourcePath.toArray(new FileObject[0]));
            newCP_ptr[0] = smartSteppingSourcePath;
        }
        Set<FileObject> disabledRoots = new HashSet<FileObject>(sourcePathOriginal);
        disabledRoots.removeAll(sourcePath);
        Set<String> disabledSourceRoots = new HashSet<String>();
        for (FileObject fo : disabledRoots) {
            disabledSourceRoots.add(getRoot(fo));
        }
        return disabledSourceRoots;
    }

    /*
    private synchronized boolean sourceRootRename(String[] sourceRoots) {
        FileObject[] currentRoots = smartSteppingSourcePath.getRoots();
        if (currentRoots.length != sourceRoots.length) {
            return false;
        }
        int i = 0;
        int index = -1;
        String renamed = null;
        FileObject renamedFO = null;
        for (FileObject fo : currentRoots) {
            String root = getRoot(fo);
            if (root != null) {
                if (root.equals(sourceRoots[i])) {
                    if (index < 0) {
                        index = i;
                        renamed = root;
                        renamedFO = fo;
                    } else {
                        return false;
                    }
                }
            }
            i++;
        }
        String newRoot = sourceRoots[index];
        FileObject newFO = getFileObject(newRoot);
        if (newFO == null) {
            throw Exceptions.attachLocalizedMessage(new IllegalArgumentException(newRoot), newRoot+" does not exists.");
        }
        //currentRoots[index] = newRoot;
        additionalSourceRoots.remove(renamed);
        additionalSourceRoots.add(newRoot);
        List<FileObject> sourcePath = new ArrayList<FileObject>(
                Arrays.asList(smartSteppingSourcePath.getRoots()));
        List<FileObject> sourcePathOriginal = new ArrayList<FileObject>(
                Arrays.asList(originalSourcePath.getRoots()));
        List<FileObject> unorderedSourcePathOriginal = new ArrayList<FileObject>(
                Arrays.asList(unorderedOriginalSourcePath.getRoots()));
        sourcePath.set(index, newFO);
        index = sourcePathOriginal.indexOf(renamedFO);
        sourcePathOriginal.set(index, newFO);
        index = unorderedSourcePathOriginal.indexOf(renamedFO);
        unorderedSourcePathOriginal.set(index, newFO);
        smartSteppingSourcePath =
                createClassPath(
                    sourcePath.toArray(new FileObject[0]));
        originalSourcePath =
                createClassPath(
                    sourcePathOriginal.toArray(new FileObject[0]));
        unorderedOriginalSourcePath =
                createClassPath(
                    unorderedSourcePathOriginal.toArray(new FileObject[0]));
        projectSourceRoots = getSourceRoots(originalSourcePath);
        Set<String> disabledRoots;
        if (baseDir != null) {
            disabledRoots = getDisabledSourceRoots(baseDir);
        } else {
            disabledRoots = getRemoteDisabledSourceRoots();
        }
        if (disabledRoots.remove(renamed)) {
            disabledRoots.add(newRoot);
            storeDisabledSourceRoots(disabledRoots);
        }
        storeAdditionalSourceRoots();
        storeSourceRootsOrder(baseDir, getSourceRoots(unorderedOriginalSourcePath), sourcePathPermutation);
        return true;
    }
     */
    
    /**
     * Adds property change listener.
     *
     * @param l new listener.
     */
    @Override
    public void addPropertyChangeListener (PropertyChangeListener l) {
        pcs.addPropertyChangeListener (l);
    }

    /**
     * Removes property change listener.
     *
     * @param l removed listener.
     */
    @Override
    public void removePropertyChangeListener (
        PropertyChangeListener l
    ) {
        pcs.removePropertyChangeListener (l);
    }
    
    
    // helper methods ..........................................................
    
    /**
     * Normalizes the given path by removing unnecessary "." and ".." sequences.
     * This normalization is needed because the compiler stores source paths like "foo/../inc.jsp" into .class files. 
     * Such paths are not supported by our ClassPath API.
     * TODO: compiler bug? report to JDK?
     * 
     * @param path path to normalize
     * @return normalized path without "." and ".." elements
     */ 
    public static String normalize(String path) {
      for (Matcher m = thisDirectoryPattern.matcher(path); m.find(); )
      {
        path = m.replaceAll("$1");
        m = thisDirectoryPattern.matcher(path);
      }
      for (Matcher m = parentDirectoryPattern.matcher(path); m.find(); )
      {
        if (!m.group(2).equals("..")) {
          path = path.substring(0, m.start()) + m.group(1) + path.substring(m.end());
          m = parentDirectoryPattern.matcher(path);        
        }
      }
      return path;
    }
    
    /**
     * Returns source root for given ClassPath root as String, or <code>null</code>.
     */
    public static String getRoot(FileObject fileObject) {
        File f = null;
        String path = "";
        try {
            if (fileObject.getFileSystem () instanceof JarFileSystem) {
                f = ((JarFileSystem) fileObject.getFileSystem ()).getJarFile ();
                if (!fileObject.isRoot()) {
                    path = "!/"+fileObject.getPath();
                }
            } else {
                f = FileUtil.toFile (fileObject);
            }
        } catch (FileStateInvalidException ex) {
        }
        if (f != null) {
            return f.getAbsolutePath () + path;
        } else {
            return null;
        }
    }

    /**
     * Returns FileObject for given String.
     */
    private static FileObject getFileObject (String file) {
        File f = new File (file);
        FileObject fo = FileUtil.toFileObject (FileUtil.normalizeFile(f));
        String path = null;
        if (fo == null && file.contains("!/")) {
            int index = file.indexOf("!/");
            f = new File(file.substring(0, index));
            fo = FileUtil.toFileObject (f);
            path = file.substring(index + "!/".length());
        }
        if (fo != null && FileUtil.isArchiveFile (fo)) {
            fo = FileUtil.getArchiveRoot (fo);
            if (path !=null) {
                fo = fo.getFileObject(path);
            }
        }
        return fo;
    }

    public static int[] createPermutation(String[] roots, Map<String, Integer> orderIndexes, String[] sortedRoots) {
        int n = roots.length;
        if (orderIndexes == null) {
            int[] perm = new int[n];
            for (int i = 0; i < n; i++) {
                sortedRoots[i] = roots[i];
                perm[i] = i;
            }
            return perm;
        }

        class IndexedRoot {
            String root;
            Integer index;
            int order;
            IndexedRoot(String root, Integer index, int order) {
                this.root = root;
                this.index = index;
                this.order = order;
            }
        }
        IndexedRoot[] indexedRoots = new IndexedRoot[n];
        List<IndexedRoot> indexed = new ArrayList<IndexedRoot>();
        for (int i = 0; i < n; i++) {
            Integer index = orderIndexes.get(roots[i]);
            indexedRoots[i] = new IndexedRoot(roots[i], index, i);
            if (index != null) {
                indexed.add(indexedRoots[i]);
            }
        }
        class Cmp implements Comparator<IndexedRoot> {
            @Override
            public int compare(IndexedRoot ir1, IndexedRoot ir2) {
                Integer i1 = ir1.index;
                Integer i2 = ir2.index;
                return i1 - i2;
            }
        }
        Cmp cmp = new Cmp();
        if (indexed.size() == indexedRoots.length) {
            // All elements have index != null
            Arrays.sort(indexedRoots, cmp);
        } else if (!indexed.isEmpty()) {
            // Sort only the elements with index != null
            indexed.sort(cmp);
            // and merge them in in the correct order:
            int indexedi = 0;
            for (int i = 0; i < n; i++) {
                if (indexedRoots[i].index != null) {
                    indexedRoots[i] = indexed.get(indexedi++);
                }
            }
        }

        int[] perm = new int[n];
        for (int i = 0; i < n; i++) {
            sortedRoots[i] = indexedRoots[i].root;
            perm[i] = indexedRoots[i].order;
        }
        return perm;
    }
    
    private ClassPath reorder(ClassPath sourcePath, final Map<String, Integer> orderIndexes) {
        String[] roots = getSourceRoots(sourcePath);
        class Cmp implements Comparator<String> {
            @Override
            public int compare(String o1, String o2) {
                int i1 = orderIndexes.get(o1);
                int i2 = orderIndexes.get(o2);
                return i1 - i2;
            }
        }
        Cmp cmp = new Cmp();
        Arrays.sort(roots, cmp);
        return createClassPath(roots);
    }

    private static ClassPath createClassPath(String[] roots) {
        int n = roots.length;
        FileObject[] froots = new FileObject[n];
        for (int i = 0; i < n; i++) {
            froots[i] = getFileObject(roots[i]);
        }
        return createClassPath(froots);
    }

    private static ClassPath createClassPath(FileObject[] froots) {
        List<PathResourceImplementation> pris = new ArrayList<PathResourceImplementation> ();
        for (FileObject fo : froots) {
            if (fo != null && fo.canRead()) {
                try {
                    URL url = fo.toURL();
                    pris.add(ClassPathSupport.createResource(url));
                } catch (IllegalArgumentException iaex) {
                    // Can be thrown from ClassPathSupport.createResource()
                    // Ignore - bad source root
                    //logger.log(Level.INFO, "Invalid source root = "+fo, iaex);
                    logger.warning(iaex.getLocalizedMessage());
                }
            }
        }
        return ClassPathSupport.createClassPath(pris);
    }
    
    private static ClassPath createClassPath(URL[] urls) {
        List<PathResourceImplementation> pris = new ArrayList<PathResourceImplementation> ();
        for (URL url : urls) {
            FileObject fo = URLMapper.findFileObject(url);
            if (fo != null && fo.canRead()) {
                try {
                    pris.add(ClassPathSupport.createResource(url));
                } catch (IllegalArgumentException iaex) {
                    // Can be thrown from ClassPathSupport.createResource()
                    // Ignore - bad source root
                    //logger.log(Level.INFO, "Invalid source root = "+fo, iaex);
                    logger.warning(iaex.getLocalizedMessage());
                }
            }
        }
        return ClassPathSupport.createClassPath(pris);
    }
    
    private ArtifactsUpdatedImpl addArtifactsUpdateListenerFor(JPDADebugger debugger, FileObject src) {
        URL url = src.toURL();
        ArtifactsUpdatedImpl l = new ArtifactsUpdatedImpl(debugger, url, src);
        BuildArtifactMapper.addArtifactsUpdatedListener(url, l);
        return l;
    }

    private static List<URL> getURLRoots(ClassPath cp) {
        List<URL> urls = new ArrayList<URL>();
        for (Entry entry : cp.entries()) {
            URL url = entry.getURL();
            urls.add(url);
        }
        return urls;
    }

    private static boolean CAN_FIX_CLASSES_AUTOMATICALLY = Boolean.getBoolean("debugger.apply-code-changes.on-save"); // NOI18N

    private static class ArtifactsUpdatedImpl implements ArtifactsUpdated {

        private Reference<JPDADebugger> debuggerRef;
        private final URL url;
        private FileObject src;

        public ArtifactsUpdatedImpl(JPDADebugger debugger, URL url, FileObject src) {
            this.debuggerRef = new WeakReference<JPDADebugger>(debugger);
            this.url = url;
            this.src = src;
        }

        public URL getURL() {
            return url;
        }

        @Override
        public void artifactsUpdated(Iterable<File> artifacts) {
            String error = null;
            final JPDADebugger debugger = debuggerRef.get();
            if (debugger == null) {
                error = NbBundle.getMessage(SourcePathProviderImpl.class, "MSG_NoJPDADebugger");
            } else if (!debugger.canFixClasses()) {
                error = NbBundle.getMessage(SourcePathProviderImpl.class, "MSG_CanNotFix");
            } else if (debugger.getState() == JPDADebugger.STATE_DISCONNECTED) {
                error = NbBundle.getMessage(SourcePathProviderImpl.class, "MSG_NoDebug");
            }

            boolean canFixClasses = Properties.getDefault().getProperties("debugger.options.JPDA").
                    getBoolean("ApplyCodeChangesOnSave", CAN_FIX_CLASSES_AUTOMATICALLY);
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("artifactsUpdated("+artifacts+") error = '"+error+"', canFixClasses = "+canFixClasses);
            }
            if (error == null) {
                if (!canFixClasses) {
                    for (File f : artifacts) {
                        FileObject fo = FileUtil.toFileObject(f);
                        if (fo != null) {
                            String className = fileToClassName(fo);
                            if (className != null) {
                                FixClassesSupport.ClassesToReload.getInstance().addClassToReload(
                                        debugger, src, className, fo);
                            }
                        }
                    }
                    return ;
                }
                Map<String, FileObject> classes = new HashMap<String, FileObject>();
                for (File f : artifacts) {
                    FileObject fo = FileUtil.toFileObject(f);
                    if (fo != null) {
                        String className = fileToClassName(fo);
                        if (className != null) {
                            classes.put(className, fo);
                        }
                    }
                }
                FixClassesSupport.reloadClasses(debugger, classes);
            } else {
                BuildArtifactMapper.removeArtifactsUpdatedListener(url, this);
            }

            if (error != null && canFixClasses) {
                FixClassesSupport.notifyError(debugger, error);
            }
        }

        private static String fileToClassName (FileObject fo) {
            // remove ".class" from and use dots for for separator
            ClassPath cp = ClassPath.getClassPath (fo, ClassPath.EXECUTE);
            if (cp == null) {
                logger.log(Level.WARNING, "Did not find EXECUTE class path for {0}", fo);
                return null;
            }
    //        FileObject root = cp.findOwnerRoot (fo);
            return cp.getResourceName (fo, '.', false);
        }
    }

    private class PathRegistryListener implements GlobalPathRegistryListener, PropertyChangeListener {

        private RequestProcessor rp = new RequestProcessor(PathRegistryListener.class.getName(), 1);
        private RequestProcessor.Task task;
        private List<URL> addedRoots = null;
        private List<URL> removedRoots = null;
        private final Object rootsLock = new Object();

        public PathRegistryListener() {
            task = rp.create(new Runnable() {
                @Override
                public void run() {
                    rootsChanged();
                }
            });
        }
        
        @Override
        public void pathsAdded(final GlobalPathRegistryEvent event) {
            List<URL> changedPaths = getChangedPaths(event);
            if (changedPaths == null) {
                return ;
            }
            synchronized (rootsLock) {
                if (addedRoots == null) {
                    addedRoots = changedPaths;
                } else {
                    addedRoots.addAll(changedPaths);
                }
            }
            task.schedule(1000);    // Work with class path is expensive.
        }
        
        @Override
        public void pathsRemoved(final GlobalPathRegistryEvent event) {
            List<URL> changedPaths = getChangedPaths(event);
            if (changedPaths == null) {
                return ;
            }
            synchronized (rootsLock) {
                if (removedRoots == null) {
                    removedRoots = changedPaths;
                } else {
                    removedRoots.addAll(changedPaths);
                }
            }
            task.schedule(1000);    // Work with class path is expensive.
        }
        
        private List<URL> getChangedPaths(final GlobalPathRegistryEvent event) {
            if (!ClassPath.SOURCE.equals(event.getId())) {
                return null;
            }
            List<URL> urls = new ArrayList<URL>();
            for (ClassPath cp : event.getChangedPaths()) {
                for (Entry entry : cp.entries()) {
                    URL url = entry.getURL();
                    urls.add(url);
                }
            }
            return urls;
        }
        
        private void rootsChanged() {
            List<URL> added;
            List<URL> removed;
            synchronized (rootsLock) {
                added = addedRoots;
                removed = removedRoots;
                addedRoots = null;
                removedRoots = null;
            }
            boolean changed = false;
            if (added != null && added.size() > 0) {
                synchronized (SourcePathProviderImpl.this) {
                    if (originalSourcePath == null) {
                        return ;
                    }
                    List<URL> sourcePaths = getURLRoots(originalSourcePath);
                    sourcePaths.addAll(added);
                    originalSourcePath = SourcePathProviderImpl.createClassPath(sourcePaths.toArray(new URL[0]));

                    sourcePaths = getURLRoots(smartSteppingSourcePath);
                    sourcePaths.addAll(added);
                    smartSteppingSourcePath = SourcePathProviderImpl.createClassPath(sourcePaths.toArray(new URL[0]));
                }
                changed = true;
            }
            if (removed != null && removed.size() > 0) {
                synchronized (SourcePathProviderImpl.this) {
                    if (originalSourcePath == null) {
                        return ;
                    }
                    List<URL> sourcePaths = getURLRoots(originalSourcePath);
                    sourcePaths.removeAll(removed);
                    originalSourcePath =
                            SourcePathProviderImpl.createClassPath(
                                sourcePaths.toArray(new URL[0]));

                    sourcePaths = getURLRoots(smartSteppingSourcePath);
                    sourcePaths.removeAll(removed);
                    smartSteppingSourcePath =
                            SourcePathProviderImpl.createClassPath(
                                sourcePaths.toArray(new URL[0]));
                }
                changed = true;
            }
            if (changed) {
                // Clear caches so that the new source roots are taken into account
                synchronized (urlCache) {
                    urlCache.clear();
                }
                synchronized (urlCacheGlobal) {
                    urlCacheGlobal.clear();
                }
                pcs.firePropertyChange (PROP_SOURCE_ROOTS, null, null);
            }
        }

        @Override
        public void propertyChange(final PropertyChangeEvent evt) {
            // JDK sources changed
            // Work with class path is expensive. Move it off AWT.
            if (EventQueue.isDispatchThread()) {
                rp.post(new Runnable() {
                    @Override
                    public void run() {
                        propertyChange(evt);
                    }
                });
                return ;
            }
            JavaPlatform[] platforms = JavaPlatformManager.getDefault ().
                getInstalledPlatforms ();
            boolean changed = false;
            synchronized (SourcePathProviderImpl.this) {
                if (originalSourcePath == null) {
                    return ;
                }
                platformSourceRoots.clear();
                List<FileObject> sourcePaths = new ArrayList<FileObject>(
                        Arrays.asList(originalSourcePath.getRoots()));
                for(JavaPlatform jp : platforms) {
                    FileObject[] roots = jp.getSourceFolders().getRoots ();
                    for (FileObject fo : roots) {
                        if (!sourcePaths.contains(fo)) {
                            sourcePaths.add(fo);
                            changed = true;
                        }
                    }
                    platformSourceRoots.addAll(getSourceRootsSet(jp.getSourceFolders()));
                }
                if (changed) {
                    originalSourcePath =
                            SourcePathProviderImpl.createClassPath(
                                sourcePaths.toArray(new FileObject[0]));
                }
            }
            if (changed) {
                // Clear caches so that the new source roots are taken into account
                synchronized (urlCache) {
                    urlCache.clear();
                }
                synchronized (urlCacheGlobal) {
                    urlCacheGlobal.clear();
                }
                pcs.firePropertyChange (PROP_SOURCE_ROOTS, null, null);
            }
        }
    }
    
    public static final class FileObjectComparator implements Comparator<FileObject> {

        @Override
        public int compare(FileObject fo1, FileObject fo2) {
            String r1 = getRoot(fo1);
            String r2 = getRoot(fo2);
            if (r1 == null) {
                return -1;
            }
            if (r2 == null) {
                return +1;
            }
            return r1.compareTo(r2);
        }
        
    }

    private static final class URLCacheMap extends LinkedHashMap<String, String> {

        private static final int URL_CACHE_SIZE = 500;

        public URLCacheMap() {
            super(URL_CACHE_SIZE, .1f, true);
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
            return size() >= URL_CACHE_SIZE;
        }

    }
}
