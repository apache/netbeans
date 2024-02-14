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

package org.apache.tools.ant.module.bridge;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.apache.tools.ant.module.AntSettings;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.modules.ModuleInfo;
import org.openide.util.ChangeSupport;
import org.openide.util.Enumerations;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;
import org.openide.util.io.NullOutputStream;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Utility class providing entry points to the bridging functionality.
 * @author Jesse Glick
 */
public final class AntBridge {
    
    private static final Logger LOG = Logger.getLogger(AntBridge.class.getName());
    
    private AntBridge() {}

    private static final class AntInstance {
        public final String mainClassPath;
        public final ClassLoader mainClassLoader;
        public final ClassLoader bridgeClassLoader;
        public final BridgeInterface bridge;
        public final Map<String,Map<String,Class>> customDefs;
        public final Map<String,ClassLoader> customDefClassLoaders;
        public AntInstance(String mainClassPath, ClassLoader mainClassLoader,
                ClassLoader bridgeClassLoader, BridgeInterface bridge,
                Map<String,Map<String,Class>> customDefs,
                Map<String,ClassLoader> customDefClassLoaders) {
            this.mainClassPath = mainClassPath;
            this.mainClassLoader = mainClassLoader;
            this.bridgeClassLoader = bridgeClassLoader;
            this.bridge = bridge;
            this.customDefs = customDefs;
            this.customDefClassLoaders = customDefClassLoaders;
            if (mainClassLoader instanceof MainClassLoader) {
                ((MainClassLoader) mainClassLoader).antInstance = this;
            }
        }
    }
    private static Reference<AntInstance> antInstance = null;
    
    private static final ChangeSupport cs = new ChangeSupport(AntBridge.class);

    public static boolean NO_MODULE_SYSTEM; // for use from tests only
    private static final class MiscListener implements PropertyChangeListener, LookupListener {
        MiscListener() {}
        private ModuleInfo[] modules = null;
        public void propertyChange(PropertyChangeEvent ev) {
            String prop = ev.getPropertyName();
            if (AntSettings.PROP_ANT_HOME.equals(prop) ||
                    AntSettings.PROP_EXTRA_CLASSPATH.equals(prop) ||
                    AntSettings.PROP_AUTOMATIC_EXTRA_CLASSPATH.equals(prop)) {
                LOG.log(Level.FINE, "AntBridge got settings change in {0}", prop);
                fireChange();
            } else if (ModuleInfo.PROP_ENABLED.equals(prop)) {
                LOG.log(Level.FINE, "AntBridge got module enablement change on {0}", ev.getSource());
                fireChange();
            }
        }
        public void resultChanged(LookupEvent ev) {
            LOG.fine("AntModule got ModuleInfo change");
            synchronized (this) {
                if (modules != null) {
                    for (ModuleInfo module : modules) {
                        module.removePropertyChangeListener(this);
                    }
                    modules = null;
                }
            }
            fireChange();
        }
        public synchronized ModuleInfo[] getEnabledModules() {
            if (NO_MODULE_SYSTEM) {
                return new ModuleInfo[0];
            }
            if (modules == null) {
                Collection<? extends ModuleInfo> c = modulesResult.allInstances();
                modules = c.toArray(new ModuleInfo[0]);
                for (ModuleInfo module : modules) {
                    module.addPropertyChangeListener(this);
                }
            }
            List<ModuleInfo> enabledModules = new ArrayList<ModuleInfo>(modules.length);
            for (ModuleInfo module : modules) {
                if (module.isEnabled()) {
                    enabledModules.add(module);
                }
            }
            return enabledModules.toArray(new ModuleInfo[0]);
        }
    }
    private static MiscListener miscListener = new MiscListener();
    private static Lookup.Result<ModuleInfo> modulesResult = Lookup.getDefault().lookupResult(ModuleInfo.class);
    static {
        AntSettings.addPropertyChangeListener(miscListener);
        modulesResult.addLookupListener(miscListener);
    }
    
    /**
     * Listen for changes in the contents of the bridge, as e.g. after changing the
     * location of the installed copy of Ant.
     */
    public static synchronized void addChangeListener(ChangeListener l) {
        cs.addChangeListener(l);
    }
    
    /**
     * Stop listening for changes in the contents of the bridge.
     */
    public static synchronized void removeChangeListener(ChangeListener l) {
        cs.removeChangeListener(l);
    }
    
    private static void fireChange() {
        antInstance = null;
        cs.fireChange();
    }
    
    /**
     * Get the loader responsible for loading Ant together with any
     * user-defined classpath.
     */
    public static ClassLoader getMainClassLoader() {
        return getAntInstance().mainClassLoader;
    }
    
    /**
     * Get any custom task/type definitions stored in $nbhome/ant/nblib/*.jar.
     * Some of the classes might not be fully resolvable, so beware.
     * The names will include namespace prefixes.
     * <p>
     * Only minimal antlib syntax is currently interpreted here:
     * only <code>&lt;taskdef&gt;</code> and <code>&lt;typedef&gt;</code>,
     * and only the <code>name</code> and <code>classname</code> attributes.
     */
    public static Map<String,Map<String,Class>> getCustomDefsWithNamespace() {
        return getAntInstance().customDefs;
    }
    
