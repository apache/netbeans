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

package org.netbeans.core.startup;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.Events;
import org.netbeans.InvalidException;
import org.netbeans.Module;
import org.netbeans.ModuleInstaller;
import org.netbeans.ModuleManager;
import org.netbeans.Stamps;
import org.netbeans.Util;
import org.netbeans.core.startup.layers.ModuleLayeredFileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.modules.Dependency;
import org.openide.modules.ModuleInfo;
import org.openide.modules.ModuleInstall;
import org.openide.modules.SpecificationVersion;
import org.openide.util.BaseUtilities;
import org.openide.util.NbCollections;
import org.openide.util.SharedClassObject;
import org.openide.util.NbBundle;
import org.openide.util.Task;
import org.openide.util.lookup.InstanceContent;


/** Concrete implementation of the module installation functionality.
 * This class can pay attention to the details of manifest format,
 * details of how to install particular sections and layers and so on.
 * It may have a limited UI, i.e. notifying problems with localized
 * messages and the like.
 * @author Jesse Glick, Jan Pokorsky, Jaroslav Tulach, et al.
 */
final class NbInstaller extends ModuleInstaller {

    private static final Logger LOG = Logger.getLogger(NbInstaller.class.getName());

    /** set of manifest sections for each module */
    private final Map<Module,Set<ManifestSection>> sections = new HashMap<Module,Set<ManifestSection>>(100);
    /** ModuleInstall classes for each module that declares one */
    private final Map<Module,Class<? extends ModuleInstall>> installs = new HashMap<Module,Class<? extends ModuleInstall>>(100);
    /** layer resources for each module that declares one */
    private final Map<Module,String> layers = new HashMap<Module,String>(100);
    /** exact use of this is hard to explain */
    private boolean initializedFolderLookup = false;
    /** where to report events to */
    private final Events ev;
    /** associated controller of module list; needed for handling ModuleInstall ser */
    private ModuleList moduleList;
    /** associated manager */
    private ModuleManager mgr;
    /** set of permitted core or package dependencies from a module */
    private final Map<Module,Set<String>> kosherPackages = new HashMap<Module,Set<String>>(100);
    /** classpath ~ JRE packages to be hidden from a module */
    private final Map<Module,List<Module.PackageExport>> hiddenClasspathPackages = new  HashMap<Module,List<Module.PackageExport>>();
    /** #164510: similar to {@link #hiddenClasspathPackages} but backwards for efficiency */
    private final Map<Module.PackageExport,List<Module>> hiddenClasspathPackagesReverse = new HashMap<Module.PackageExport,List<Module>>();
    /** caches important values from module manifests */
    private final Cache cache = new Cache();
    /** Processing @OnStart/@OnStop calls */
    private final NbStartStop onStartStop = new NbStartStop(null, null);
        
    /** Create an NbInstaller.
     * You should also call {@link #registerManager} and if applicable
     * {@link #registerList} to make the installer fully usable.
     * @param ev events to log
     */
    public NbInstaller(Events ev) {
        this.ev = ev;
    }
    
    /** Access from ModuleSystem. */
    void registerList(ModuleList list) {
        if (moduleList != null) throw new IllegalStateException();
        moduleList = list;
    }
    void registerManager(ModuleManager manager) {
        if (mgr != null) throw new IllegalStateException();
        mgr = manager;
    }

    // @SuppressWarnings("unchecked")
    @Override
    public void prepare(Module m) throws InvalidException {
        ev.log(Events.PREPARE, m);
        checkForHiddenPackages(m);
        Set<ManifestSection> mysections = null;
        Class<?> clazz = null;
        
        String processSections = cache.findGlobalProperty("processSections", null, "false"); // NOI18N
        if (!"false".equals(processSections)) { // NOI18N
            // Find and load manifest sections.
            Manifest mani = m.getManifest();
            if (mani == null) {
                throw new InvalidException(m, "no manifest");
            }
            for (Map.Entry<String,Attributes> entry : mani.getEntries().entrySet()) {
                ManifestSection section = ManifestSection.create(entry.getKey(), entry.getValue(), m);
                if (section != null) {
                    if (mysections == null) {
                        mysections = new HashSet<ManifestSection>(25);
                    }
                    mysections.add(section);
                }
            }
            if (mysections != null) {
                cache.findGlobalProperty("processSections", "false", "true"); // NOI18N
            }
        }
        String installClass = cache.findProperty(m, "OpenIDE-Module-Install", false); // NOI18N
        if (installClass != null) {
            String installClassName;
            try {
                installClassName = Util.createPackageName(installClass);
            } catch (IllegalArgumentException iae) {
                InvalidException ie = new InvalidException(m, iae.toString());
                ie.initCause(iae);
                throw ie;
            }
            if (installClass.endsWith(".ser")) throw new InvalidException(m, "Serialized module installs not supported: " + installClass); // NOI18N
            try {
                // We could simply load the ModuleInstall right away in all cases.
                // However consider a ModuleInstall that has a static or instance
                // initializer or initialize() calling NbBundle.getBundle(String). In the
                // old module installer that would work and it was never specifically discouraged,
                // so that needs to continue working. So do not resolve in most cases;
                // if the module specifically has a validate method, then this is clearly
                // documented to be called before systemClassLoader is ready, so the onus
                // is on the module author to avoid anything dangerous there.
                clazz = Class.forName(installClassName, false, m.getClassLoader());
                if (clazz.getClassLoader() != m.getClassLoader()) {
                    ev.log(Events.WRONG_CLASS_LOADER, m, clazz, m.getClassLoader());
                }
                // Search for a validate() method; if there is none, do not resolve the class now!
                Class<?> c;
                for (c = clazz; c != ModuleInstall.class && c != Object.class; c = c.getSuperclass()) {
                    try {
                        // #13997: do not search in superclasses.
                        c.getDeclaredMethod("validate"); // NOI18N
                        // It has one. We are permitted to resolve the class, create
                        // the installer instance, and validate it.
                        ModuleInstall install = SharedClassObject.findObject(clazz.asSubclass(ModuleInstall.class), true);
                        // The following can throw IllegalStateException, which we would
                        // rethrow as InvalidException below:
                        install.validate();
                    } catch (NoSuchMethodException nsme) {
                        // OK, did not find it here, continue.
                    }
                }
                if (c == Object.class) throw new ClassCastException("Should extend ModuleInstall: " + clazz.getName()); // NOI18N
                // Did not find any validate() method, so remember the class and resolve later.
            } catch (Exception t) {
                InvalidException ie = new InvalidException(m, t.toString());
                ie.initCause(t);
                throw ie;
            } catch (LinkageError t) {
                InvalidException ie = new InvalidException(m, t.toString());
                ie.initCause(t);
                throw ie;
            }
        }
        // For layer & help set, validate only that the base-locale resource
        // exists, not its contents or anything.
        String layerResource = cache.findProperty(m, "OpenIDE-Module-Layer", false); // NOI18N
        if (layerResource != null && !m.isNetigso()) {
            URL layer = m.getClassLoader().getResource(layerResource);
            if (layer == null) throw new InvalidException(m, "Layer not found: " + layerResource); // NOI18N
        }
        String helpSetName = cache.findProperty(m, "OpenIDE-Module-Description", false); // NOI18N
        if (helpSetName != null) {
            Util.err.log(Level.WARNING, "Use of OpenIDE-Module-Description in {0} is deprecated.", m.getCodeNameBase());
            Util.err.warning("(Please install help using an XML layer instead.)");
        }
        // We are OK, commit everything to our cache.
        if (mysections != null) {
            sections.put(m, mysections);
        }
        if (clazz != null) {
            installs.put(m, clazz.asSubclass(ModuleInstall.class));
        }
        if (layerResource != null) {
            layers.put(m, layerResource);
        }   
    }

