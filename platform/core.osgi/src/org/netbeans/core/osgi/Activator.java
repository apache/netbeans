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

package org.netbeans.core.osgi;

import java.beans.Introspector;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.core.startup.CoreBridge;
import org.netbeans.core.startup.Main;
import org.netbeans.core.startup.RunLevel;
import org.netbeans.core.startup.Splash;
import org.openide.modules.ModuleInstall;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbCollections;
import org.openide.util.SharedClassObject;
import org.openide.util.lookup.Lookups;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.SynchronousBundleListener;
import org.osgi.framework.launch.Framework;
import org.osgi.service.url.AbstractURLStreamHandlerService;
import org.osgi.service.url.URLConstants;
import org.osgi.service.url.URLStreamHandlerService;

/**
 * Initializes critical NetBeans infrastructure inside an OSGi container.
 */
public class Activator implements BundleActivator, SynchronousBundleListener {

    private static final Logger LOG = Logger.getLogger(Activator.class.getName());

    public Activator() {}

    /** Bundles which have been loaded or are in line to be loaded. */
    private DependencyQueue<String,Bundle> queue;
    private BundleContext context;
    private Framework framework;

    public @Override void start(final BundleContext context) throws Exception {
        if (System.getProperty("netbeans.home") != null) {
            throw new IllegalStateException("Should not be run from inside regular NetBeans module system");
        }
        String storage = context.getProperty(Constants.FRAMEWORK_STORAGE);
        if (storage != null) {
            System.setProperty("netbeans.user", storage);
        }
        System.setProperty("TopSecurityManager.disable", "true");
        NbBundle.setBranding(System.getProperty("branding.token"));
        OSGiMainLookup.initialize(context);
        queue = new DependencyQueue<String,Bundle>();
        this.context = context;
        framework = ((Framework) context.getBundle(0));
        if (framework.getState() == Bundle.STARTING) {
            LOG.fine("framework still starting");
            final AtomicReference<FrameworkListener> frameworkListener = new AtomicReference<FrameworkListener>();
            frameworkListener.set(new FrameworkListener() {
                public @Override void frameworkEvent(FrameworkEvent event) {
                    if (event.getType() == FrameworkEvent.STARTED) {
//                        System.err.println("framework started");
                        context.removeFrameworkListener(frameworkListener.get());
                        context.addBundleListener(Activator.this);
                        processLoadedBundles();
                    }
                }
            });
            context.addFrameworkListener(frameworkListener.get());
        } else {
            LOG.fine("framework already started");
            context.addBundleListener(this);
            processLoadedBundles();
        }
    }

    private void processLoadedBundles() {
        List<Bundle> toLoad = new ArrayList<Bundle>();
        for (Bundle b : context.getBundles()) {
            if (b.getState() == Bundle.ACTIVE) {
                Dictionary<?,?> headers = b.getHeaders();
                toLoad.addAll(queue.offer(b, provides(headers), requires(headers), needs(headers)));
            }
        }
//        System.err.println("processing already loaded bundles: " + toLoad);
        load(toLoad);
    }

    public @Override void stop(BundleContext context) throws Exception {}

    public @Override void bundleChanged(BundleEvent event) {
        Bundle bundle = event.getBundle();
        switch (event.getType()) {
        case BundleEvent.STARTED:
//            System.err.println("started " + bundle.getSymbolicName());
            Dictionary<?,?> headers = bundle.getHeaders();
            load(queue.offer(bundle, provides(headers), requires(headers), needs(headers)));
            break;
        case BundleEvent.STOPPED:
//            System.err.println("stopped " + bundle.getSymbolicName());
            if (framework.getState() == Bundle.STOPPING) {
//                System.err.println("fwork stopping during " + bundle.getSymbolicName());
//                ActiveQueue.stop();
            } else {
                unload(queue.retract(bundle));
            }
            break;
        }
    }

    static Set<String> provides(Dictionary<?,?> headers) {
        Set<String> deps = new TreeSet<String>(splitTokens((String) headers.get("OpenIDE-Module-Provides")));
        String name = (String) headers.get(Constants.BUNDLE_SYMBOLICNAME);
        if (name != null) {
            name = name.replaceFirst(";.+", "");
            deps.add("cnb." + name);
            if (name.equals("org.openide.modules")) {
                CoreBridge.defineOsTokens(deps);
            }
        }
        return deps;
    }

