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
package org.netbeans.core.netigso;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.ArchiveResources;
import org.netbeans.Module;
import org.netbeans.Module.PackageExport;
import org.netbeans.NetigsoFramework;
import org.netbeans.ProxyClassLoader;
import org.netbeans.Stamps;
import static org.netbeans.core.netigso.Bundle.*;
import org.netbeans.core.startup.Main;
import org.openide.modules.ModuleInfo;
import org.openide.modules.Places;
import org.openide.util.*;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.service.packageadmin.RequiredBundle;
import org.osgi.service.startlevel.StartLevel;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@ServiceProviders({
    @ServiceProvider(service = NetigsoFramework.class),
    @ServiceProvider(service = Netigso.class)
})
public final class Netigso extends NetigsoFramework 
implements Cloneable, Stamps.Updater {
    static final Logger LOG = Logger.getLogger(Netigso.class.getName());
    private static final AtomicBoolean SELF_QUERY = new AtomicBoolean();
    private static final String[] EMPTY = {};

    private Framework framework;
    private ClassLoader frameworkLoader;
    private NetigsoActivator activator;
    private Integer defaultStartLevel;
    private String defaultCoveredPkgs;
    
    @Override
    protected NetigsoFramework clone() {
        return new Netigso();
    }

    Framework getFramework() {
        return framework;
    }
    @Override
    protected ClassLoader findFrameworkClassLoader() {
        ClassLoader l = frameworkLoader;
        if (l != null) {
            return l;
        }
        Framework f = framework;
        if (f != null) {
            return frameworkLoader = f.getClass().getClassLoader();
        }
        return getClass().getClassLoader();
    }

    @Override
    @Messages({"#NOI18N", "FRAMEWORK_START_LEVEL="})
    protected void prepare(Lookup lkp, Collection<? extends Module> preregister) {
        if (framework == null) {
            readBundles();
            
            Map configMap = new HashMap();
            injectSystemProperties(configMap); // ensure we read system properties
            final String cache = getNetigsoCache().getPath();
            configMap.put(Constants.FRAMEWORK_STORAGE, cache);
            activator = new NetigsoActivator(this);
            configMap.put("netigso.archive", NetigsoArchiveFactory.DEFAULT.create(this)); // NOI18N
            if (!configMap.containsKey("felix.log.level")) { // NOI18N
                String felixLevel = felixLogLevel(LOG);
                configMap.put("felix.log.level", felixLevel); // NOI18N
            }
            configMap.put("felix.bootdelegation.classloaders", activator); // NOI18N
            String startLevel = FRAMEWORK_START_LEVEL();
            if (!startLevel.isEmpty()) {
                configMap.put("org.osgi.framework.startlevel.beginning", startLevel); // NOI18N
            }
            FrameworkFactory frameworkFactory = lkp.lookup(FrameworkFactory.class);
            if (frameworkFactory == null) {
                throw new IllegalStateException(
                        "Cannot find OSGi framework implementation." + // NOI18N
                        " Is org.netbeans.libs.felix module or similar enabled?" // NOI18N
                        );
            }
            framework = frameworkFactory.newFramework(configMap);
            try {
                System.clearProperty("java.security.manager");
                framework.init();
                NetigsoServices ns = new NetigsoServices(this, framework);
            } catch (BundleException ex) {
                LOG.log(Level.SEVERE, "Cannot start OSGi framework", ex); // NOI18N
            }
            LOG.finer("OSGi Container initialized"); // NOI18N
        }
        for (Module mi : preregister) {
            try {
                fakeOneModule(mi, null);
            } catch (IOException ex) {
                LOG.log(Level.WARNING, "Cannot fake " + mi.getCodeName(), ex);
            }
        }
    }

    static String felixLogLevel(final Logger log) {
        String felixLevel = "1"; // NOI18N
        if (log.isLoggable(Level.WARNING)) {
            felixLevel = "2"; // NOI18N
        }
        if (log.isLoggable(Level.CONFIG)) {
            felixLevel = "3"; // NOI18N
        }
        if (log.isLoggable(Level.FINE)) {
            felixLevel = "4"; // NOI18N
        }
        return felixLevel;
    }

    @Override
    protected Set<String> start(Collection<? extends Module> allModules) {
        return toActivate(framework, allModules);
    }

    @Override
    protected void start() {
        try {
            framework.start();
        } catch (BundleException ex) {
            LOG.log(Level.WARNING, "Cannot start Container" + framework, ex);
        }
    }

    /** contributed by Alex Bowen (trajar@netbeans.org) */
    private void injectSystemProperties(Map configProps) {
        for (Enumeration<?> e = System.getProperties().propertyNames(); e.hasMoreElements(); ) {
            String key = e.nextElement().toString();
            if (key.startsWith("felix.") || key.startsWith("org.osgi.framework.")) { // NOI18N
                configProps.put(key, System.getProperty(key));
            }
        }
    }

    private static Set<String> toActivate(Framework f, Collection<? extends Module> allModules) {
        ServiceReference sr = f.getBundleContext().getServiceReference("org.osgi.service.packageadmin.PackageAdmin"); // NOI18N
        if (sr == null) {
            return null;
        }
        PackageAdmin pkgAdm = (PackageAdmin)f.getBundleContext().getService(sr);
        if (pkgAdm == null) {
            return null;
        }
        Set<String> allCnbs = new HashSet<String>(allModules.size() * 2);
        for (ModuleInfo m : allModules) {
            allCnbs.add(m.getCodeNameBase());
        }
        
        Set<String> needEnablement = new HashSet<String>();
        for (Bundle b : f.getBundleContext().getBundles()) {
            String loc = b.getLocation();
            if (loc.startsWith("netigso://")) {
                loc = loc.substring("netigso://".length());
            } else {
                continue;
            }
            RequiredBundle[] arr = pkgAdm.getRequiredBundles(loc);
            if (arr != null) for (RequiredBundle rb : arr) {
                for (Bundle n : rb.getRequiringBundles()) {
                    if (allCnbs.contains(n.getSymbolicName().replace('-', '_'))) {
                        needEnablement.add(loc);
                    }
                }
            }
        }
        return needEnablement;
    }

    @Override
    protected void shutdown() {
        try {
            if (framework != null) {
                framework.stop();
                framework.waitForStop(10000);
            }
            framework = null;
            frameworkLoader = null;
        } catch (InterruptedException ex) {
            LOG.log(Level.WARNING, "Wait for shutdown failed" + framework, ex);
        } catch (BundleException ex) {
            LOG.log(Level.WARNING, "Cannot start Container" + framework, ex);
        }
    }

    @Override
    @Messages({"#NOI18N", "DEFAULT_BUNDLE_START_LEVEL=0"})
    protected int defaultStartLevel() {
        if (defaultStartLevel == null) {
            defaultStartLevel = Integer.parseInt(DEFAULT_BUNDLE_START_LEVEL());
        }
        return defaultStartLevel;
    }

    @Override
    @Messages({"#NOI18N", "MODULE_START_LEVEL="})
    protected Set<String> createLoader(ModuleInfo m, ProxyClassLoader pcl, File jar) throws IOException {
        try {
            assert registered.containsKey(m.getCodeNameBase()) : m.getCodeNameBase();
            Bundle b = findBundle(m.getCodeNameBase());
            if (b == null) {
                for (Bundle bb : framework.getBundleContext().getBundles()) {
                    LOG.log(Level.FINE, "Bundle {0}: {1}", new Object[] { bb.getBundleId(), bb.getSymbolicName() });
                }
                throw new IOException("Not found bundle:" + m.getCodeNameBase());
            }
            NetigsoLoader l = new NetigsoLoader(b, m, jar);
            Set<String> pkgs = new HashSet<String>();
            String[] knownPkgs = registered.get(m.getCodeNameBase());
            Object exported = b.getHeaders("").get("Export-Package");
            if (knownPkgs == EMPTY) {
                try {
                    SELF_QUERY.set(true);
                    if (findCoveredPkgs(exported)) {
                        Enumeration<URL> en = b.findEntries("", null, true);
                        if (en == null) {
                            LOG.log(Level.INFO, "Bundle {0}: {1} is empty", new Object[] { b.getBundleId(), b.getSymbolicName() });
                        } else {
                            while (en.hasMoreElements()) {
                                URL url = en.nextElement();
                                if (url.getFile().startsWith("/META-INF")) {
                                    pkgs.add(url.getFile().substring(9));
                                    continue;
                                }
                                pkgs.add(url.getFile().substring(1).replaceFirst("/[^/]*$", "").replace('/', '.'));
                            }
                        }
                    }
                    if (exported instanceof String) {
                        for (String p : exported.toString().split(",")) { // NOI18N
                            pkgs.add(extractBundleName(p));
                        }
                    }
                } finally {
                    SELF_QUERY.set(false);
                }
                registered.put(m.getCodeNameBase(), pkgs.toArray(new String[0]));
                Stamps.getModulesJARs().scheduleSave(this, "netigso-bundles", false); // NOI18N
            } else {
                pkgs.addAll(Arrays.asList(knownPkgs));
            }
            pcl.append(new ClassLoader[]{ l });
            try {
                String msl = MODULE_START_LEVEL();
                boolean start = true;
                if (!msl.isEmpty()) {
                    int moduleStartLevel = Integer.parseInt(msl);
                    int level = getBundleStartLevel(b, framework.getBundleContext());
                    start = moduleStartLevel >= level;
                }
                LOG.log(Level.FINE, "Starting bundle {0}: {1}", new Object[] { m.getCodeNameBase(), start });
                if (start) {
                    b.start();
                    if (findCoveredPkgs(exported) && !isResolved(b) && isRealBundle(b)) {
                        throw new IOException("Cannot start " + m.getCodeName() + " state remains INSTALLED after start()"); // NOI18N
                    }
                }
            } catch (BundleException possible) {
                if (isRealBundle(b)) {
                    throw possible;
                }
                // Bundle is a fragment, replace it with host in classloader
                String fragmentHost = extractBundleName(b.getHeaders("").get("Fragment-Host"));
                Bundle hostBundle = findBundle(fragmentHost);
                if (hostBundle == null) {
                    LOG.log(Level.WARNING, "Failed to locate fragment host bundle {0} for fragment bundle {1}",
                            new Object[]{fragmentHost, m.getCodeNameBase()});
                    throw new IOException("Not found bundle: " + hostBundle);
                }
                l.setBundle(hostBundle);
                LOG.log(Level.FINE, "Not starting fragment {0}, using host bundle {1} for classloading instead", 
                        new Object[]{m.getCodeNameBase(), fragmentHost});
            }
            return pkgs;
        } catch (BundleException ex) {
            throw new IOException("Cannot start " + jar, ex);
        }
    }
    private static boolean isResolved(Bundle b) {
        if (b.getState() == Bundle.INSTALLED) {
            // try to ask for a known resource which is known to resolve 
            // the bundle
            b.findEntries("META-INF", "MANIFEST.MF", false); // NOI18N
        }
        return b.getState() != Bundle.INSTALLED;
    }

    private static boolean isRealBundle(Bundle b) {
        return b.getHeaders("").get("Fragment-Host") == null; // NOI18N
    }

    private static String extractBundleName(String fullBundleSpec) {
        if (fullBundleSpec == null) {
            return fullBundleSpec;
        }
        int semic = fullBundleSpec.indexOf(';');
        if (semic >= 0) {
            return fullBundleSpec.substring(0, semic);
        }
        return fullBundleSpec;
    }

    @Override
    protected void stopLoader(ModuleInfo m, ClassLoader loader) {
        NetigsoLoader nl = (NetigsoLoader)loader;
        Bundle b = nl.getBundle();
        try {
            assert b != null;
            try {
                LOG.log(Level.FINE, "Stopping bundle {0}", m.getCodeNameBase());
                b.stop();
            } catch (BundleException possible) {
                if (isRealBundle(b)) {
                    throw possible;
                }
                LOG.log(Level.FINE, "Not stopping fragment {0}", m.getCodeNameBase());
            }
        } catch (BundleException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    protected Enumeration<URL> findResources(Module m, String resName) {
        Bundle b = findBundle(m.getCodeNameBase());
        URL u = b.getEntry(resName);
        return u == null ? Enumerations.<URL>empty() : Enumerations.singleton(u);
    }

    @Override
    protected void reload(Module m) throws IOException {
        try {
            Bundle b = findBundle(m.getCodeNameBase());
            b.stop();
            fakeOneModule(m, b);
        } catch (BundleException ex) {
            throw new IOException(ex);
        }
    }

    //
    // take care about the registered bundles
    //
    private final Map<String,String[]> registered = new HashMap<String,String[]>();

    private static final RequestProcessor RP = new RequestProcessor("Netigso Events"); // NOI18N
    final void notifyBundleChange(final String symbolicName, final Version version, final int action) {
        final Exception stack = Netigso.LOG.isLoggable(Level.FINER) ? new Exception("StackTrace") : null;
        final Runnable doLog = new Runnable() {
            @Override
            public void run() {
                if (isEnabled(symbolicName)) {
                    return;
                }
                final Mutex mutex = Main.getModuleSystem().getManager().mutex();
                if (!mutex.isReadAccess()) {
                    mutex.postReadRequest(this);
                    return;
                }
                String type = "" + action;
                Level notify = Level.INFO;
                switch (action) {
                    case BundleEvent.INSTALLED:
                        return; // no message for installed
                    case BundleEvent.RESOLVED:
                        type = "resolved";
                        break;
                    case BundleEvent.STARTED:
                        type = "started";
                        break;
                    case BundleEvent.STOPPED:
                        type = "stopped";
                        break;
                    case BundleEvent.UNINSTALLED:
                        return; // nothing for uninstalled
                    case BundleEvent.LAZY_ACTIVATION:
                        type = "lazy";
                        notify = Level.FINEST;
                        break;
                    case BundleEvent.STARTING:
                        type = "starting";
                        notify = Level.FINEST;
                        break;
                }
                Netigso.LOG.log(notify, "bundle {0}@{2} {1}", new Object[]{
                            symbolicName, type, version
                        });
                if (stack != null) {
                    Netigso.LOG.log(Level.FINER, null, stack);
                }
            }
        };
        RP.post(doLog);
    }

    private File getNetigsoCache() throws IllegalStateException {
        // Explicitly specify the directory to use for caching bundles.
        return Places.getCacheSubdirectory("netigso");
    }

    private void deleteRec(File dir) {
        File[] arr = dir.listFiles();
        if (arr != null) {
            for (File f : arr) {
                deleteRec(f);
            }
        }
        dir.delete();
    }

    private void fakeOneModule(Module m, Bundle original) throws IOException {
        String cnb = m.getCodeNameBase();
        if (registered.get(cnb) != null && original == null) {
            return;
        }
        registered.put(cnb, EMPTY);
        Bundle b;
        try {
            String symbolicName = (String) m.getAttribute("Bundle-SymbolicName");
            if ("org.netbeans.core.osgi".equals(symbolicName)) { // NOI18N
                // Always ignore.
            } else if (symbolicName != null) { // NOI18N
                if (original != null) {
                    LOG.log(Level.FINE, "Updating bundle {0}", original.getLocation());
                    FileInputStream is = new FileInputStream(m.getJarFile());
                    original.update(is);
                    is.close();
                    b = original;
                } else {
                    BundleContext bc = framework.getBundleContext();
                    File jar = m.getJarFile();
                    String loc;
                    if (m.isReloadable()) {
                        loc = toURI(jar);
                    } else {
                        loc = "reference:" + toURI(jar); // NOI18N
                    }
                    LOG.log(Level.FINE, "Installing bundle {0}", loc);
                    b = bc.installBundle(loc);
                    int startLevel = m.getStartLevel();
                    if (startLevel == -1) {
                        startLevel = defaultStartLevel();
                    }
                    if (startLevel > 0) {
                        setBundleStartLevel(bc, b, startLevel);
                    }
                }
            } else {
                InputStream is = fakeBundle(m);
                if (is != null) {
                    if (original != null) {
                        original.update(is);
                        b = original;
                    } else {
                        assert framework != null;
                        BundleContext bc = framework.getBundleContext();
                        assert bc != null;
                        b = bc.installBundle("netigso://" + cnb, is);
                    }
                    is.close();
                }
            }
            Stamps.getModulesJARs().scheduleSave(this, "netigso-bundles", false); // NOI18N
        } catch (BundleException ex) {
            throw new IOException(ex);
        }
    }

    private void setFrameworkStartLevel(BundleContext bc, int startLevel) {
        ServiceReference sr = bc.getServiceReference("org.osgi.service.startlevel.StartLevel"); // NOI18N
        StartLevel level = null;
        if (sr != null) {
            level = (StartLevel) bc.getService(sr);
            if (level != null) {
                level.setStartLevel(startLevel);
                return;
            }
        }
        LOG.log(
            Level.WARNING, 
            "Cannot set framewok startLevel to {1} reference: {2} level {3}", 
            new Object[]{null, startLevel, sr, level}
        );
    }
    private void setBundleStartLevel(BundleContext bc, Bundle b, int startLevel) {
        ServiceReference sr = bc.getServiceReference("org.osgi.service.startlevel.StartLevel"); // NOI18N
        StartLevel level = null;
        if (sr != null) {
            level = (StartLevel) bc.getService(sr);
            if (level != null) {
                level.setBundleStartLevel(b, startLevel);
                return;
            }
        }
        LOG.log(
            Level.WARNING, 
            "Cannot set startLevel for {0} to {1} reference: {2} level {3}", 
            new Object[]{b.getSymbolicName(), startLevel, sr, level}
        );
    }
    
    private int getBundleStartLevel(Bundle b, BundleContext bc) {
        ServiceReference sr = bc.getServiceReference("org.osgi.service.startlevel.StartLevel"); // NOI18N
        StartLevel level = null;
        if (sr != null) {
            level = (StartLevel) bc.getService(sr);
            if (level != null) {
                return level.getBundleStartLevel(b);
            }
        }
        return 0;
    }

    
    private static String threeDotsWithMajor(String version, String withMajor) {
        int indx = withMajor.indexOf('/');
        int major = 0;
        if (indx > 0) {
            major = Integer.parseInt(withMajor.substring(indx + 1));
        }
        String[] segments = (version + ".0.0.0").split("\\.");
        assert segments.length >= 3 && segments[0].length() > 0;

        return (Integer.parseInt(segments[0]) + major * 100) + "."  + segments[1] + "." + segments[2];
    }

    /** Creates a fake bundle definition that represents one NetBeans module
     *
     * @param m the module
     * @return the stream to read the definition from or null, if it does not
     *   make sense to represent this module as bundle
     */
    private static InputStream fakeBundle(Module m) throws IOException {
        String netigsoExp = (String) m.getAttribute("Netigso-Export-Package"); // NOI18N
        String exp = (String) m.getAttribute("OpenIDE-Module-Public-Packages"); // NOI18N
        if (netigsoExp == null) {
            if ("-".equals(exp) || m.getAttribute("OpenIDE-Module-Friends") != null) { // NOI18N
                return null;
            }
        }
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Manifest man = new Manifest();
        man.getMainAttributes().putValue("Manifest-Version", "1.0"); // workaround for JDK bug
        man.getMainAttributes().putValue("Bundle-ManifestVersion", "2"); // NOI18N
        man.getMainAttributes().putValue("Bundle-SymbolicName", m.getCodeNameBase()); // NOI18N

        if (m.getSpecificationVersion() != null) {
            String spec = threeDotsWithMajor(m.getSpecificationVersion().toString(), m.getCodeName());
            man.getMainAttributes().putValue("Bundle-Version", spec.toString()); // NOI18N
        }
        if (netigsoExp != null) {
            man.getMainAttributes().putValue("Export-Package", netigsoExp); // NOI18N
        } else if (exp != null) {
            man.getMainAttributes().putValue("Export-Package", substitutePkg(m)); // NOI18N
        } else {
            man.getMainAttributes().putValue("Export-Package", m.getCodeNameBase()); // NOI18N
        }        
        JarOutputStream jos = new JarOutputStream(os, man);
        jos.close();
        return new ByteArrayInputStream(os.toByteArray());
    }

    private void readBundles() {
        assert registered.isEmpty();
        try {
            InputStream is = Stamps.getModulesJARs().asStream("netigso-bundles");
            if (is == null) {
                File f;
                try {
                    f = getNetigsoCache();
                } catch (IllegalStateException ex) {
                    return;
                }
                deleteRec(f);
                return;
            }
            Properties p = new Properties();
            p.load(is);
            is.close();
            for (Map.Entry<Object, Object> entry : p.entrySet()) {
                String k = (String)entry.getKey();
                String v = (String)entry.getValue();
                registered.put(k, v.trim().isEmpty() ? EMPTY : v.split(","));
                LOG.log(Level.FINE, "readBundle: {0}", k);
            }
        } catch (IOException ex) {
            LOG.log(Level.WARNING, "Cannot read cache", ex);
        }
    }

    @Override
    public void flushCaches(DataOutputStream os) throws IOException {
        Properties p = new Properties();
        for (Map.Entry<String, String[]> entry : registered.entrySet()) {
            StringBuilder sb = new StringBuilder();
            String sep = "";
            for (String s : entry.getValue()) {
                sb.append(sep);
                sb.append(s);
                sep = ",";
            }
            p.setProperty(entry.getKey(), sb.toString());
        }

        p.store(os, null);
    }

    @Override
    public void cacheReady() {
    }

    private Bundle findBundle(String codeNameBase) {
        for (Bundle bb : framework.getBundleContext().getBundles()) {
            final String bbName = bb.getSymbolicName().replace('-', '_');
            if (bbName.equals(codeNameBase)) {
                return bb;
            }
        }
        return null;
    }

    public byte[] fromArchive(long bundleId, String resource, ArchiveResources ar) throws IOException {
        if (SELF_QUERY.get()) {
            return ar.resource(resource);
        }
        return fromArchive(ar, resource);
    }

    public boolean isArchiveActive() {
        return !SELF_QUERY.get();
    }

    private static String toURI(final File file) {
        class VFile extends File {

            public VFile() {
                super(file.getPath());
            }

            @Override
            public boolean isDirectory() {
                return false;
            }

            @Override
            public File getAbsoluteFile() {
                return this;
            }
        }
        return Utilities.toURI(new VFile()).toString();
    }

    @Messages({"#NOI18N", "FIND_COVERED_PKGS=findEntries"})
    private boolean findCoveredPkgs(Object exportedPackages) {
        if (defaultCoveredPkgs == null) {
            defaultCoveredPkgs = FIND_COVERED_PKGS();
        }
        if ("exportedIfPresent".equals(defaultCoveredPkgs)) { // NOI18N
            return exportedPackages == null;
        }
        
        return "findEntries".equals(defaultCoveredPkgs); // NOI18N
    }

    final ClassLoader findClassLoader(String cnb) {
        return createClassLoader(cnb);
    }
    private boolean isEnabled(String cnd) {
        Module m = findModule(cnd);
        return m != null && m.isEnabled();
    }
    private static String substitutePkg(Module m) {
        StringBuilder exported = new StringBuilder();
        String sep = "";
        PackageExport[] pblk = m.getPublicPackages();
        if (pblk == null) {
            pblk = new PackageExport[1];
            pblk[0] = new PackageExport("", true);
        }
        
        for (Module.PackageExport packageExport : pblk) {
            Set<String> pkgs;
            if (packageExport.recursive) {
                pkgs = findRecursivePkgs(m, packageExport);
            } else {
                pkgs = Collections.singleton(packageExport.pkg);
            }
            for (String p : pkgs) {
                if (p.endsWith("/")) { // NOI18N
                    p = p.substring(0, p.length() - 1);
                }
                exported.append(sep).append(p.replace('/', '.'));
                sep = ",";
            }
        }
        if (exported.length() == 0) {
            exported.append(m.getCodeNameBase());
        }
        return exported.toString();
    }
    private static Set<String> findRecursivePkgs(Module m, PackageExport packageExport)  {
        Set<String> pkgs = new HashSet<String>();
        for (File f : m.getAllJars()) {
            JarFile jf = null;
            try {
                jf = new JarFile(f);
                Enumeration<JarEntry> en = jf.entries();
                while (en.hasMoreElements()) {
                    JarEntry e = en.nextElement();
                    if (e.isDirectory()) {
                        continue;
                    }
                    final String entryName = e.getName();
                    int lastSlash = entryName.lastIndexOf('/');
                    if (lastSlash == -1) {
                        continue;
                    }
                    String pkg = entryName.substring(0, lastSlash + 1);
                    if (pkg.startsWith(packageExport.pkg)) {
                        pkgs.add(pkg);
                    }
                }
            } catch (IOException ex) {
                LOG.log(Level.INFO, "Can't process " + f, ex);
            } finally {
                try {
                    jf.close();
                } catch (IOException ex) {
                    LOG.log(Level.INFO, "Can't close " + f, ex);
                }
            }
        }
        return pkgs;
    }

    public final byte[] patchBC(ClassLoader l, String className, ProtectionDomain pd, byte[] arr) {
        return patchByteCode(l, className, pd, arr);
    }
}