    private void checkForHiddenPackages(Module m) throws InvalidException {
        List<Module.PackageExport> hiddenPackages = new ArrayList<Module.PackageExport>();
        List<Module> mWithDeps = new LinkedList<Module>();
        mWithDeps.add(m);
        if (mgr != null) {
            addEnabledFragments(m, mWithDeps);
            for (Dependency d : m.getDependencies()) {
                if (d.getType() == Dependency.TYPE_MODULE) {
                    Module _m = mgr.get((String) Util.parseCodeName(d.getName())[0]);
                    assert _m != null : d;
                    mWithDeps.add(_m);
                    addEnabledFragments(_m, mWithDeps);
                }
            }
        }
        for (Module _m : mWithDeps) {
            String hidden = cache.findProperty(_m, "OpenIDE-Module-Hide-Classpath-Packages", false); // NOI18N
            if (hidden != null) {
                for (String piece : hidden.trim().split("[ ,]+")) { // NOI18N
                    try {
                        if (piece.endsWith(".*")) { // NOI18N
                            String pkg = piece.substring(0, piece.length() - 2);
                            Dependency.create(Dependency.TYPE_MODULE, pkg);
                            if (pkg.lastIndexOf('/') != -1) {
                                throw new IllegalArgumentException("Illegal OpenIDE-Module-Hide-Classpath-Packages: " + hidden); // NOI18N
                            }
                            hiddenPackages.add(new Module.PackageExport(pkg.replace('.', '/') + '/', false));
                        } else if (piece.endsWith(".**")) { // NOI18N
                            String pkg = piece.substring(0, piece.length() - 3);
                            Dependency.create(Dependency.TYPE_MODULE, pkg);
                            if (pkg.lastIndexOf('/') != -1) {
                                throw new IllegalArgumentException("Illegal OpenIDE-Module-Hide-Classpath-Packages: " + hidden); // NOI18N
                            }
                            hiddenPackages.add(new Module.PackageExport(pkg.replace('.', '/') + '/', true));
                        } else {
                            throw new IllegalArgumentException("Illegal OpenIDE-Module-Hide-Classpath-Packages: " + hidden); // NOI18N
                        }
                    } catch (IllegalArgumentException x) {
                        throw new InvalidException(_m, x.getMessage());
                    }
                }
            }
        }
        if (!hiddenPackages.isEmpty()) {
            synchronized (hiddenClasspathPackages) {
                hiddenClasspathPackages.put(m, hiddenPackages);
                for (Module.PackageExport pkg : hiddenPackages) {
                    List<Module> ms = hiddenClasspathPackagesReverse.get(pkg);
                    if (ms == null) {
                        hiddenClasspathPackagesReverse.put(pkg, ms = new LinkedList<Module>());
                    }
                    ms.add(m);
                }
            }
        }
    }
    
    private void addEnabledFragments(Module forModule, List<Module> moduleWithDependencies) {
        for (Module fragment : mgr.getAttachedFragments(forModule)) {
            if (mgr.isOrWillEnable(fragment)) {
                moduleWithDependencies.add(fragment);
            }
        }
    }

    public void dispose(Module m) {
        Util.err.fine("dispose: " + m);
        // Events probably not needed here.
        Set<ManifestSection> s = sections.remove(m);
        if (s != null) {
            for (ManifestSection sect : s) {
                sect.dispose();
            }
        }
        installs.remove(m);
        layers.remove(m);
        kosherPackages.remove(m);
        synchronized (hiddenClasspathPackages) {
            hiddenClasspathPackages.remove(m);
            for (List<Module> ms : hiddenClasspathPackagesReverse.values()) {
                ms.remove(m);
                // could also delete entry if ms.isEmpty()
            }
        }
    }

    @Override
    protected void classLoaderUp(ClassLoader cl) {
        MainLookup.systemClassLoaderChanged(cl);
        ev.log(Events.PERF_TICK, "META-INF/services/ additions registered"); // NOI18N
    }

    final void waitOnStart() {
        onStartStop.waitOnStart();
    }
    
    @Override
    public void load(final List<Module> modules) {
        FileUtil.runAtomicAction(new Runnable() {
            @Override
            public void run() {
                loadImpl(modules);
            }
        });
    }
    