    /**
     * Same as {@link #getCustomDefsWithNamespace} but without any namespace prefixes.
     */
    public static Map<String,Map<String,Class>> getCustomDefsNoNamespace() {
        Map<String,Map<String,Class>> m = new HashMap<String,Map<String,Class>>();
        for (Map.Entry<String,Map<String,Class>> entry : getCustomDefsWithNamespace().entrySet()) {
            String type = entry.getKey();
            Map<String,Class> defs = entry.getValue();
            Map<String,Class> m2 = new HashMap<String,Class>();
            for (Map.Entry<String,Class> entry2 : defs.entrySet()) {
                String fqn = entry2.getKey();
                Class<?> clazz = entry2.getValue();
                String name;
                int idx = fqn.lastIndexOf(':');
                if (idx != -1) {
                    name = fqn.substring(idx + 1);
                } else {
                    name = fqn;
                }
                m2.put(name, clazz);
            }
            m.put(type, m2);
        }
        return m;
    }
    
    /**
     * Get a map from enabled module code name bases to class loaders containing
     * JARs from ant/nblib/*.jar.
     */
    // XXX does not have to be here, used only by BridgeImpl
    public static Map<String,ClassLoader> getCustomDefClassLoaders() throws IOException {
        return getAntInstance().customDefClassLoaders;
    }
    
    /**
     * Get the bridge interface.
     */
    public static BridgeInterface getInterface() {
        return getAntInstance().bridge;
    }
    
    private static synchronized AntInstance getAntInstance() {
        AntInstance ai;
        if (antInstance != null) {
            ai = antInstance.get();
        } else {
            ai = null;
        }
        if (ai == null) {
            ai = createAntInstance();
            // XXX would be more accurate to stuff this struct into by BridgeImpl
            // so that it all lives or dies iff that class loader is still alive
            // (current impl is just workaround for JDK #6389107)
            antInstance = new SoftReference<AntInstance>(ai);
        }
        return ai;
    }
    
    private static AntInstance createAntInstance() {
        LOG.fine("AntBridge.createAntInstance - loading Ant installation...");
        try {
            List<File> mainClassPath = createMainClassPath();
            LOG.log(Level.FINER, "mainClassPath={0}", mainClassPath);
            ClassLoader main = createMainClassLoader(mainClassPath);
            ClassLoader bridgeLoader = createBridgeClassLoader(main);
            // Ensures that the loader is functional, and that it is at least 1.5.x
            // so that our classes can link against it successfully, and that
            // we are really loading Ant from the right place:
            Class ihClazz = Class.forName("org.apache.tools.ant.input.InputHandler", false, bridgeLoader); // NOI18N
            Class<? extends BridgeInterface> impl = bridgeLoader.loadClass("org.apache.tools.ant.module.bridge.impl.BridgeImpl").asSubclass(BridgeInterface.class); // NOI18N
            if (AntSettings.getAntHome() != null) {
                ClassLoader loaderUsedForAnt = ihClazz.getClassLoader();
                if (loaderUsedForAnt != main) {
                    throw new IllegalStateException("Wrong class loader is finding Ant: " + loaderUsedForAnt); // NOI18N
                }
                Class ihClazz2 = Class.forName("org.apache.tools.ant.input.InputHandler", false, main); // NOI18N
                if (ihClazz2 != ihClazz) {
                    throw new IllegalStateException("Main and bridge class loaders do not agree on version of Ant: " + ihClazz2.getClassLoader()); // NOI18N
                }
                try {
                    Class alClazz = Class.forName("org.apache.tools.ant.taskdefs.Antlib", false, bridgeLoader); // NOI18N
                    if (alClazz.getClassLoader() != main) {
                        throw new IllegalStateException("Bridge loader is loading stuff from elsewhere: " + alClazz.getClassLoader()); // NOI18N
                    }
                    Class alClazz2 = Class.forName("org.apache.tools.ant.taskdefs.Antlib", false, main); // NOI18N
                    if (alClazz2 != alClazz) {
                        throw new IllegalStateException("Main and bridge class loaders do not agree on version of Ant: " + alClazz2.getClassLoader()); // NOI18N
                    }
                } catch (ClassNotFoundException cnfe) {
                    // Fine, it was added in Ant 1.6.
                }
                if (impl.getClassLoader() != bridgeLoader) {
                    throw new IllegalStateException("Wrong class loader is finding bridge impl: " + impl.getClassLoader()); // NOI18N
                }
            } // in classpath mode, these checks do not apply
            Map<String,ClassLoader> cDCLs = createCustomDefClassLoaders(main);
            return new AntInstance(classPathToString(mainClassPath), main, bridgeLoader, impl.getDeclaredConstructor().newInstance(), createCustomDefs(cDCLs), cDCLs);
        } catch (Exception e) {
            return fallback(e);
        } catch (LinkageError e) {
            return fallback(e);
        }
    }
    
