/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.netbinox;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.net.URLConnection;
import java.security.ProtectionDomain;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.eclipse.osgi.baseadaptor.BaseAdaptor;
import org.eclipse.osgi.baseadaptor.BaseData;
import org.eclipse.osgi.baseadaptor.HookConfigurator;
import org.eclipse.osgi.baseadaptor.HookRegistry;
import org.eclipse.osgi.baseadaptor.bundlefile.BundleEntry;
import org.eclipse.osgi.baseadaptor.bundlefile.BundleFile;
import org.eclipse.osgi.baseadaptor.bundlefile.MRUBundleFileList;
import org.eclipse.osgi.baseadaptor.hooks.AdaptorHook;
import org.eclipse.osgi.baseadaptor.hooks.BundleFileFactoryHook;
import org.eclipse.osgi.baseadaptor.hooks.ClassLoadingHook;
import org.eclipse.osgi.baseadaptor.loader.BaseClassLoader;
import org.eclipse.osgi.baseadaptor.loader.ClasspathEntry;
import org.eclipse.osgi.baseadaptor.loader.ClasspathManager;
import org.eclipse.osgi.framework.adaptor.BundleProtectionDomain;
import org.eclipse.osgi.framework.adaptor.ClassLoaderDelegate;
import org.eclipse.osgi.framework.log.FrameworkLog;
import org.eclipse.osgi.framework.log.FrameworkLogEntry;
import org.netbeans.core.netigso.spi.NetigsoArchive;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.wiring.BundleWiring;

/**
 *
 * @author Jaroslav Tulach <jaroslav.tulach@netbeans.org>
 */