    private void loadImpl(List<Module> modules) {
        ev.log(Events.START_LOAD, modules);
        
        checkForDeprecations(modules);
        
        loadLayers(modules, true);
        ev.log(Events.PERF_TICK, "layers loaded"); // NOI18N
	
        onStartStop.initialize();
        ev.log(Events.PERF_TICK, "@OnStart"); // NOI18N

        ev.log(Events.PERF_START, "NbInstaller.load - sections"); // NOI18N
        ev.log(Events.LOAD_SECTION);
        CoreBridge.getDefault().loaderPoolTransaction(true);
        try {
            for (Module m: modules) {
                try {
                    loadSections(m, true);
                } catch (Exception t) {
                    Util.err.log(Level.SEVERE, null, t);
                } catch (LinkageError le) {
                    Util.err.log(Level.SEVERE, null, le);
                }
                ev.log(Events.PERF_TICK, "sections for " + m.getCodeName() + " loaded"); // NOI18N
            }
        } finally {
            CoreBridge.getDefault().loaderPoolTransaction(false);
        }
        ev.log(Events.PERF_END, "NbInstaller.load - sections"); // NOI18N

        // Yarda says to put this here.
        if (! initializedFolderLookup) {
            Util.err.fine("modulesClassPathInitialized");
            MainLookup.modulesClassPathInitialized();
            initializedFolderLookup = true;
        }
        
        // we need to initialize UI before we let modules run ModuleInstall.restore
        Main.initUICustomizations();

        ev.log(Events.PERF_START, "NbInstaller.load - ModuleInstalls"); // NOI18N
        for (Module m: modules) {
            try {
                loadCode(m, true);
            } catch (Exception t) {
                Util.err.log(Level.SEVERE, null, t);
            } catch (LinkageError le) {
                Util.err.log(Level.SEVERE, null, le);
            } catch (AssertionError e) {
                Util.err.log(Level.SEVERE, null, e);
            }
	    ev.log(Events.PERF_TICK, "ModuleInstall for " + m.getCodeName() + " called"); // NOI18N
        }
        ev.log(Events.PERF_END, "NbInstaller.load - ModuleInstalls"); // NOI18N

        ev.log(Events.FINISH_LOAD, modules);
        
        if (Boolean.getBoolean("netbeans.preresolve.classes")) {
            preresolveClasses(modules);
        }
    }
    
    final void preloadCache(Collection<Module> modules) {
        for (Module m : modules) {
            // initialize the cache
            isShowInAutoUpdateClient(m);
        }
    }
    
    @Override
    public void unload(final List<Module> modules) {
        FileUtil.runAtomicAction(new Runnable() {
            @Override
            public void run() {
                unloadImpl(modules);
            }
        });
    }
    
    private void unloadImpl(List<Module> modules) {
        ev.log(Events.START_UNLOAD, modules);
        for (Module m: modules) {
            try {
                loadCode(m, false);
            } catch (Exception t) {
                Util.err.log(Level.SEVERE, null, t);
            } catch (LinkageError le) {
                Util.err.log(Level.SEVERE, null, le);
            }
        }
        CoreBridge.getDefault().loaderPoolTransaction(true);
        try {
            for (Module m: modules) {
                try {
                    loadSections(m, false);
                } catch (Exception t) {
                    Util.err.log(Level.SEVERE, null, t);
                } catch (LinkageError le) {
                    Util.err.log(Level.SEVERE, null, le);
                }
            }
        } finally {
            try {
                CoreBridge.getDefault().loaderPoolTransaction(false);
            } catch (RuntimeException e) {
                Util.err.log(Level.SEVERE, null, e);
            }
        }
        loadLayers(modules, false);
        ev.log(Events.FINISH_UNLOAD, modules);
    }
    
    /** Load/unload installer code for a module. */
    @SuppressWarnings("deprecation") // old ModuleInstall methods we have to call
    private void loadCode(Module m, boolean load) throws Exception {
        Class<? extends ModuleInstall> instClazz = installs.get(m);
        if (instClazz != null) {
            ModuleInstall inst = SharedClassObject.findObject(instClazz, true);
            if (load) {
                ev.log(Events.RESTORE, m);
                inst.restored();
            } else {
                ev.log(Events.UNINSTALL, m);
                inst.uninstalled();
            }
        }
    }
    
    /** Load/unload all manifest sections for a given module. */
    @SuppressWarnings("deprecation") // old ManifestSection.* we have to interpret
    private void loadSections(Module m, boolean load) throws Exception {
        Set<ManifestSection> s = sections.get(m);
        if (s == null) {
            return;
        }
        // Whether we yet had occasion to attach to the module actions list.
        boolean attachedToMA = false;
        try {
	    ev.log(Events.LOAD_SECTION);
            for (ManifestSection sect : s) {
                if (sect instanceof ManifestSection.ActionSection) {
                    if (! attachedToMA) {
                        // First categorize the actions we will add.
                        Object category = m.getLocalizedAttribute("OpenIDE-Module-Display-Category"); // NOI18N
                        if (category == null) {
                            // uncategorized modules group by themselves
                            category = m.getCodeNameBase();
                        }
                        CoreBridge.getDefault().attachToCategory(category);
                        attachedToMA = true;
                    }
                    CoreBridge.getDefault ().loadActionSection((ManifestSection.ActionSection)sect, load);
                } else if (sect instanceof ManifestSection.ClipboardConvertorSection) {
                    loadGenericSection(sect, load);
                } else if (sect instanceof ManifestSection.DebuggerSection) {
                    loadGenericSection(sect, load);
                } else if (sect instanceof ManifestSection.LoaderSection) {
                    CoreBridge.getDefault().loadLoaderSection((ManifestSection.LoaderSection)sect, load);
                } else {
                    assert false : sect;
                }
            }
        } finally {
            if (attachedToMA) {
                CoreBridge.getDefault ().attachToCategory (null);
            }
        }
    }
    
    // Load or unload various possible manifest sections.
    
    /** Simple section that can just be passed to lookup.
     * The lookup sees the real object, not the section.
     * You tell it whether to convert the result to the real
     * instance, or just register the section itself.
     */
    private void loadGenericSection(ManifestSection s, boolean load) {
        CoreBridge.getDefault().loadDefaultSection(s, convertor, load);
    }
    