    private static AntInstance fallback(Throwable e) {
        ClassLoader dummy = ClassLoader.getSystemClassLoader();
        Map<String,Map<String,Class>> defs = new HashMap<String,Map<String,Class>>();
        defs.put("task", new HashMap<String,Class>()); // NOI18N
        defs.put("type", new HashMap<String,Class>()); // NOI18N
        return new AntInstance("", dummy, dummy, new DummyBridgeImpl(e), defs, Collections.<String,ClassLoader>emptyMap());
    }
    
    private static final class JarFilter implements FilenameFilter {
        JarFilter() {}
        public boolean accept(File dir, String name) {
            return name.toLowerCase(Locale.US).endsWith(".jar"); // NOI18N
        }
    }
    
    private static String classPathToString(List<File> cp) {
        StringBuffer b = new StringBuffer();
        Iterator<File> it = cp.iterator();
        while (it.hasNext()) {
            b.append(it.next().getAbsolutePath());
            if (it.hasNext()) {
                b.append(File.pathSeparator);
            }
        }
        return b.toString();
    }
    /**
     * Get the equivalent of java.class.path for the main Ant loader.
     * Includes everything in the main class loader.
     */
    public static String getMainClassPath() {
        return getAntInstance().mainClassPath;
    }
    
    private static List<File> createMainClassPath() throws Exception {
        // Use LinkedHashSet to automatically suppress duplicates.
        final Collection<File> cp = new LinkedHashSet<>();
        File antHome = AntSettings.getAntHome();
        if (antHome != null) {
            File libdir = new File(antHome, "lib"); // NOI18N
            if (!libdir.isDirectory()) {
                throw new IOException("No such Ant library dir: " + libdir); // NOI18N
            }
            LOG.log(Level.FINE, "Creating main class loader from {0}", libdir);
            // First look for ${ant.home}/patches/*.jar, to support e.g. patching #47708:
            addJARs(cp, new File(libdir.getParentFile(), "patches")); // NOI18N
            // Now continue with regular classpath.
            addJARs(cp, libdir);
        }
        addJARs(cp, new File(new File(System.getProperty("user.home"), ".ant"), "lib"));
        cp.addAll(AntSettings.getExtraClasspath());
        cp.addAll(AntSettings.getAutomaticExtraClasspath());
        return new ArrayList<>(cp);
    }

    private static void addJARs(Collection<File> cp, File dir) {
        File[] libs = dir.listFiles(new JarFilter());
        if (libs != null) {
            Collections.addAll(cp, libs);
        }
    }

    private static URL[] toURLs(List<? extends File> classPath) throws MalformedURLException {
        final URL[] urls = new URL[classPath.size()];
        int i = 0;
        for (File entry : classPath) {
            urls[i++] = /* #162158: do not use FileUtil.urlForArchiveOrDir(entry) */Utilities.toURI(entry).toURL();
        }
        return urls;
    }

    private static boolean hasJavac(final ClassLoader cl) {
        return cl.getResource("com/sun/tools/javac/Main.class") != null;    //NOI18N
    }

    private static List<File> prependTools(List<File> origCp) {
        final Collection<File> tools = new LinkedHashSet<>();
        addJARs(tools, new File(new File(System.getProperty("java.home")).getParentFile(), "lib")); //NOI18N
        tools.removeAll(origCp);
        final List<File> res = new ArrayList<>(tools.size() + origCp.size());
        res.addAll(tools);
        res.addAll(origCp);
        return res;
    }

    private static ClassLoader createMainClassLoader(List<File> mainClassPath) throws Exception {
        if (AntSettings.getAntHome() != null) {
            final ClassLoader sysClassLoader = ClassLoader.getSystemClassLoader();
            ClassLoader parent = sysClassLoader/* #152620 */.getParent();
            if (LOG.isLoggable(Level.FINE)) {
                List<URL> parentURLs;
                if (parent instanceof URLClassLoader) {
                    parentURLs = Arrays.asList(((URLClassLoader) parent).getURLs());
                } else {
                    parentURLs = null;
                }
                LOG.log(Level.FINER, "AntBridge.createMainClassLoader: cp={0} parent.urls={1}", new Object[] {mainClassPath, parentURLs});
            }
            if (!hasJavac(parent)) {
                //No javac in ext  ClassLoader add it either from sys ClassLoader
                //or try to add tools.jar on cp
                if (hasJavac(sysClassLoader)) {
                    parent = new AddJavacClassLoader(parent, sysClassLoader);
                } else {
                    mainClassPath = prependTools(mainClassPath);
                }
            }
            return new MainClassLoader(toURLs(mainClassPath), parent);
        } else {
            // Run-in-classpath mode.
            ClassLoader existing = AntBridge.class.getClassLoader();
            if (!hasJavac(existing)) {
                //javac is not transitivelly on ext ClassLoader
                //try to add it
                mainClassPath = prependTools(mainClassPath);
            }
            final URL[] cp = toURLs(mainClassPath);
            if (existing instanceof URLClassLoader) {
                try {
                    // Need to insert resources into it.
                    // We could also try making a fresh loader which masks the parent
                    // yet delegates findResource to it, so as to be initiating loader
                    // for everything Ant. Might be safer.
                    Method addURL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
                    addURL.setAccessible(true);
                    for (URL u : cp) {
                        addURL.invoke(existing, u);
                    }
                    return existing;
                } catch (Exception e) {
                    // Problem. Don't do it, I guess.
                    LOG.log(Level.WARNING, null, e);
                }
            }
            // Probably won't work as desired, but just in case:
            return new AllPermissionURLClassLoader(cp, existing);
        }
    }
    