    static Set<String> requires(Dictionary<?,?> headers) {
        Set<String> deps = new TreeSet<String>();
        String v = (String) headers.get(Constants.REQUIRE_BUNDLE);
        if (v != null) {
            // PackageAdmin.getRequiredBundles is not suitable for this - it is backwards.
            // XXX try to follow the spec more closely; this will work at least for headers created by MakeOSGi:
            for (String item : v.split(", ")) {
                deps.add("cnb." + item.replaceFirst(";.+", ""));
            }
        }
        // XXX also check for BUNDLE_SYMBOLICNAME_ATTRIBUTE in IMPORT_PACKAGE (though not currently used by MakeOSGi)
        deps.addAll(splitTokens((String) headers.get("OpenIDE-Module-Requires")));
        return deps;
    }

    static Set<String> needs(Dictionary<?,?> headers) {
        return splitTokens((String) headers.get("OpenIDE-Module-Needs"));
    }

    private static Set<String> splitTokens(String tokens) {
        if (tokens == null) {
            return Collections.emptySet();
        }
        Set<String> split = new TreeSet<String>(Arrays.asList(tokens.split("[, ]+")));
        split.remove("");
        return split;
    }

    static final Map<Bundle,ModuleInstall> installers = new HashMap<Bundle,ModuleInstall>();

    private void load(List<Bundle> bundles) {
        if (bundles.isEmpty() || bundles.size() == 1 && bundles.iterator().next().getBundleId() == 0) {
            return;
        }
        LOG.log(Level.FINE, "loading: {0}", bundles);
        OSGiMainLookup.bundlesAdded(bundles);
        boolean showWindowSystem = false; // trigger for showing main window and setting up related GUI elements
        boolean loadServicesFolder = false;
        for (Bundle bundle : bundles) {
            registerUrlProtocolHandlers(bundle);
            if (bundle.getSymbolicName().equals("org.netbeans.core")) { // NOI18N
                loadServicesFolder = true;
            } else if (bundle.getSymbolicName().equals("org.netbeans.bootstrap")) { // NOI18N
                System.setProperty("netbeans.buildnumber", bundle.getVersion().getQualifier()); // NOI18N
            } else if (bundle.getSymbolicName().equals("org.netbeans.core.windows")) { // NOI18N
                showWindowSystem = true;
            }
        }
        OSGiRepository.DEFAULT.addLayersFor(bundles);
        if (loadServicesFolder) {
            OSGiMainLookup.loadServicesFolder();
        }
        if (showWindowSystem) {
            Splash.getInstance().setRunning(true);
            Main.initUICustomizations();
        }
        for (Bundle bundle : bundles) {
            ModuleInstall mi = installerFor(bundle);
            if (mi != null) {
                installers.put(bundle, mi);
                LOG.log(Level.FINE, "restored: {0}", bundle.getSymbolicName());
                mi.restored();
            }
        }
        ClassLoader l = Lookup.getDefault().lookup(ClassLoader.class);
        final Lookup start = Lookups.metaInfServices(l, "META-INF/namedservices/Modules/Start/");
        // NbStartStop not quite appropriate here; will not properly handle multiple enable/disable cycles
        // (but it does run them in parallel, which may be desirable)
        for (Runnable r : start.lookupAll(Runnable.class)) {
            if (bundles.contains(FrameworkUtil.getBundle(r.getClass()))) {
                LOG.log(Level.FINE, "starting {0}", r.getClass().getName());
                r.run();
            }
        }
        if (showWindowSystem) {
            // XXX set ${jdk.home}?
            List<String> bisp = new ArrayList<String>(Arrays.asList(Introspector.getBeanInfoSearchPath()));
            bisp.add("org.netbeans.beaninfo"); // NOI18N
            Introspector.setBeanInfoSearchPath(bisp.toArray(new String[0]));
            CoreBridge.getDefault().registerPropertyEditors();
        }
        for (RunLevel rl : Lookup.getDefault().lookupAll(RunLevel.class)) {
            rl.run();
        }
        if (showWindowSystem) {
            Splash.getInstance().setRunning(false);
        }
    }