public final class NetbinoxHooks implements HookConfigurator, ClassLoadingHook,
BundleFileFactoryHook, FrameworkLog, FrameworkListener, AdaptorHook, LookupListener {
    private static Map<Bundle,ClassLoader> map;
    private static NetigsoArchive archive;
    private static Lookup.Result<HookConfigurator> configurators;
    private static Collection<? extends HookConfigurator> previous = Collections.emptyList();
    private static HookRegistry hookRegistry;
    
    @Override
    public void addHooks(HookRegistry hr) {
        initRegistry(hr, this);
        initAndRefresh();
    }


    @Override
    public byte[] processClass(String className, byte[] bytes, ClasspathEntry ce, BundleEntry be, ClasspathManager cm) {
        final BaseData bd = ce.getBaseData();
        if (bd == null) {
            return bytes;
        }
        final Bundle b = bd.getBundle();
        if (b == null) {
            return bytes;
        }
        BundleWiring w = b.adapt(org.osgi.framework.wiring.BundleWiring.class);
        if (w == null) {
            return bytes;
        }
        ClassLoader loader = w.getClassLoader();
        return archive.patchByteCode(loader, className, ce.getDomain(), bytes);
    }

    @Override
    public boolean addClassPathEntry(ArrayList al, String string, ClasspathManager cm, BaseData bd, ProtectionDomain pd) {
        return false;
    }

    @Override
    public String findLibrary(BaseData bd, String string) {
        return null;
    }

    @Override
    public ClassLoader getBundleClassLoaderParent() {
        return null;
    }

    @Override
    public BaseClassLoader createClassLoader(ClassLoader parent, final ClassLoaderDelegate delegate, final BundleProtectionDomain bpd, BaseData bd, String[] classpath) {
        String loc = bd.getBundle().getLocation();
        //NetigsoModule.LOG.log(Level.FINER, "createClassLoader {0}", bd.getLocation());
        final String pref = "netigso://"; // NOI18N
        ClassLoader ml = null;
        if (loc != null && loc.startsWith(pref)) {
            ml = classLoaderForBundle(bd);
        }
        if (ml == null) {
            return new NetbinoxLoader(parent, delegate, bpd, bd, classpath);
        } else {
            return new NetigsoBaseLoader(ml, delegate, bpd, bd);
        }
    }

    @Override
    public void initializedClassLoader(BaseClassLoader bcl, BaseData bd) {
    }

    private final MRUBundleFileList mruList = new MRUBundleFileList();
    @Override
    public BundleFile createBundleFile(Object file, final BaseData bd, boolean isBase) throws IOException {

        if (file instanceof File) {
            final File f = (File)file;
// running with fake manifest fails for some reason, disabling for now
//            final String loc = bd.getLocation();
//            if (loc != null && loc.startsWith("netigso://")) {
//                return new NetigsoBundleFile(f, bd);
//            }
            return new JarBundleFile(f, bd, archive, mruList, isBase);
        }
        return null;
    }

    @Override
    public void frameworkEvent(FrameworkEvent ev) {
		if (ev.getType() == FrameworkEvent.ERROR) {
            log(ev);
		}
    }
    
    @Override
    public void log(FrameworkEvent fe) {
        Level l = Level.FINE;
        if ((fe.getType() & FrameworkEvent.ERROR) != 0) {
            l = Level.SEVERE;
        } else if ((fe.getType() & FrameworkEvent.WARNING) != 0) {
            l = Level.WARNING;
        } else if ((fe.getType() & FrameworkEvent.INFO) != 0) {
            l = Level.INFO;
        }
        LogRecord lr = new LogRecord(l, "framework event {0} type {1}");
        lr.setParameters(new Object[]{fe.getBundle().getSymbolicName(), fe.getType()});
        lr.setThrown(fe.getThrowable());
        lr.setLoggerName(NetbinoxFactory.LOG.getName());
        NetbinoxFactory.LOG.log(lr);
    }

    @Override
    public void log(FrameworkLogEntry fle) {
        NetbinoxFactory.LOG.log(Level.FINE, "entry {0}", fle);
    }

    @Override
    public void setWriter(Writer writer, boolean bln) {
    }

    @Override
    public void setFile(File file, boolean bln) throws IOException {
    }

    @Override
    public File getFile() {
        return null;
    }

    @Override
    public void setConsoleLog(boolean bln) {
    }

    @Override
    public void close() {
    }

    // adaptor hooks

    @Override
    public void initialize(BaseAdaptor ba) {
    }

    @Override
    public void frameworkStart(BundleContext bc) throws BundleException {
        bc.addFrameworkListener(this);
    }

    @Override
    public void frameworkStop(BundleContext bc) throws BundleException {
        bc.removeFrameworkListener(this);
    }

    @Override
    public void frameworkStopping(BundleContext bc) {
    }

    @Override
    public void addProperties(Properties prprts) {
    }

    @Override
    public URLConnection mapLocationToURLConnection(String string) throws IOException {
        return null;
    }

    @Override
    public void handleRuntimeError(Throwable thrwbl) {
        NetbinoxFactory.LOG.log(Level.WARNING, thrwbl.getMessage(), thrwbl);
    }

    @Override
    public FrameworkLog createFrameworkLog() {
        return this;
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        initAndRefresh();
    }
    
    //
    // synchronized to access internal data structures
    //
    private void initAndRefresh() {
        Set<HookConfigurator> added;
        synchronized (NetbinoxHooks.class) {
            if (configurators == null) {
                configurators = Lookup.getDefault().lookupResult(HookConfigurator.class);
                configurators.addLookupListener(this);
            }
    
            Collection<? extends HookConfigurator> now = configurators.allInstances();
            added = new HashSet<HookConfigurator>(now);
            added.removeAll(previous);
            previous = now;
        }
        for (HookConfigurator hc : added) {
            hc.addHooks(hookRegistry);
        }
    }
    static synchronized void clear() {
        map = null;
        archive = null;
        configurators = null;
        hookRegistry = null;
    }
    private static synchronized ClassLoader classLoaderForBundle(BaseData bd) {
        if (map == null) {
            return null;
        }
        return map.get(bd.getBundle());
    }


    static synchronized void registerMap(Map<Bundle, ClassLoader> bundleMap) {
        map = bundleMap;
    }

    static synchronized void registerArchive(NetigsoArchive netigsoArchive) {
        archive = netigsoArchive;
    }

    private static synchronized void initRegistry(HookRegistry hr, NetbinoxHooks hooks) {
        hookRegistry = hr;
        hr.addClassLoadingHook(hooks);
        hr.addBundleFileFactoryHook(hooks);
        hr.addAdaptorHook(hooks);
    }
}