    private static ClassLoader createBridgeClassLoader(ClassLoader main) throws Exception {
        File bridgeJar = InstalledFileLocator.getDefault().locate("ant/nblib/bridge.jar", "org.apache.tools.ant.module", false); // NOI18N
        if (bridgeJar == null) {
            // Run-in-classpath mode.
            return main;
        }
        return createAuxClassLoader(bridgeJar, main, AntBridge.class.getClassLoader());
    }
    
    private static ClassLoader createAuxClassLoader(File lib, ClassLoader main, ClassLoader moduleLoader) throws IOException {
        return new AuxClassLoader(moduleLoader, main, Utilities.toURI(lib).toURL());
    }
    
    /**
     * Get a map from enabled module code name bases to class loaders containing
     * JARs from ant/nblib/*.jar.
     */
    private static Map<String,ClassLoader> createCustomDefClassLoaders(ClassLoader main) throws IOException {
        Map<String,ClassLoader> m = new HashMap<String,ClassLoader>();
        ModuleInfo[] modules = miscListener.getEnabledModules();
        InstalledFileLocator ifl = InstalledFileLocator.getDefault();
        for (ModuleInfo module : modules) {
            String cnb = module.getCodeNameBase();
            String cnbDashes = cnb.replace('.', '-');
            File lib = ifl.locate("ant/nblib/" + cnbDashes + ".jar", cnb, false); // NOI18N
            if (lib == null) {
                if (main.getResource(cnb.replace('.', '/') + "/antlib.xml") != null) { // NOI18N
                    // Run-in-classpath mode.
                    m.put(cnb, main);
                }
                continue;
            }
            ClassLoader l = createAuxClassLoader(lib, main, module.getClassLoader());
            m.put(cnb, l);
        }
        return m;
    }
    
    private static Map<String,Map<String,Class>> createCustomDefs(Map<String,ClassLoader> cDCLs) throws IOException {
        Map<String,Map<String,Class>> m = new HashMap<String,Map<String,Class>>();
        Map<String,Class> tasks = new HashMap<String,Class>();
        Map<String,Class> types = new HashMap<String,Class>();
        // XXX #36776: should eventually support <macrodef>s here
        m.put("task", tasks); // NOI18N
        m.put("type", types); // NOI18N
        for (Map.Entry<String,ClassLoader> entry : cDCLs.entrySet()) {
            String cnb = entry.getKey();
            ClassLoader l = entry.getValue();
            String resource = cnb.replace('.', '/') + "/antlib.xml"; // NOI18N
            URL antlib = l.getResource(resource);
            if (antlib == null) {
                throw new IOException("Could not find " + resource + " in ant/nblib/" + cnb.replace('.', '-') + ".jar"); // NOI18N
            }
            Document doc;
            try {
                doc = XMLUtil.parse(new InputSource(antlib.toExternalForm()), false, true, /*XXX needed?*/null, null);
            } catch (SAXException e) {
                throw (IOException)new IOException(e.toString()).initCause(e);
            }
            Element docEl = doc.getDocumentElement();
            if (!docEl.getLocalName().equals("antlib")) { // NOI18N
                throw new IOException("Bad root element for " + antlib + ": " + docEl); // NOI18N
            }
            NodeList nl = docEl.getChildNodes();
            Map<String,String> newTaskDefs = new HashMap<String,String>();
            Map<String,String> newTypeDefs = new HashMap<String,String>();
            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                if (n.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                Element def = (Element)n;
                boolean type;
                if (def.getNodeName().equals("taskdef")) { // NOI18N
                    type = false;
                } else if (def.getNodeName().equals("typedef")) { // NOI18N
                    type = true;
                } else {
                    LOG.warning("Warning: unrecognized definition " + def + " in " + antlib);
                    continue;
                }
                String name = def.getAttribute("name"); // NOI18N
                if (name == null) {
                    // Not a hard error since there might be e.g. <taskdef resource="..."/> here
                    // which we do not parse but which is permitted in antlib by Ant.
                    LOG.warning("Warning: skipping definition " + def + " with no 'name' in " + antlib);
                    continue;
                }
                String classname = def.getAttribute("classname"); // NOI18N
                if (classname == null) {
                    // But this is a hard error.
                    throw new IOException("No 'classname' attr on def of " + name + " in " + antlib); // NOI18N
                }
                // XXX would be good to handle at least onerror attr too
                String nsname = "antlib:" + cnb + ":" + name; // NOI18N
                (type ? newTypeDefs : newTaskDefs).put(nsname, classname);
            }
            loadDefs(newTaskDefs, tasks, l);
            loadDefs(newTypeDefs, types, l);
        }
        return m;
    }
    
