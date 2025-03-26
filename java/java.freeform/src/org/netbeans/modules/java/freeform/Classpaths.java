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

package org.netbeans.modules.java.freeform;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.ant.freeform.spi.support.Util;
import org.netbeans.modules.java.freeform.jdkselection.JdkConfiguration;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.FilteringPathResourceImplementation;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.PathMatcher;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Element;

/**
 * Handle classpaths for the freeform project.
 * Keeps three caches:
 * <ol>
 * <li>Classpaths registered when the project is opened. The same are unregistered
 *     when it is closed again, regardless of what might have changed since.
 * <li>A map from abstract compilation units (keyed by the literal text of the
 *     <code>&lt;package-root&gt;</code> elements) to implementations which do the
 *     actual listening and (re-)computation of roots.
 * <li>A map from actual package roots to the matching classpath.
 * </ol>
 * The complexity here is needed because
 * <ol>
 * <li>It is necessary to always unregister the exact same set of ClassPath objects
 *     you initially registered (even if some have since become invalid, etc.).
 *     Ideally, adding or removing whole paths would dynamically register or unregister
 *     them (if the project is currently open); the current code does not do this.
 * <li>A given ClassPath object must fire changes if its list of roots changes.
 * <li>It is necessary to return the same ClassPath object for the same FileObject.
 * </ol>
 * @author Jesse Glick
 */
final class Classpaths implements ClassPathProvider, AntProjectListener, PropertyChangeListener {
    
    private static final ErrorManager err = ErrorManager.getDefault().getInstance(Classpaths.class.getName());

    //for tests only:
    static CountDownLatch TESTING_LATCH = null;
    
    private final AntProjectHelper helper;
    private final PropertyEvaluator evaluator;
    private final AuxiliaryConfiguration aux;
    private final SourceForBinaryQueryImpl sfbqImpl;
    
    /**
     * Map from classpath types to maps from package roots to classpaths.
     */
    private final Map<String,Map<FileObject,ClassPath>> classpaths = new HashMap<String,Map<FileObject,ClassPath>>();
    
    /**
     * Map from classpath types to maps from lists of package root names to classpath impls.
     */
    private final Map<String,Map<List<String>,MutableClassPathImplementation>> mutablePathImpls = new HashMap<String,Map<List<String>,MutableClassPathImplementation>>();
    
    private final Map<MutableClassPathImplementation,ClassPath> mutableClassPathImpl2ClassPath = new HashMap<MutableClassPathImplementation,ClassPath>();
    
    /**
     * Map from classpath types to sets of classpaths we last registered to GlobalPathRegistry.
     */
    //@GuardedBy(this)
    private Map<String,Set<ClassPath>> registeredClasspaths = null;

    public Classpaths(
            @NonNull final AntProjectHelper helper,
            @NonNull final PropertyEvaluator evaluator,
            @NonNull final AuxiliaryConfiguration aux,
            @NonNull final SourceForBinaryQueryImpl sfbqImpl) {
        this.helper = helper;
        this.evaluator = evaluator;
        this.aux = aux;
        this.sfbqImpl = sfbqImpl;
        helper.addAntProjectListener(this);
        evaluator.addPropertyChangeListener(this);
    }
    
    public ClassPath findClassPath(final FileObject file, final String type) {
        //#77015: the findClassPathImpl method takes read access on ProjectManager.mutex
        //taking the read access before the private lock to prevent deadlocks.
        return ProjectManager.mutex().readAccess(new Mutex.Action<ClassPath>() {
            public ClassPath run() {
                return findClassPathImpl(file, type);
            }
        });
    }