    private void unload(List<Bundle> bundles) {
        if (bundles.isEmpty()) {
            return;
        }
        LOG.log(Level.FINE, "unloading: {0}", bundles);
        ClassLoader l = Lookup.getDefault().lookup(ClassLoader.class);
        final Lookup stop = Lookups.metaInfServices(l, "META-INF/namedservices/Modules/Stop/");
        for (Callable<?> r : stop.lookupAll(Callable.class)) {
            if (bundles.contains(FrameworkUtil.getBundle(r.getClass()))) {
                try {
                    if (!((Boolean) r.call())) {
                        LOG.log(Level.WARNING, "ignoring false return value from {0}", r.getClass().getName());
                    }
                } catch (Exception x) {
                    LOG.log(Level.WARNING, null, x);
                }
            }
        }
        for (Runnable r : stop.lookupAll(Runnable.class)) {
            if (bundles.contains(FrameworkUtil.getBundle(r.getClass()))) {
                r.run();
            }
        }
        for (Bundle bundle : bundles) {
            ModuleInstall mi = installers.remove(bundle);
            if (mi != null) {
                LOG.log(Level.FINE, "uninstalled: {0}", bundle.getSymbolicName());
                mi.uninstalled();
            }
        }
        OSGiRepository.DEFAULT.removeLayersFor(bundles);
        OSGiMainLookup.bundlesRemoved(bundles);
    }

    private static ModuleInstall installerFor(Bundle b) {
        if (b.getSymbolicName().equals("org.netbeans.modules.autoupdate.ui")) { // NOI18N
            // Won't work anyway, so don't even try.
            return null;
        }
        String respath = b.getHeaders().get("OpenIDE-Module-Install");
        if (respath != null) {
            String fqn = respath.replaceFirst("[.]class$", "").replace('/', '.');
            try {
                return SharedClassObject.findObject(((Class<?>) b.loadClass(fqn)).asSubclass(ModuleInstall.class), true);
            } catch (Exception x) { // CNFE, CCE, ...
                LOG.log(Level.WARNING, "Could not load " + fqn, x);
                return null;
            }
        }
        return null;
    }

    private void registerUrlProtocolHandlers(final Bundle bundle) {
        Enumeration<?> e = bundle.getEntryPaths("META-INF/namedservices/URLStreamHandler/");
        if (e != null) {
            for (String path : NbCollections.iterable(NbCollections.checkedEnumerationByFilter(e, String.class, true))) {
                URL entry = bundle.getEntry(path + "java.net.URLStreamHandler");
                if (entry != null) {
                    String protocol = path.replaceAll("^META-INF/namedservices/URLStreamHandler/|/$", "");
                    try {
                        InputStream is = entry.openStream();
                        try {
                            BufferedReader r = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                            String line;
                            while ((line = r.readLine()) != null) {
                                if (!line.isEmpty() && !line.startsWith("#")) {
                                    final String fqn = line;
                                    Dictionary<String,Object> props = new Hashtable<String,Object>();
                                    props.put(URLConstants.URL_HANDLER_PROTOCOL, protocol);
                                    class Svc extends AbstractURLStreamHandlerService {
                                        public @Override URLConnection openConnection(final URL u) throws IOException {
                                            try {
                                                URLStreamHandler handler = (URLStreamHandler) bundle.loadClass(fqn).getDeclaredConstructor().newInstance();
                                                Method openConnection = URLStreamHandler.class.getDeclaredMethod("openConnection", URL.class);
                                                openConnection.setAccessible(true);
                                                return (URLConnection) openConnection.invoke(handler, u);
                                            } catch (Exception x) {
                                                throw (IOException) new IOException(x.toString()).initCause(x);
                                            }
                                        }
                                    }
                                    BundleContext context = bundle.getBundleContext();
                                    if (context != null) {
                                        context.registerService(URLStreamHandlerService.class.getName(), new Svc(), props);
                                    } else {
                                        LOG.log(Level.WARNING, "no context for {0} in state {1}", new Object[] {bundle.getSymbolicName(), bundle.getState()});
                                    }
                                }
                            }
                        } finally {
                            is.close();
                        }
                    } catch (Exception x) {
                        LOG.log(Level.WARNING, "Could not load protocol handler for " + protocol + " from " + bundle.getSymbolicName(), x);
                    }
                }
            }
        }
    }

}