    private static void loadDefs(Map<String,String> p, Map<String,Class> defs, ClassLoader l) throws IOException {
        // Similar to IntrospectedInfo.load, after having parsed the properties.
        for (Map.Entry<String,String> entry : p.entrySet()) {
            String name = entry.getKey();
            String clazzname = entry.getValue();
            try {
                Class<?> clazz = l.loadClass(clazzname);
                defs.put(name, clazz);
            } catch (ClassNotFoundException cnfe) {
                // This is not normal. If the class is mentioned, it should be there.
                throw (IOException) new IOException("Could not load class " + clazzname + ": " + cnfe).initCause(cnfe); // NOI18N
            } catch (NoClassDefFoundError ncdfe) {
                // Normal for e.g. tasks dumped there by disabled modules.
                // Cf. #36702 for possible better solution.
                LOG.log(Level.FINE, "AntBridge.loadDefs: skipping {0}: {1}", new Object[] {clazzname, ncdfe});
            } catch (LinkageError e) {
                // Not normal; if it is there it ought to be resolvable etc.
                throw (IOException) new IOException("Could not load class " + clazzname + ": " + e).initCause(e); // NOI18N
            }
        }
    }
    
    static class AllPermissionURLClassLoader extends URLClassLoader {
        
        private static PermissionCollection allPermission;
        private static synchronized PermissionCollection getAllPermissions() {
            if (allPermission == null) {
                allPermission = new Permissions();
                allPermission.add(new AllPermission());
            }
            return allPermission;
        }
        
        public AllPermissionURLClassLoader(URL[] urls, ClassLoader parent) {
            super(sanitize(urls), parent);
        }

        private static URL[] sanitize(URL[] urls) { // #192190
            for (int i = 0; i < urls.length; i++) {
                try {
                    urls[i] = URI.create(urls[i].toURI().toASCIIString()).toURL();
                } catch (/*URISyntax,MalformedURL*/Exception x) {
                    assert false : "converting " + urls[i] + ": " + x;
                }
            }
            return urls;
        }
        
        @Override
        protected final PermissionCollection getPermissions(CodeSource cs) {
            return getAllPermissions();
        }
        
        @Override
        public String toString() {
            return super.toString() + "[parent=" + getParent() + ",urls=" + Arrays.asList(getURLs()) + "]";
        }

        @Override
        public URL getResource(String name) {
            URL u = super.getResource(name);
            LOG.log(Level.FINER, "APURLCL.gR: {0} -> {1} [{2}]", new Object[] {name, u, this});
            return u;
        }
        
        @Override
        public Enumeration<URL> findResources(String name) throws IOException {
            try {
                Enumeration<URL> us = super.findResources(name);
                if (LOG.isLoggable(Level.FINER)) {
                    // Make a copy so it can be logged:
                    List<URL> resources = Collections.list(us);
                    us = Collections.enumeration(resources);
                    LOG.finer("APURLCL.fRs: " + name + " -> " + resources + " [" + this + "]");
                }
                return us;
            } catch (IOException e) {
                LOG.log(Level.FINE, null, e);
                throw e;
            }
        }

    }
    
    /**
     * Special class loader that refuses to load Ant or NetBeans classes from its parent.
     * Necessary in order to be able to load the intended Ant distro from a unit test.
     */
    private static final class MainClassLoader extends AllPermissionURLClassLoader {

        AntInstance antInstance; // forces AI + MCL to either be collected together, or not at all
        
        public MainClassLoader(URL[] urls, ClassLoader parent) {
            super(urls, parent);
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            if (name.startsWith("com.sun.jdi.")) { // NOI18N
                // Must be loaded from regular AppClassLoader; otherwise get CCE during debugging
                // since org-netbeans-api-debugger-jpda.jar would be loading a different copy.
                throw new ClassNotFoundException("Will not load JDI separately from tools.jar: " + name);
            }
            return super.findClass(name);
        }
        
        @Override // #139048: work around JAXP #6723276
        public InputStream getResourceAsStream(String name) {
            if (name.equals("META-INF/services/javax.xml.stream.XMLInputFactory")) { // NOI18N
                // StAX is different; defined and implemented in BEA-specific code with different
                // search logic and an unusable fallback implementation.
                return super.getResourceAsStream(name);
            } else if (name.startsWith("META-INF/services/javax.xml.")) { // NOI18N
                // For JAXP services defined and implemented in the JRE,
                // this is the only workaround for JDK 5 & 6 to load the JRE's copy.
                return new ByteArrayInputStream(new byte[0]);
            } else {
                return super.getResourceAsStream(name);
            }
        }
        
    }