    private synchronized ClassPath findClassPathImpl(FileObject file, String type) {
        if (TESTING_LATCH != null) {
            //only for tests:
            TESTING_LATCH.countDown();
            try {
                TESTING_LATCH.await(2, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                ErrorManager.getDefault().notify(e);
            }
            
            classpaths.clear();
        }
        
        Map<FileObject,ClassPath> classpathsByType = classpaths.get(type);
        if (classpathsByType == null) {
            classpathsByType = new WeakHashMap<FileObject,ClassPath>();
            classpaths.put(type, classpathsByType);
        }
        // Check for cached value.
        for (Map.Entry entry : classpathsByType.entrySet()) {
            FileObject root = (FileObject)entry.getKey();
            if (root == file || FileUtil.isParentOf(root, file)) {
                // Already have it.
                return (ClassPath)entry.getValue();
            }
        }
        // Need to create it.
        Element java = aux.getConfigurationFragment(JavaProjectNature.EL_JAVA, JavaProjectNature.NS_JAVA_LASTEST, true);
        if (java == null) {
            return null;
        }
        List<Element> compilationUnits = XMLUtil.findSubElements(java);

        for (Element compilationUnitEl : compilationUnits) {
            assert compilationUnitEl.getLocalName().equals("compilation-unit") : compilationUnitEl;
            List<FileObject> packageRoots = findPackageRoots(helper, evaluator, compilationUnitEl);
            for (FileObject root : packageRoots) {
                if (root == file || FileUtil.isParentOf(root, file)) {
                    // Got it. Compute classpath and cache it (for each root).
                    ClassPath cp = getPath(compilationUnitEl, packageRoots, type);

                    for (FileObject root2 : packageRoots) {
                        classpathsByType.put(root2, cp);
                    }
                    return cp;
                }
            }
        }
        return null;
    }
    
    /** All classpath types we handle. */
    private static final String[] TYPES = {
        ClassPath.SOURCE,
        ClassPath.BOOT,
        ClassPath.EXECUTE,
        ClassPath.COMPILE,
        JavaClassPathConstants.PROCESSOR_PATH,
    };
    
    /**
     * Called when project is opened.
     * Tries to find all compilation units and calculate all the paths needed
     * for each of them and register them all.
     */
    public void opened() {
        // #97366: taking read access to prevent deadlock, same trick as #77015
        ProjectManager.mutex().readAccess(new Mutex.Action<Void>() {
            public Void run() {
                openedImpl();
                return null;
            }
        });
    }
    
    private void openedImpl() {
        //Threading: confinement within a local scope
        Map<String,Set<ClassPath>> _registeredClasspaths;
        synchronized (this) {
            if (registeredClasspaths != null) {
                //Threading: When already assigned the thread has to leave the method in
                //the synchronized block to prevent a multiple registration of class paths.
                return;
            }
            _registeredClasspaths = new HashMap<String,Set<ClassPath>>();
            for (String type : TYPES) {
                _registeredClasspaths.put(type, new HashSet<ClassPath>());
            }
            Element java = aux.getConfigurationFragment(JavaProjectNature.EL_JAVA, JavaProjectNature.NS_JAVA_LASTEST, true);
            if (java == null) {
                return;
            }
            for (Element compilationUnitEl : XMLUtil.findSubElements(java)) {
                assert compilationUnitEl.getLocalName().equals("compilation-unit") : compilationUnitEl;
                // For each compilation unit, find the package roots first.
                List<FileObject> packageRoots = findPackageRoots(helper, evaluator, compilationUnitEl);
                for (String type : TYPES) {
                    // Then for each type, collect the classpath (creating it as needed).
                    Map<FileObject,ClassPath> classpathsByType = classpaths.get(type);
                    if (classpathsByType == null) {
                        classpathsByType = new WeakHashMap<FileObject,ClassPath>();
                        classpaths.put(type, classpathsByType);
                    }
                    Set<ClassPath> registeredClasspathsOfType = _registeredClasspaths.get(type);
                    assert registeredClasspathsOfType != null;
                    // Check if there is already a ClassPath registered to one of these roots.
                    ClassPath cp = null;
                    for (FileObject root : packageRoots) {
                        cp = classpathsByType.get(root);
                        if (cp != null) {
                            break;
                        }
                    }
                    if (cp == null) {
                        // Nope. Calculate and register it now.
                        cp = getPath(compilationUnitEl, packageRoots, type);
                        for (FileObject root : packageRoots) {
                            classpathsByType.put(root, cp);
                        }
                    }
                    assert cp != null;
                    registeredClasspathsOfType.add(cp);
                }
            }
            if (err.isLoggable(ErrorManager.INFORMATIONAL)) {
                err.log("classpaths for " + helper.getProjectDirectory() + ": " + classpaths);
            }
            // Don't do this before it is calculated, or a runtime error above might corrupt state:
            this.registeredClasspaths = _registeredClasspaths;
        }   //End synchronized (this), POST: _registeredClasspaths != null        
            assert _registeredClasspaths != null;
            // Register all of the classpaths we found.
            GlobalPathRegistry gpr = GlobalPathRegistry.getDefault();
            for (String type : TYPES) {
                Set<ClassPath> registeredClasspathsOfType = _registeredClasspaths.get(type);
                gpr.register(type, registeredClasspathsOfType.toArray(new ClassPath[0]));
            }
    }
    
    //XXX: Threading: calls non private code under lock
    private synchronized void registerNewClasspath(String type, ClassPath cp) {
        if (registeredClasspaths == null) {
            return;
        }
        Set<ClassPath> s = registeredClasspaths.get(type);
        s.add(cp);
        GlobalPathRegistry.getDefault().register(type, new ClassPath[] {cp});
    }
    
    /**
     * Called when project is closed.
     * Unregisters any previously registered classpaths.
     */
    public void closed() {
        Map<String,Set<ClassPath>> toUnregister;
        synchronized (this) {
            if (registeredClasspaths == null) {
                return;
            }
            toUnregister = new HashMap<String,Set<ClassPath>>(registeredClasspaths);
            registeredClasspaths = null;
        }
        GlobalPathRegistry gpr = GlobalPathRegistry.getDefault();
        for (String type : TYPES) {
            Set<ClassPath> registeredClasspathsOfType = toUnregister.get(type);
            gpr.unregister(type, registeredClasspathsOfType.toArray(new ClassPath[0]));
        }
    }
    
    static List<String> findPackageRootNames(Element compilationUnitEl) {
        List<String> names = new ArrayList<String>();
        for (Element e : XMLUtil.findSubElements(compilationUnitEl)) {
            if (!e.getLocalName().equals("package-root")) { // NOI18N
                continue;
            }
            String location = XMLUtil.findText(e);
            names.add(location);
        }
        return names;
    }
    
    static Map<String,FileObject> findPackageRootsByName(AntProjectHelper helper, PropertyEvaluator evaluator, List<String> packageRootNames) {
        Map<String,FileObject> roots = new LinkedHashMap<String,FileObject>();
        for (String location : packageRootNames) {
            String locationEval = evaluator.evaluate(location);
            if (locationEval != null) {
                File locationFile = helper.resolveFile(locationEval);
                FileObject locationFileObject = FileUtil.toFileObject(locationFile);
                if (locationFileObject != null) {
                    if (FileUtil.isArchiveFile(locationFileObject)) {
                        locationFileObject = FileUtil.getArchiveRoot(locationFileObject);
                    }
                    roots.put(location, locationFileObject);
                }
            }
        }
        return roots;
    }
    
    private static List<FileObject> findPackageRoots(AntProjectHelper helper, PropertyEvaluator evaluator, List<String> packageRootNames) {
        return new ArrayList<FileObject>(findPackageRootsByName(helper, evaluator, packageRootNames).values());
    }
    
    public static List<FileObject> findPackageRoots(AntProjectHelper helper, PropertyEvaluator evaluator, Element compilationUnitEl) {
        return findPackageRoots(helper, evaluator, findPackageRootNames(compilationUnitEl));
    }
    
    private ClassPath getPath(Element compilationUnitEl, List<FileObject> packageRoots, String type) {
        if (type.equals(ClassPath.SOURCE) || type.equals(ClassPath.COMPILE) ||
                type.equals(ClassPath.EXECUTE) || type.equals(ClassPath.BOOT) ||
                type.equals(JavaClassPathConstants.PROCESSOR_PATH)) {
            List<String> packageRootNames = findPackageRootNames(compilationUnitEl);
            Map<List<String>,MutableClassPathImplementation> mutablePathImplsByType;
            synchronized (this) {
                mutablePathImplsByType = mutablePathImpls.get(type);
                if (mutablePathImplsByType == null) {
                    mutablePathImplsByType = new HashMap<List<String>,MutableClassPathImplementation>();
                    mutablePathImpls.put(type, mutablePathImplsByType);
                }
            MutableClassPathImplementation impl = mutablePathImplsByType.get(packageRootNames);
            if (impl == null) {
                // XXX will it ever not be null?
                impl = new MutableClassPathImplementation(packageRootNames, type, compilationUnitEl);
                mutablePathImplsByType.put(packageRootNames, impl);
            }
            ClassPath cp = mutableClassPathImpl2ClassPath.get(impl);
            if (cp == null) {
                cp = ClassPathFactory.createClassPath(impl);
                mutableClassPathImpl2ClassPath.put(impl, cp);
                registerNewClasspath(type, cp);
            }
            return cp;
            }
        } else {
            // Unknown.
            return null;
        }
    }
    
    private List<URL> createSourcePath(List<String> packageRootNames) {
        List<URL> roots = new ArrayList<URL>(packageRootNames.size());
        for (String location : packageRootNames) {
            String locationEval = evaluator.evaluate(location);
            if (locationEval != null) {
                roots.add(createClasspathEntry(locationEval));
            }
        }
        return roots;
    }
    
    private List<URL> createCompileClasspath(Element compilationUnitEl) {
        for (Element e : XMLUtil.findSubElements(compilationUnitEl)) {
            if (e.getLocalName().equals("classpath") && e.getAttribute("mode").equals("compile")) { // NOI18N
                return createClasspath(e, new RemoveSources(helper, sfbqImpl));
            }
        }
        // None specified; assume it is empty.
        return Collections.emptyList();
    }
    
    /**
     * Create a classpath from a &lt;classpath&gt; element.
     */
    private List<URL> createClasspath(
            final Element classpathEl,
            final Function<URL,Collection<URL>> translate) {
        String cp = XMLUtil.findText(classpathEl);
        if (cp == null) {
            cp = "";
        }
        String cpEval = evaluator.evaluate(cp);
        if (cpEval == null) {
            return Collections.emptyList();
        }
        final String[] path = PropertyUtils.tokenizePath(cpEval);
        final List<URL> res = new ArrayList<>();
        for (String pathElement : path) {
            res.addAll(translate.apply(createClasspathEntry(pathElement)));
        }
        return res;
    }
    
    private URL createClasspathEntry(String text) {
        File entryFile = helper.resolveFile(text);
        return FileUtil.urlForArchiveOrDir(entryFile);
    }
    
    private List<URL> createExecuteClasspath(List<String> packageRoots, Element compilationUnitEl) {
        for (Element e : XMLUtil.findSubElements(compilationUnitEl)) {
            if (e.getLocalName().equals("classpath") && e.getAttribute("mode").equals("execute")) { // NOI18N
                return createClasspath(e, new RemoveSources(helper, sfbqImpl));
            }
        }
        // None specified; assume it is same as compile classpath plus (cf. #49113) <built-to> dirs/JARs
        // if there are any (else include the source dir(s) as a fallback for the I18N wizard to work).
        Set<URL> urls = new LinkedHashSet<>();
        urls.addAll(createCompileClasspath(compilationUnitEl));
        final Project prj = FileOwnerQuery.getOwner(helper.getProjectDirectory());
        if (prj != null) {
            for (URL src : createSourcePath(packageRoots)) {
                urls.addAll(sfbqImpl.findBinaryRoots(src));
            }
        }
        return new ArrayList<>(urls);
    }

    private List<URL> createProcessorClasspath(Element compilationUnitEl) {
        final Element ap = XMLUtil.findElement(compilationUnitEl, AnnotationProcessingQueryImpl.EL_ANNOTATION_PROCESSING, JavaProjectNature.NS_JAVA_LASTEST);
        if (ap != null) {
            final Element path = XMLUtil.findElement(ap, AnnotationProcessingQueryImpl.EL_PROCESSOR_PATH, JavaProjectNature.NS_JAVA_LASTEST);
            if (path != null) {
                return createClasspath(path, new RemoveSources(helper, sfbqImpl));
            }
        }
        // None specified; assume it is the same as the compile classpath.
        return createCompileClasspath(compilationUnitEl);
    }

    private List<URL> createBootClasspath(Element compilationUnitEl) {
        for (Element e : XMLUtil.findSubElements(compilationUnitEl)) {
            if (e.getLocalName().equals("classpath") && e.getAttribute("mode").equals("boot")) { // NOI18N
                return createClasspath(e, new Function<URL,Collection<URL>>() {
                    @Override
                    public Collection<URL> apply(URL p) {
                        return Collections.singleton(p);
                    }
                });
            }
        }
        // None specified;
        // First check whether user has configured a specific JDK.
        JavaPlatform platform = new JdkConfiguration(null, helper, evaluator).getSelectedPlatform();
        if (platform == null) {
            // Nope; Use default one
            JavaPlatformManager jpm = JavaPlatformManager.getDefault();
            platform = jpm.getDefaultPlatform(); // fallback
            // #126216: source level guessing logic removed
        }
        if (platform != null) {
            // XXX this is not ideal; should try to reuse the ClassPath as is?
            // The current impl will not listen to changes in the platform classpath correctly.
            List<ClassPath.Entry> entries = platform.getBootstrapLibraries().entries();
            List<URL> urls = new ArrayList<URL>(entries.size());
            for (ClassPath.Entry entry : entries) {
                urls.add(entry.getURL());
            }
            return urls;
        } else {
            assert false : "JavaPlatformManager has no default platform";
            return Collections.emptyList();
        }
    }

    public void configurationXmlChanged(AntProjectEvent ev) {
        pathsChanged();
    }

    public void propertiesChanged(AntProjectEvent ev) {
        pathsChanged(); // in case it is nbjdk.properties
    }

    public void propertyChange(PropertyChangeEvent evt) {
        pathsChanged();
    }
    
    private void pathsChanged() {
        synchronized (this) {
            classpaths.clear();
            for (Map<List<String>,MutableClassPathImplementation> m : mutablePathImpls.values()) {
                for (MutableClassPathImplementation impl : m.values()) {
                    impl.change();
                }
            }
        }
    }

    /**
     * Representation of one path.
     * Listens to changes in project.xml and/or evaluator and responds.
     */
    private final class MutableClassPathImplementation implements ClassPathImplementation {
        
        private final List<String> packageRootNames;
        private final String type;
        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
        private List<URL> roots; // should always be non-null
        private List<PathResourceImplementation> resources;
        
        public MutableClassPathImplementation(List<String> packageRootNames, String type, Element initialCompilationUnit) {
            this.packageRootNames = packageRootNames;
            this.type = type;
            initRoots(initialCompilationUnit);
        }
        
        private Element findCompilationUnit() {
            Element java = aux.getConfigurationFragment(JavaProjectNature.EL_JAVA, JavaProjectNature.NS_JAVA_LASTEST, true);
            if (java == null) {
                return null;
            }
            List<Element> compilationUnits = XMLUtil.findSubElements(java);
            for (Element compilationUnitEl : compilationUnits) {
                assert compilationUnitEl.getLocalName().equals("compilation-unit") : compilationUnitEl;
                if (packageRootNames.equals(findPackageRootNames(compilationUnitEl))) {
                    // Found a matching compilation unit.
                    return compilationUnitEl;
                }
            }
            // Did not find it.
            return null;
        }
        
        /**
         * Initialize list of URL roots.
         */
        private boolean initRoots(Element compilationUnitEl) {
            List<URL> oldRoots = roots;
            if (compilationUnitEl != null) {
                if (type.equals(ClassPath.SOURCE)) {
                    roots = createSourcePath(packageRootNames);
                } else if (type.equals(ClassPath.COMPILE)) {
                    roots = createCompileClasspath(compilationUnitEl);
                } else if (type.equals(ClassPath.EXECUTE)) {
                    roots = createExecuteClasspath(packageRootNames, compilationUnitEl);
                } else if (type.equals(JavaClassPathConstants.PROCESSOR_PATH)) {
                    roots = createProcessorClasspath(compilationUnitEl);
                } else {
                    assert type.equals(ClassPath.BOOT) : type;
                    roots = createBootClasspath(compilationUnitEl);
                }
            } else {
                // Dead.
                roots = Collections.emptyList();
            }
            assert roots != null;
            if (!roots.equals(oldRoots)) {
                resources = new ArrayList<PathResourceImplementation>(roots.size());
                for (URL root : roots) {
                    if (root != null) {
                        assert root.toExternalForm().endsWith("/") : "Had bogus roots " + roots + " for type " + type + " in " + helper.getProjectDirectory();
                        PathResourceImplementation pri;
                        if (type.equals(ClassPath.SOURCE)) {
                            pri = new SourcePRI(root);
                        } else {
                            pri = ClassPathSupport.createResource(root);
                        }
                        resources.add(pri);
                    }
                }
                return true;
            } else {
                return false;
            }
        }

        public List<PathResourceImplementation> getResources() {
            assert resources != null;
            return resources;
        }

        /**
         * Notify impl of a possible change in data.
         */
        public void change() {
            if (initRoots(findCompilationUnit())) {
                if (err.isLoggable(ErrorManager.INFORMATIONAL)) {
                    err.log("MutableClassPathImplementation.change: packageRootNames=" + packageRootNames + " type=" + type + " roots=" + roots);
                }
                pcs.firePropertyChange(ClassPathImplementation.PROP_RESOURCES, null, null);
            }
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            pcs.addPropertyChangeListener(listener);
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
            pcs.removePropertyChangeListener(listener);
        }
        
    }
    
    private final class SourcePRI implements FilteringPathResourceImplementation, PropertyChangeListener, AntProjectListener {
        private final URL root;
        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
        private PathMatcher matcher;
        private String includes, excludes;
        public SourcePRI(URL root) {
            this.root = root;
            helper.addAntProjectListener(WeakListeners.create(AntProjectListener.class, this, helper));
            evaluator.addPropertyChangeListener(WeakListeners.propertyChange(this, evaluator));
            computeMatcher();
        }
        private boolean computeMatcher() {
            String incl = null;
            String excl = null;
            URI rootURI = URI.create(root.toExternalForm());
            // Annoying to duplicate logic from FreeformSources.
            // But using SourceGroup.contains is not an option since that requires FileObject creation.
            File rootFolder;
            try {
                rootFolder = Utilities.toFile(rootURI);
            } catch (IllegalArgumentException x) {
                Logger.getLogger(Classpaths.class.getName()).warning("Illegal source root: " + rootURI);
                rootFolder = null;
            }
            Element genldata = Util.getPrimaryConfigurationData(helper);
            Element foldersE = XMLUtil.findElement(genldata, "folders", Util.NAMESPACE); // NOI18N
            if (foldersE != null) {
                for (Element folderE : XMLUtil.findSubElements(foldersE)) {
                    if (folderE.getLocalName().equals("source-folder")) {
                        Element typeE = XMLUtil.findElement(folderE, "type", Util.NAMESPACE); // NOI18N
                        if (typeE != null) {
                            String type = XMLUtil.findText(typeE);
                            if (type.equals(JavaProjectConstants.SOURCES_TYPE_JAVA)) {
                                Element locationE = XMLUtil.findElement(folderE, "location", Util.NAMESPACE); // NOI18N
                                String location = evaluator.evaluate(XMLUtil.findText(locationE));
                                if (location != null && helper.resolveFile(location).equals(rootFolder)) {
                                    Element includesE = XMLUtil.findElement(folderE, "includes", Util.NAMESPACE); // NOI18N
                                    if (includesE != null) {
                                        incl = evaluator.evaluate(XMLUtil.findText(includesE));
                                        if (incl != null && incl.matches("\\$\\{[^}]+\\}")) { // NOI18N
                                            // Clearly intended to mean "include everything".
                                            incl = null;
                                        }
                                    }
                                    Element excludesE = XMLUtil.findElement(folderE, "excludes", Util.NAMESPACE); // NOI18N
                                    if (excludesE != null) {
                                        excl = evaluator.evaluate(XMLUtil.findText(excludesE));
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (!Utilities.compareObjects(incl, includes) || !Utilities.compareObjects(excl, excludes)) {
                includes = incl;
                excludes = excl;
                matcher = new PathMatcher(incl, excl, rootFolder);
                return true;
            } else {
                if (matcher == null) {
                    matcher = new PathMatcher(incl, excl, rootFolder);
                }
                return false;
            }
        }
        public URL[] getRoots() {
            return new URL[] {root};
        }
        public boolean includes(URL root, String resource) {
            return matcher.matches(resource, true);
        }
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            pcs.addPropertyChangeListener(listener);
        }
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            pcs.removePropertyChangeListener(listener);
        }
        public ClassPathImplementation getContent() {
            return null;
        }
        public void propertyChange(PropertyChangeEvent ev) {
            change(ev);
        }
        public void configurationXmlChanged(AntProjectEvent ev) {
            change(ev);
        }
        public void propertiesChanged(AntProjectEvent ev) {}
        private void change(Object propid) {
            if (computeMatcher()) {
                PropertyChangeEvent ev = new PropertyChangeEvent(this, FilteringPathResourceImplementation.PROP_INCLUDES, null, null);
                ev.setPropagationId(propid);
                pcs.firePropertyChange(ev);
            }
        }
    }

    private static final class RemoveSources implements Function<URL,Collection<URL>> {
        private final Set<URL> sourceRoots;
        private final SourceForBinaryQueryImpl sfbqImpl;

        RemoveSources(
                @NonNull final AntProjectHelper helper,
                @NonNull final SourceForBinaryQueryImpl sfbqImpl) {
            this.sourceRoots = new HashSet<>();
            this.sfbqImpl = sfbqImpl;
            final Project prj = FileOwnerQuery.getOwner(helper.getProjectDirectory());
            if (prj != null) {
                for (SourceGroup sg : ProjectUtils.getSources(prj).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)) {
                    final FileObject root = sg.getRootFolder();
                    if (root != null) {
                        sourceRoots.add(root.toURL());
                    }
                }
            }
        }

        @Override
        public Collection<URL> apply(URL p) {
            Collection<URL> res = Collections.emptySet();
            if (sourceRoots.contains(p)) {
                res = sfbqImpl.findBinaryRoots(p);
            }
            if (res.isEmpty()) {
                res = Collections.singletonList(p);
            }
            return res;
        }
    }
}