    private final InstanceContent.Convertor<ManifestSection,Object> convertor = new Convertor();
    private final class Convertor implements InstanceContent.Convertor<ManifestSection,Object> { // or <ManifestSection,SharedClassObject>?
        Convertor() {}
        public Object convert(ManifestSection s) {
            try {
                return s.getInstance();
            } catch (Exception e) {
                Util.err.log(Level.WARNING, null, e);
                // Try to remove it from the pool so it does not continue
                // to throw errors over and over. Hopefully it is kosher to
                // do this while it is in the process of converting! I.e.
                // hopefully InstanceLookup is well-synchronized.
                loadGenericSection(s, false);
                return null;
            }
        }
        public Class<?> type(ManifestSection s) {
            return s.getSuperclass();
        }
        
        /** Computes the ID of the resulted object.
         * @param obj the registered object
         * @return the ID for the object
         */
        public String id(ManifestSection obj) {
            return obj.toString ();
        }
        
        /** The human presentable name for the object.
         * @param obj the registered object
         * @return the name representing the object for the user
         */
        public String displayName(ManifestSection obj) {
            return obj.toString ();
        }
        
    }
    
    /** Either load or unload the layer, if any, for a set of modules.
     * If the parameter load is true, load it, else unload it.
     * Locale/branding variants are likewise loaded or unloaded.
     * If a module has no declared layer, does nothing.
     */
    void loadLayers(List<Module> modules, boolean load) {
        ev.log(load ? Events.LOAD_LAYERS : Events.UNLOAD_LAYERS, modules);
        // #23609: dependent modules should be able to override:
        modules = new ArrayList<Module>(modules);
        Collections.reverse(modules);
        Map<ModuleLayeredFileSystem,Collection<URL>> urls = new HashMap<ModuleLayeredFileSystem,Collection<URL>>(5);
        ModuleLayeredFileSystem userModuleLayer = ModuleLayeredFileSystem.getUserModuleLayer();
        ModuleLayeredFileSystem installationModuleLayer = ModuleLayeredFileSystem.getInstallationModuleLayer();
        urls.put(userModuleLayer, new LinkedHashSet<URL>(1000));
        urls.put(installationModuleLayer, new LinkedHashSet<URL>(1000));
        for (Module m: modules) {
            // #19458: only put reloadables into the "session layer"
            // (where they will not have their layers cached). All others
            // should go into "installation layer" (so that they can mask
            // layers according to cross-dependencies).
            ModuleLayeredFileSystem host = m.isReloadable() ? userModuleLayer : installationModuleLayer;
            Collection<URL> theseurls = urls.get(host);
            if (theseurls == null) {
                theseurls = new LinkedHashSet<URL>(1000);
                urls.put(host, theseurls);
            }
            String s = layers.get(m);
            if (s != null) {
                Util.err.log(Level.FINE, "loadLayer: {0} load={1}", new Object[] { s, load });
                // Actually add a sequence of layers, in locale order.
                String base, ext;
                int idx = s.lastIndexOf('.'); // NOI18N
                if (idx == -1) {
                    base = s;
                    ext = ""; // NOI18N
                } else {
                    base = s.substring(0, idx);
                    ext = s.substring(idx);
                }
                boolean foundSomething = false;
                for (String suffix : NbCollections.iterable(NbBundle.getLocalizingSuffixes())) {
                    String resource = base + suffix + ext;
                    Enumeration<URL> en = m.findResources(resource);
                    if (en.hasMoreElements()) {
                        URL u = en.nextElement();
                        theseurls.add(u);
                        foundSomething = true;
                        if (en.hasMoreElements()) {
                            String patchesClassPath = System.getProperty("netbeans.patches." + m.getCodeNameBase()); // NOI18N
                            assert patchesClassPath != null : "At most one resource per module: " + m; // NOI18N
                            Util.err.log(Level.INFO, "Using {0} as layer for {1} not {2}", new Object[]{u, m.getCodeNameBase(), en.nextElement()}); // NOI18N
                        }
                    }
                }
                if (! foundSomething) {
                    // Should never happen (we already checked in prepare() for base layer)...
                    Util.err.fine("Module layer not found: " + s);
                    continue;
                }
            }
            Enumeration<URL> e = m.findResources("META-INF/generated-layer.xml"); // NOI18N
            while (e.hasMoreElements()) {
                URL u = e.nextElement();
                theseurls.add(u);
            }
        }
        // Now actually do it.
        for (Map.Entry<ModuleLayeredFileSystem,Collection<URL>> entry: urls.entrySet()) {
            ModuleLayeredFileSystem host = entry.getKey();
            Collection<URL> theseurls = entry.getValue();
            Util.err.log(Level.FINE, "Adding/removing layer URLs: host={0} urls={1}", new Object[] { host, theseurls });
            try {
                if (load) {
                    host.addURLs(theseurls);
                } else {
                    // #106737: we might have the wrong host, since it switches when reloadable flag is toggled.
                    // To be safe, remove from both.
                    userModuleLayer.removeURLs(theseurls);
                    installationModuleLayer.removeURLs(theseurls);
                }
            } catch (Exception e) {
                Util.err.log(Level.WARNING, null, e);
            }
        }
    }
    
    /** Scan a (nondeprecated) module for direct dependencies on deprecated modules.
     * Deprecated modules can quietly depend on other deprecated modules.
     * And if the module is not actually enabled, it does not matter.
     * Indirect dependencies are someone else's problem.
     * Provide-require dependencies do not count either.
     * @param modules the modules which are now being turned on
     */
    private void checkForDeprecations(List<Module> modules) {
        Map<String,Set<String>> depToUsers = new TreeMap<String,Set<String>>();
        for (Module m : modules) {
            String depr = cache.findProperty(m, "OpenIDE-Module-Deprecated", false); // NOI18N
            if (!Boolean.parseBoolean(depr)) { 
                for (Dependency dep : m.getDependencies()) {
                    if (dep.getType() == Dependency.TYPE_MODULE) {
                        String cnb = (String) Util.parseCodeName(dep.getName())[0];
                        Set<String> users = depToUsers.get(cnb);
                        if (users == null) {
                            users = new TreeSet<String>();
                            depToUsers.put(cnb, users);
                        }
                        users.add(m.getCodeNameBase());
                    }
                }
            }
        }
        for (Map.Entry<String,Set<String>> entry : depToUsers.entrySet()) {
            String dep = entry.getKey();
            Module o = mgr.get(dep);
            assert o != null : "No such module: " + dep;
            String depr = cache.findProperty(o, "OpenIDE-Module-Deprecated", false); // NOI18N
            if (Boolean.parseBoolean(depr)) {
                String message = cache.findProperty(o, "OpenIDE-Module-Deprecation-Message", true); // NOI18N
                // XXX use NbEvents? I18N?
                // For now, assume this is a developer-oriented message that need not be localized or displayed in a pretty fashion.
                Set<String> users = entry.getValue();
                if (message != null) {
                    Util.err.log(Level.WARNING, "the modules {0} use {1} which is deprecated: {2}", new Object[] {users, dep, message});
                } else {
                    Util.err.log(Level.WARNING, "the modules {0} use {1} which is deprecated.", new Object[] {users, dep});
                }
            }
        }
    }
        