    private static final class AddJavacClassLoader extends ClassLoader {
        private static final Iterable<? extends String> javacPackages = Arrays.asList(
                "com.sun.jarsigner.",               //NOI18N
                "com.sun.javadoc.",                 //NOI18N
                "com.sun.mirror.",                  //NOI18N
                "com.sun.source.",                  //NOI18N
                "com.sun.tools.",                   //NOI18N
                "javax.annotation.processing.",     //NOI18N
                "javax.lang.model.",                //NOI18N
                "javax.tools.",                     //NOI18N
                "sun.rmi.",                         //NOI18N
                "sun.security.",                    //NOI18N
                "sun.tools.");                      //NOI18N





        private static final int INITIAL_BUFSIZ = 1<<14;

        private final ClassLoader contextClassLoader;
        //@GuardedBy("getClassLoadingLock()")
        private byte[] buffer;

        public AddJavacClassLoader(
                @NonNull final ClassLoader parentClassLoader,
                @NonNull final ClassLoader contextClassLoader) {
            super(parentClassLoader);
            this.contextClassLoader = contextClassLoader;
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            if (isFromContextClassLoader(name, true)) {
                try {
                    synchronized (getClassLoadingLock(name)) {
                        Class<?> c = findLoadedClass(name);
                        if (c == null) {
                            InputStream in = contextClassLoader.getResourceAsStream(
                                    name.replace('.', '/').concat(".class"));    //NOI18N
                            if (in != null) {
                                try {
                                    in = new BufferedInputStream(in, INITIAL_BUFSIZ);
                                    final int len = readFully(in);
                                    final int lastDot = name.lastIndexOf('.');   //NOI18N
                                    if (lastDot >= 0) {
                                        final String pack = name.substring(0, lastDot);
                                        if (getPackage(pack) == null) {
                                            definePackage(pack, null, null, null, null, null, null, null);
                                        }
                                    }
                                    c = defineClass(name, buffer, 0, len);
                                } finally {
                                    in.close();
                                }
                            } else {
                                throw new ClassNotFoundException(String.format(
                                        "The class: %s is not found in %s", //NOI18N
                                        name,
                                        contextClassLoader));
                            }
                        }
                        if (resolve) {
                            resolveClass(c);
                        }
                        return c;
                    }
                } catch (final IOException ioe) {
                    throw new ClassNotFoundException(String.format(
                            "IO Error while loading: %s", name),
                            ioe);
                }
            }
            return super.loadClass(name, resolve);
        }

        @Override
        public URL getResource(String name) {
            if (isFromContextClassLoader(name, false)) {
                return contextClassLoader.getResource(name);
            }
            if (META_INF_PLATFORM_PROVIDER_REGISTRATION_NAME.equals(name)) {
                return META_INF_PLATFORM_PROVIDER_REGISTRATION;
            }
            return super.getResource(name);
        }

        @Override
        public Enumeration<URL> getResources(String name) throws IOException {
            Enumeration<URL> res =  super.getResources(name);
            if (isFromContextClassLoader(name, false)) {
                final Enumeration<URL> cclRes = contextClassLoader.getResources(name);
                res = Enumerations.concat(res, cclRes);
            }
            if (META_INF_PLATFORM_PROVIDER_REGISTRATION_NAME.equals(name)) {
                res = Enumerations.concat(res, Enumerations.singleton(META_INF_PLATFORM_PROVIDER_REGISTRATION));
            }
            return res;
        }

        //need to add JDKPlatformProvider registration into META-INF/services, otherwise it would not be found by javac, and --release would not work:
        private static final String META_INF_PLATFORM_PROVIDER_REGISTRATION_NAME = "META-INF/services/com.sun.tools.javac.platform.PlatformProvider";
        private static final FileSystem META_INF_PLATFORM_PROVIDER_FS;
        private static final URL META_INF_PLATFORM_PROVIDER_REGISTRATION;

        static {
            try {
                META_INF_PLATFORM_PROVIDER_FS = FileUtil.createMemoryFileSystem();
                FileObject file = FileUtil.createData(META_INF_PLATFORM_PROVIDER_FS.getRoot(), META_INF_PLATFORM_PROVIDER_REGISTRATION_NAME);
                try (OutputStream out = file.getOutputStream()) {
                    out.write("com.sun.tools.javac.platform.JDKPlatformProvider\n".getBytes(StandardCharsets.UTF_8));
                }
                META_INF_PLATFORM_PROVIDER_REGISTRATION = file.toURL();
            } catch (Throwable t) {
                throw new IllegalStateException(t);
            }
        }

        private static boolean isFromContextClassLoader(
                @NonNull String name,
                final boolean pkg) {
            //the 5-th letter of all interesting packages is either 's','x','r','t'
            //using that to prevent (possibly expensive) loop through javacPackages:
            char f = name.length() > 4 ? name.charAt(4) : '\0';
            if (f == 'x' || f == 's' || f == 'r' || f =='t') { //NOI18N
                if (!pkg) {
                    name = name.replace('/', '.');  //NOI18N
                }
                for (String pack : javacPackages) {
                    if (name.startsWith(pack)) {
                        return true;
                    }
                }
            }
            return false;
        }