    public boolean closing(List<Module> modules) {
        Util.err.fine("closing: " + modules);
        for (Module m: modules) {
            Class<? extends ModuleInstall> instClazz = installs.get(m);
            if (instClazz != null) {
                try {
                    ModuleInstall inst = SharedClassObject.findObject(instClazz, true);
                    if (! inst.closing()) {
                        Util.err.fine("Module " + m + " refused to close");
                        return false;
                    }
                } catch (RuntimeException re) {
                    Util.err.log(Level.SEVERE, null, re);
                    // continue, assume it is trash
                } catch (LinkageError le) {
                    Util.err.log(Level.SEVERE, null, le);
                }
            }
        }
        return onStartStop.closing(modules);
    }
    
    @Override
    public void close(List<Module> modules) {
        closeAsync(modules).waitFinished();
    }

    @Override
    public Task closeAsync(List<Module> modules) {
        Util.err.fine("close: " + modules);
        ev.log(Events.CLOSE);
        moduleList.shutDown();
        List<Task> waitFor = onStartStop.startClose(modules);
        // [PENDING] this may need to write out changed ModuleInstall externalized
        // forms...is that really necessary to do here, or isn't it enough to
        // do right after loading etc.? Currently these are only written when
        // a ModuleInstall has just been restored etc. which is probably fine.
        for (Module m : modules) {
            Class<? extends ModuleInstall> instClazz = installs.get(m);
            if (instClazz != null) {
                try {
                    ModuleInstall inst = SharedClassObject.findObject(instClazz, true);
                    if (inst == null) throw new IllegalStateException("Inconsistent state: " + instClazz); // NOI18N
                    inst.close();
                } catch (ThreadDeath td) {
                    throw td;
                } catch (Throwable t) {
                    // Catch even the heavy stuff here, we are going away.
                    Util.err.log(Level.SEVERE, null, t);
                    // oh well
                }
            }
        }
        waitFor.add(WarmUpSupport.waitTask());
        return new ProxyTask(waitFor);
    }
    private static String cacheCnb;
    private static Set<Dependency> cacheDeps;
    @Override
    protected Set<Dependency> loadDependencies(String cnb) {
        return cnb.equals(cacheCnb) ? cacheDeps : null;
    }
    @SuppressWarnings("unchecked")
    static void register(String name, Object obj) {
        cacheCnb = name;
        cacheDeps = (Set<Dependency>)obj;
    }

    
    /** Overridden to perform automatic API upgrades.
     * That is, should do nothing on new modules, but for older ones will
     * automatically make them depend on things they need.
     * This is now all handled from declarative configuration files:
     * in the system filesystem, ModuleAutoDeps/*.xml may be added
     * according to the DTD "-//NetBeans//DTD Module Automatic Dependencies 1.0//EN".
     */
    public @Override void refineDependencies(Module m, Set<Dependency> dependencies) {
        /* JST-PENDING just tring to comment this out
        // All modules implicitly depend on the APIs somehow.
        if (!m.getCodeNameBase().equals("org.openide") &&
                Util.getModuleDep(dependencies, "org.openide") == null) {
            dependencies.addAll(Dependency.create(Dependency.TYPE_MODULE, "org.openide/1 > 0")); // NOI18N
        }
         */
        if (Boolean.getBoolean("org.netbeans.core.modules.NbInstaller.noAutoDeps")) {
            // Skip them all - useful for unit tests.
            return;
        }
        AutomaticDependencies.Report rep = AutomaticDependencies.getDefault().refineDependenciesAndReport(m.getCodeNameBase(), dependencies);
        if (rep.isModified()) {
            Util.err.warning(rep.toString());
        }
    }

    public @Override String[] refineProvides (Module m) {
        if (m.getCodeNameBase ().equals ("org.openide.modules")) { // NOI18N
            List<String> arr = new ArrayList<String>(4);
            CoreBridge.defineOsTokens(arr);
            
            // module format is now 2
            arr.add("org.openide.modules.ModuleFormat1"); // NOI18N
            arr.add("org.openide.modules.ModuleFormat2"); // NOI18N
            
            return arr.toArray (new String[0]);
        }
        return null;
    }
    
    public @Override boolean shouldDelegateResource(Module m, Module parent, String pkg) {
        //Util.err.fine("sDR: m=" + m + " parent=" + parent + " pkg=" + pkg);
        // Cf. #19622:
        if (parent == null) {
            // Application classpath checks.
            for (String cppkg : CLASSPATH_PACKAGES) {
                if (pkg.startsWith(cppkg) && !findKosher(m).contains(cppkg)) {
                    // Undeclared use of a classpath package. Refuse it.
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine("Refusing to load classpath package " + pkg + " for " + m.getCodeNameBase() + " without a proper dependency"); // NOI18N
                    }
                    return false;
                }
            }
            List<Module.PackageExport> hiddenPackages;
            synchronized (hiddenClasspathPackages) {
                hiddenPackages = hiddenClasspathPackages.get(m);
            }
            if (hiddenPackages != null) {
                for (Module.PackageExport hidden : hiddenPackages) {
                    if (hidden.recursive ? pkg.startsWith(hidden.pkg) : pkg.equals(hidden.pkg)) {
                        if (LOG.isLoggable(Level.FINE)) {
                            LOG.fine("Refusing to load classpath package " + pkg + " for " + m.getCodeNameBase());
                        }
                        return false;
                    }
                }
            }
            if (!checkBootDelegation(pkg)) {
                return false;
            }
        }
        if (LOG.isLoggable(Level.FINER) && /* otherwise ClassCircularityError on LogRecord*/!pkg.equals("java/util/logging/")) {
            LOG.finer("Delegating resource " + pkg + " from " + parent + " for " + m.getCodeNameBase());
        }
        return true;
    }

    public @Override boolean shouldDelegateClasspathResource(String pkg) {
        synchronized (hiddenClasspathPackages) {
            for (Map.Entry<Module.PackageExport,List<Module>> entry : hiddenClasspathPackagesReverse.entrySet()) {
                Module.PackageExport hidden = entry.getKey();
                if (hidden.recursive ? pkg.startsWith(hidden.pkg) : pkg.equals(hidden.pkg)) {
                    for (Module m : entry.getValue()) {
                        if (m.isEnabled()) {
                            if (LOG.isLoggable(Level.FINE)) {
                                LOG.fine("Refusing to load classpath package " + pkg + " because of " + m.getCodeNameBase());
                            }
                            return false;
                        }
                    }
                }
            }
        }
        return checkBootDelegation(pkg);
    }
    
    private boolean checkBootDelegation(String pkg) {
        String del = System.getProperty("netbeans.bootdelegation"); // NOI18N
        if (del != null) {
            if (!pkg.startsWith("java/")) {
                boolean allowed = false;
                for (String s : del.split(",")) {
                    s = s.trim();
                    if (s.endsWith(".*")) {
                        s = s.substring(0, s.length() - 2).replace('.', '/') + '/';
                        if (pkg.startsWith(s) && pkg.length() > s.length()) {
                            allowed = true;
                            break;
                        }
                    } else {
                        s = s.replace('.', '/') + '/';
                        if (pkg.equals(s)) {
                            allowed = true;
                            break;
                        }
                    }
                }
                if (!allowed) {
                    return false;
                }
            }
        }
        return true;
    }
    
    private static final String[] CLASSPATH_PACKAGES = {
        // core.jar shall be inaccessible
        "org/netbeans/core/startup/",
        // Do not add JRE packages here! See issue #96711 for the alternative.
    };
    
    private Set<String> findKosher(Module m) {
        Set<String> s = kosherPackages.get(m);
        if (s == null) {
            s = new HashSet<String>();
            Set<Dependency> deps = m.getDependencies();
            SpecificationVersion openide = Util.getModuleDep(deps, "org.openide"); // NOI18N
            boolean pre27853 = (openide == null || openide.compareTo(new SpecificationVersion("1.3.12")) < 0); // NOI18N
            for (Dependency dep : deps) {
                // Extend this for other classpath modules:
                if (dep.getType() == Dependency.TYPE_MODULE &&
                        dep.getName().equals("org.netbeans.core.startup/1")) { // NOI18N
                    // Various modules (incl. o.n.core) dep on this as friends and need to access it.
                    s.add("org/netbeans/core/startup/"); // NOI18N
                } else if (pre27853 && dep.getType() == Dependency.TYPE_MODULE) {
                    // Module dependency. If a package was kosher for A and B depends
                    // on A, we let B use it undeclared. Cf. javacvs -> vcscore & RE.
                    // But #27853: only do this for old modules.
                    String name = dep.getName();
                    int idx = name.indexOf('/');
                    if (idx != -1) {
                        name = name.substring(0, idx);
                    }
                    Module other = mgr.get(name);
                    if (other == null) throw new IllegalStateException("Should have found dep " + dep + " from " + m); // NOI18N
                    s.addAll(findKosher(other));
                } else if (dep.getType() == Dependency.TYPE_PACKAGE) {
                    String depname = dep.getName();
                    String req;
                    int idx = depname.indexOf('['); // NOI18N
                    if (idx == -1) {
                        // depname = org.apache.xerces.parsers
                        // req = org/apache/xerces/parsers/
                        req = depname.replace('.', '/').concat("/"); // NOI18N
                    } else if (idx == 0) {
                        // depname = [org.apache.xerces.parsers.DOMParser]
                        // req = org/apache/xerces/parsers/
                        int idx2 = depname.lastIndexOf('.');
                        req = depname.substring(1, idx2).replace('.', '/').concat("/"); // NOI18N
                    } else {
                        // depname = org.apache.xerces.parsers[DOMParser]
                        // req = org/apache/xerces/parsers/
                        req = depname.substring(0, idx).replace('.', '/').concat("/"); // NOI18N
                    }
                    for (String cppkg : CLASSPATH_PACKAGES) {
                        if (req.startsWith(cppkg)) {
                            // Module requested this exact package or some subpackage or
                            // a class in one of these packages; it is kosher.
                            s.add(cppkg);
                        }
                    }
                }
            }
            if (s.isEmpty()) s = Collections.<String>emptySet();
            kosherPackages.put(m, s);
        }
        return s;
    }
    
    /** Information about contents of some JARs on the startup classpath (both lib/ and lib/ext/).
     * The first item in each is a prefix for JAR filenames.
     * For a JAR matching such a prefix, the remaining items are entries
     * from CLASSPATH_PACKAGES.
     * <p>In this case, the JAR is only on a module's effective classpath in case
     * {@link #findKosher} reports that at least one of the packages is requested -
     * in which case that package and any descendants, but not other packages in the JAR,
     * are included in the effective classpath.
     * <p>JARs which are not listed here but are in lib/ or lib/ext/ are assumed to be
     * in the effective classpath of every module, automatically.
     * <p>Note that updater.jar is *not* on the startup classpath and is explicitly
     * added to autoupdate.jar via Class-Path, so it need not concern us here -
     * it should appear on the effective classpath of autoupdate only.
     * @see "#22466"
     */
    private static final String[][] CLASSPATH_JARS = {
        {"core", "org/netbeans/core/", "org/netbeans/beaninfo/"}, // NOI18N
        // No one ought to be using boot.jar:
        {"boot"}, // NOI18N
    };
    
    /** Get the effective "classpath" used by a module.
     * Specific syntax: classpath entries as usual, but
     * they may be qualified with packages, e.g.:
     * <code>/path/to/api-module.jar[org.netbeans.api.foo.*,org.netbeans.spi.foo.**]:/path/to/wide-open.jar</code>
     * @see ModuleSystem#getEffectiveClasspath
     * @see "#22466"
     */
    String getEffectiveClasspath(Module m) {
        if (!m.isEnabled()) {
            // For disabled modules, we do not know for sure what it can load.
            return ""; // NOI18N
        }
        // The classpath entries - each is a filename possibly followed by package qualifications.
        List<String> l = new ArrayList<String>(100);
        // Start with boot classpath.
        createBootClassPath(l);
        // Move on to "startup classpath", qualified by applicable package deps etc.
        // Fixed classpath modules don't get restricted in this way.
        Set<String> kosher = m.isFixed() ? null : findKosher(m);
        StringTokenizer tok = new StringTokenizer(System.getProperty("java.class.path", ""), File.pathSeparator);
        while (tok.hasMoreTokens()) {
            addStartupClasspathEntry(new File(tok.nextToken()), l, kosher);
        }
        // See org.netbeans.Main for actual computation of the dynamic classpath.
        tok = new StringTokenizer(System.getProperty("netbeans.dynamic.classpath", ""), File.pathSeparator);
        while (tok.hasMoreTokens()) {
            addStartupClasspathEntry(new File(tok.nextToken()), l, kosher);
        }
        // Finally include this module and its dependencies recursively.
        // Modules whose direct classpath has already been added to the list:
        Set<Module> modulesConsidered = new HashSet<Module>(50);
        // Code names of modules on which this module has an impl dependency
        // (so can use any package):
        Set<String> implDeps = new HashSet<String>(10);
        for (Dependency dep : m.getDependencies()) {
            // Remember, provide-require deps do not affect classpath!
            if (dep.getType() == Dependency.TYPE_MODULE && dep.getComparison() == Dependency.COMPARE_IMPL) {
                // We can assume the impl dep has the correct version;
                // otherwise this module could not have been enabled to begin with.
                implDeps.add(dep.getName());
            }
        }
        SpecificationVersion openide = Util.getModuleDep(m.getDependencies(), "org.openide"); // NOI18N
        boolean pre27853 = (openide == null || openide.compareTo(new SpecificationVersion("1.3.12")) < 0); // NOI18N
        // #27853: only make recursive for old modules.
        addModuleClasspathEntries(m, m, modulesConsidered, implDeps, l, pre27853 ? Integer.MAX_VALUE : 1);
        // Done, package it all up as a string.
        StringBuilder buf = new StringBuilder(l.size() * 100 + 1);
        for (String s: l) {
            if (buf.length() > 0) {
                buf.append(File.pathSeparatorChar);
            }
            buf.append(s);
        }
        return buf.toString();
    }
    
    // Copied from NbClassPath:
    private static void createBootClassPath(List<String> l) {
        // boot
        String boot = System.getProperty("sun.boot.class.path"); // NOI18N
        if (boot != null) {
            StringTokenizer tok = new StringTokenizer(boot, File.pathSeparator);
            while (tok.hasMoreTokens()) {
                l.add(tok.nextToken());
            }
        }
        
        // std extensions
        String extensions = System.getProperty("java.ext.dirs"); // NOI18N
        if (extensions != null) {
            for (StringTokenizer st = new StringTokenizer(extensions, File.pathSeparator); st.hasMoreTokens();) {
                File dir = new File(st.nextToken());
                File[] entries = dir.listFiles();
                if (entries != null) {
                    for (File f : entries) {
                        String name = f.getName().toLowerCase(Locale.US);
                        if (name.endsWith(".zip") || name.endsWith(".jar")) { // NOI18N
                            l.add(f.getAbsolutePath());
                        }
                    }
                }
            }
        }
    }
    
    /** Add a classpath entry from the lib/ or lib/ext/ dirs, if appropriate.
     * @param entry a classpath entry; either a directory, or a JAR file which might be controlled
     * @param cp the classpath (<code>List&lt;String&gt;</code>) to add to
     * @param kosher known packages which may be accessed (<code>Set&lt;String&gt;</code>), or null for no restrictions
     * @see "#22466"
     */
    private static void addStartupClasspathEntry(File cpEntry, List<String> cp, Set<String> kosher) {
        if (cpEntry.isDirectory()) {
            cp.add(cpEntry.getAbsolutePath());
            return;
        }
        // JAR or ZIP. Check whether we can access it.
        String name = cpEntry.getName();
        for (String[] cpjar : CLASSPATH_JARS) {
            if (kosher != null && name.startsWith(cpjar[0])) {
                // Restricted JAR.
                StringBuilder entry = null; // will be set if there are any packages
                for (int k = 1; k < cpjar.length; k++) {
                    String pkg = cpjar[k];
                    if (kosher.contains(pkg)) {
                        // Module is permitted to use this package.
                        if (entry == null) {
                            entry = new StringBuilder(100);
                            entry.append(cpEntry.getAbsolutePath());
                            entry.append('['); // NOI18N
                        } else {
                            entry.append(','); // NOI18N
                        }
                        // pkg format: org/foo/bar/
                        entry.append(pkg.replace('/', '.')); // NOI18N
                        entry.append("**"); // NOI18N
                    }
                }
                if (entry != null) {
                    // Had >= 1 packages available from this JAR.
                    entry.append(']'); // NOI18N
                    cp.add(entry.toString());
                }
                return;
            }
        }
        // Did not match any, assumed to be on classpath.
        cp.add(cpEntry.getAbsolutePath());
    }

    /** Recursively build a classpath based on the module dependency tree.
     * @param m the current module whose JAR(s) might be added to the classpath
     * @param orig the module whose classpath we are ultimately trying to compute
     * @param considered modules we have already handled
     * @param implDeps module code names on which the orig module has an impl dep
     * @param cp the classpath to add to
     * @param depth the recursion depth to go to: 0 = only m's JAR, 1 = m + its parents, ..., MAX_INT = full recursion
     * @see "#22466"
     */
    private void addModuleClasspathEntries(Module m, Module orig, Set<Module> considered, Set<String> implDeps, List<String> cp, int depth) {
        // Head recursion so that baser modules are added to the front of the classpath:
        if (!considered.add(m)) return;
        for (Dependency dep : m.getDependencies()) {
            if (dep.getType() == Dependency.TYPE_MODULE) {
                String cnb = (String) Util.parseCodeName(dep.getName())[0];
                Module next = mgr.get(cnb);
                if (next == null) throw new IllegalStateException("No such module: " + cnb); // NOI18N
                if (depth > 0) {
                    addModuleClasspathEntries(next, orig, considered, implDeps, cp, depth - 1);
                }
            }
        }
        // Now add entries from m, if applicable.
        // We are friendly if we are considering the original module itself,
        // or if the original module has a (direct) impl dep on this module.
        boolean friend = (m == orig) || implDeps.contains(m.getCodeName());
        // Friend modules export everything to the classpath.
        // Otherwise, there may or may not be package restrictions.
        Module.PackageExport[] exports = friend ? null : m.getPublicPackages();
        String qualification = ""; // NOI18N
        if (exports != null) {
            if (exports.length == 0) {
                // Everything is blocked.
                return;
            }
            // Only certain packages are exported, so list them explicitly.
            StringBuilder b = new StringBuilder(100);
            b.append('['); // NOI18N
            for (int i = 0; i < exports.length; i++) {
                if (i > 0) {
                    b.append(','); // NOI18N
                }
                b.append(exports[i].pkg.replace('/', '.')); // NOI18N
                b.append(exports[i].recursive ? "**" : "*"); // NOI18N
            }
            b.append(']'); // NOI18N
            qualification = b.toString();
        }
        for (File jar : m.getAllJars()) {
            cp.add(jar.getAbsolutePath() + qualification);
        }
    }
    
    /** Check all module classes to make sure there are no unresolvable compile-time
     * dependencies. Turn on this mode with
     * <code>-J-Dnetbeans.preresolve.classes=true</code>
     * May be more useful to run org.netbeans.core.ValidateClassLinkageTest.
     * @param modules a list of modules, newly enabled, to check; fixed modules will be ignored
     */
    private void preresolveClasses(List<Module> modules) {
        Util.err.info("Pre-resolving classes for all loaded modules...be sure you have not specified -J-Xverify:none in ide.cfg");
        for (Module m : modules) {
            if (m.isFixed()) continue;
            if (m.getJarFile() == null) continue;
            File jar = m.getJarFile();
            // Note: extension JARs not checked.
            try {
                JarFile j = new JarFile(jar, true);
                try {
                    for (JarEntry entry : NbCollections.iterable(j.entries())) {
                        String name = entry.getName();
                        if (name.endsWith(".class")) { // NOI18N
                            String clazz = name.substring(0, name.length() - 6).replace('/', '.'); // NOI18N
                            Throwable t = null;
                            try {
                                Class.forName(clazz, false, m.getClassLoader());
                            } catch (ClassNotFoundException cnfe) { // e.g. "Will not load classes from default package" from ProxyClassLoader
                                t = cnfe;
                            } catch (LinkageError le) {
                                t = le;
                            } catch (RuntimeException re) { // e.g. IllegalArgumentException from package defs
                                t = re;
                            }
                            if (t != null) {
                                // XXX #106153: consider excluding mobility/ant-ext classes
                                Util.err.log(Level.WARNING, "From " + clazz + " in " + m.getCodeNameBase() + " with effective classpath " + getEffectiveClasspath(m), t);
                            }
                        }
                    }
                } finally {
                    j.close();
                }
            } catch (IOException ioe) {
                Util.err.log(Level.WARNING, null, ioe);
            }
        }
    }
    
    final boolean isShowInAutoUpdateClient(ModuleInfo m) {
        String show = cache.findProperty(m, "AutoUpdate-Show-In-Client", false); // NOI18N
        if (show != null) {
            return Boolean.parseBoolean(show);
        }
        // OSGi bundles should be considered invisible by default since they are typically autoloads.
        // (NB modules get AutoUpdate-Show-In-Client inserted into the JAR by the build process.)
        if (m instanceof Module) {
            return !((Module)m).isNetigso();
        }
        return true;
    }
    
    /** Cache important attributes from module manifests */
    static class Cache implements Stamps.Updater {
        private static final String CACHE = "all-installer.dat"; // NOI18N
        private final boolean modulePropertiesCached;
        private final Properties moduleProperties;

        public Cache() {
            InputStream is = Stamps.getModulesJARs().asStream(CACHE);
            IF:
            if (is != null) {
                Properties p = new Properties();
                try {
                    p.load(is);
                    is.close();
                } catch (IOException ex) {
                    LOG.log(Level.INFO, "Can't load all-installer.dat", ex);
                    break IF;
                }
                moduleProperties = p;
                modulePropertiesCached = true;
                return;
            }
            moduleProperties = new Properties();
            modulePropertiesCached = false;
        }

        final String findProperty(ModuleInfo m, String name, boolean localized) {
            final String fullName = m.getCodeNameBase() + '.' + name;
            final String nullValue = "\u0000"; // NOI18N
            if (modulePropertiesCached) {
                String val = moduleProperties.getProperty(fullName);
                if (nullValue.equals(val)) { 
                    return null;
                }
                if (val != null) {
                    return val;
                }
                LOG.log(Level.FINE, "not cached value: {0} for {1}", new Object[]{name, m});
            } 
            Object p = localized ? m.getLocalizedAttribute(name) : m.getAttribute(name);
            if (p == null) {
                moduleProperties.setProperty(fullName, nullValue);
                Stamps.getModulesJARs().scheduleSave(this, CACHE, false);
                return null;
            }
            String prop = p instanceof String ? (String)p : null;
            if (prop != null) {
                moduleProperties.setProperty(fullName, prop);
                Stamps.getModulesJARs().scheduleSave(this, CACHE, false);
            }
            return prop;
        }

        final String findGlobalProperty(String name, String expValue, String replaceValue) {
            assert name != null;
            assert replaceValue != null;
            if (modulePropertiesCached) {
                return moduleProperties.getProperty(name);
            } else {
                final Object prevValue = moduleProperties.get(name);
                if (BaseUtilities.compareObjects(expValue, prevValue)) {
                    moduleProperties.put(name, replaceValue);
                }
                Stamps.getModulesJARs().scheduleSave(this, CACHE, false);
                return null;
            }
        }

        @Override
        public void flushCaches(DataOutputStream os) throws IOException {
            moduleProperties.store(os, null);
        }

        @Override
        public void cacheReady() {
        }
    } // end of Cache
}