        private int readFully(@NonNull final InputStream in) throws IOException {
            if (buffer == null) {
                buffer = new byte[INITIAL_BUFSIZ];
            }
            int capacity = buffer.length;
            int nread = 0, n;
            while (true) {
                while ((n = in.read(buffer, nread, capacity - nread)) > 0) {
                    nread += n;
                }
                if (n < 0 || (n = in.read()) < 0) {
                    break;
                }
                capacity = capacity << 1;
                buffer = Arrays.copyOf(buffer, capacity);
                buffer[nread++] = (byte)n;
            }
            return nread;
        }

    }
    // I/O redirection impl. Keyed by thread group (each Ant process has its own TG).
    // Various Ant tasks (e.g. <java fork="false" output="..." ...>) need the system
    // I/O streams to be redirected to the demux streams of the project so they can
    // be handled properly. Ideally nothing would try to read directly from stdin
    // or print directly to stdout/stderr but in fact some tasks do.
    // Could also pass a custom InputOutput to ExecutionEngine, perhaps, but this
    // seems a lot simpler and probably has the same effect.

    private static int delegating = 0;
    private static InputStream origIn;
    private static PrintStream origOut, origErr;
    private static Map<ThreadGroup,InputStream> delegateIns = new HashMap<ThreadGroup,InputStream>();
    private static Map<ThreadGroup,PrintStream> delegateOuts = new HashMap<ThreadGroup,PrintStream>();
    private static Map<ThreadGroup,PrintStream> delegateErrs = new HashMap<ThreadGroup,PrintStream>();
    /** list, not set, so can be reentrant - treated as a multiset */
    private static List<Thread> suspendedDelegationTasks = new ArrayList<Thread>();
    
    /**
     * Handle I/O scoping for overlapping project runs.
     * You must call {@link #restoreSystemInOutErr} in a finally block.
     * @param in new temporary input stream for this thread group
     * @param out new temporary output stream for this thread group
     * @param err new temporary error stream for this thread group
     * @see "#36396"
     */
    public static synchronized void pushSystemInOutErr(InputStream in, PrintStream out, PrintStream err) {
        if (delegating++ == 0) {
            origIn = System.in;
            origOut = System.out;
            origErr = System.err;
            System.setIn(new MultiplexInputStream());
            System.setOut(new MultiplexPrintStream(false));
            System.setErr(new MultiplexPrintStream(true));
        }
        ThreadGroup tg = Thread.currentThread().getThreadGroup();
        delegateIns.put(tg, in);
        delegateOuts.put(tg, out);
        delegateErrs.put(tg, err);
    }
    
    /**
     * Restore original I/O streams after a call to {@link #pushSystemInOutErr}.
     */
    public static synchronized void restoreSystemInOutErr() {
        assert delegating > 0;
        if (--delegating == 0) {
            System.setIn(origIn);
            System.setOut(origOut);
            System.setErr(origErr);
            origIn = null;
            origOut = null;
            origErr = null;
        }
        ThreadGroup tg = Thread.currentThread().getThreadGroup();
        delegateIns.remove(tg);
        delegateOuts.remove(tg);
        delegateErrs.remove(tg);
    }

    /**
     * Temporarily suspend delegation of system I/O streams for the current thread.
     * Useful when running callbacks to IDE code that might try to print to stderr etc.
     * Must be matched in a finally block by {@link #resumeDelegation}.
     * Safe to call when not actually delegating; in that case does nothing.
     * Safe to call in reentrant but not overlapping fashion.
     */
    public static synchronized void suspendDelegation() {
        Thread t = Thread.currentThread();
        //assert delegateOuts.containsKey(t.getThreadGroup()) : "Not currently delegating in " + t;
        // #58394: do *not* check that it does not yet contain t. It is OK if it does; need to
        // be able to call suspendDelegation reentrantly.
        suspendedDelegationTasks.add(t);
    }
    
    /**
     * Resume delegation of system I/O streams for the current thread group
     * after a call to {@link #suspendDelegation}.
     */
    public static synchronized void resumeDelegation() {
        Thread t = Thread.currentThread();
        //assert delegateOuts.containsKey(t.getThreadGroup()) : "Not currently delegating in " + t;
        // This is still valid: suspendedDelegationTasks must have *at least one* copy of t.
        assert suspendedDelegationTasks.contains(t) : "Have not suspended delegation in " + t;
        suspendedDelegationTasks.remove(t);
    }

    public static synchronized InputStream delegateInputStream() {
        Thread t = Thread.currentThread();
        ThreadGroup tg = t.getThreadGroup();
        while (tg != null && !delegateIns.containsKey(tg)) {
            tg = tg.getParent();
        }
        InputStream is = delegateIns.get(tg);
        return is != null ? is : origIn;
    }

    public static synchronized PrintStream delegateOutputStream(boolean err) {
        Thread t = Thread.currentThread();
        ThreadGroup tg = t.getThreadGroup();
        while (tg != null && !delegateIns.containsKey(tg)) {
            tg = tg.getParent();
        }
        PrintStream ps = (err ? delegateErrs : delegateOuts).get(tg);
        return ps != null ? ps : (err ? origErr : origOut);
    }

    private static final class MultiplexInputStream extends InputStream {
        
        public MultiplexInputStream() {}
        
        private InputStream delegate() {
            Thread t = Thread.currentThread();
            ThreadGroup tg = t.getThreadGroup();
            while (tg != null && !delegateIns.containsKey(tg)) {
                tg = tg.getParent();
            }
            InputStream is = delegateIns.get(tg);
            if (is != null && !suspendedDelegationTasks.contains(t)) {
                return is;
            } else if (delegating > 0) {
                assert origIn != null;
                return origIn;
            } else {
                // Probably should not happen? But not sure.
                return System.in;
            }
        }
        
        @Override
        public int read() throws IOException {
            return delegate().read();
        }        
        
        @Override
        public int read(byte[] b) throws IOException {
            return delegate().read(b);
        }
        
        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return delegate().read(b, off, len);
        }
        
        @Override
        public int available() throws IOException {
            return delegate().available();
        }
        
        @Override
        public boolean markSupported() {
            return delegate().markSupported();
        }        
        
        @Override
        public void mark(int readlimit) {
            delegate().mark(readlimit);
        }
        
        @Override
        public void close() throws IOException {
            delegate().close();
        }
        
        @Override
        public long skip(long n) throws IOException {
            return delegate().skip(n);
        }
        
        @Override
        public void reset() throws IOException {
            delegate().reset();
        }
        
    }
    
    private static final class MultiplexPrintStream extends PrintStream {
        
        private final boolean err;
        
        public MultiplexPrintStream(boolean err) {
            this(new NullOutputStream(), err);
        }
        
        private MultiplexPrintStream(NullOutputStream nos, boolean err) {
            super(nos);
            nos.throwException = true;
            this.err = err;
        }
        
        private PrintStream delegate() {
            Thread t = Thread.currentThread();
            ThreadGroup tg = t.getThreadGroup();
            Map<ThreadGroup,PrintStream> delegates = err ? delegateErrs : delegateOuts;
            while (tg != null && !delegates.containsKey(tg)) {
                tg = tg.getParent();
            }
            PrintStream ps = delegates.get(tg);
            if (ps != null && !suspendedDelegationTasks.contains(t)) {
                assert !(ps instanceof MultiplexPrintStream);
                return ps;
            } else if (delegating > 0) {
                PrintStream orig = err ? origErr : origOut;
                assert orig != null;
                assert !(orig instanceof MultiplexPrintStream);
                return orig;
            } else {
                // Probably should not happen? But not sure. See: #89020, #144468
                // Safest to just discard output in this case.
                return new PrintStream(new ByteArrayOutputStream());
            }
        }
        
        @Override
        public boolean checkError() {
            return delegate().checkError();
        }
        
        @Override
        public void close() {
            delegate().close();
        }
        
        @Override
        public void flush() {
            delegate().flush();
        }
        
        @Override
        public void print(long l) {
            delegate().print(l);
        }
        
        @Override
        public void print(char[] s) {
            delegate().print(s);
        }
        
        @Override
        public void print(int i) {
            delegate().print(i);
        }
        
        @Override
        public void print(boolean b) {
            delegate().print(b);
        }
        
        @Override
        public void print(char c) {
            delegate().print(c);
        }
        
        @Override
        public void print(float f) {
            delegate().print(f);
        }
        
        @Override
        public void print(double d) {
            delegate().print(d);
        }
        
        @Override
        public void print(Object obj) {
            delegate().print(obj);
        }
        
        @Override
        public void print(String s) {
            delegate().print(s);
        }
        
        @Override
        public void println(double x) {
            delegate().println(x);
        }
        
        @Override
        public void println(Object x) {
            delegate().println(x);
        }
        
        @Override
        public void println(float x) {
            delegate().println(x);
        }
        
        @Override
        public void println(int x) {
            delegate().println(x);
        }
        
        @Override
        public void println(char x) {
            delegate().println(x);
        }
        
        @Override
        public void println(boolean x) {
            delegate().println(x);
        }
        
        @Override
        public void println(String x) {
            delegate().println(x);
        }
        
        @Override
        public void println(char[] x) {
            delegate().println(x);
        }
        
        @Override
        public void println() {
            delegate().println();
        }
        
        @Override
        public void println(long x) {
            delegate().println(x);
        }
        
        @Override
        public void write(int b) {
            delegate().write(b);
        }
        
        @Override
        public void write(byte[] b) throws IOException {
            delegate().write(b);
        }
        
        @Override
        public void write(byte[] b, int off, int len) {
            delegate().write(b, off, len);
        }

        @Override
        public PrintStream append(CharSequence csq) {
            return delegate().append(csq);
        }

        @Override
        public PrintStream append(char c) {
            return delegate().append(c);
        }

        @Override
        public PrintStream append(CharSequence csq, int start, int end) {
            return delegate().append(csq, start, end);
        }

        @Override
        public PrintStream format(String format, Object... args) {
            return delegate().format(format, args);
        }

        @Override
        public PrintStream format(Locale l, String format, Object... args) {
            return delegate().format(l, format, args);
        }

    }
}
