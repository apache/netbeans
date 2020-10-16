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

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import java.util.jar.Manifest;
import org.netbeans.ArchiveResources;
import org.netbeans.Module;
import org.netbeans.ProxyClassLoader;
import org.netbeans.core.netigso.spi.BundleContent;
import org.netbeans.core.netigso.spi.NetigsoArchive;
import org.netbeans.junit.MockServices;
import org.openide.modules.Dependency;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Lookup;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.Version;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

public class NetigsoSelfQueryTest extends NetigsoHid {

    public NetigsoSelfQueryTest(String name) {
        super(name);
    }

    protected @Override void setUp() throws Exception {
        Locale.setDefault(Locale.US);
        clearWorkDir();
        System.setProperty("netbeans.user", getWorkDirPath());

        MockServices.setServices(MockFrameworkFactory.class);
    }
    
    public void testSelfInspectionIsNotArchived() throws Exception {
        Netigso nf = Lookup.getDefault().lookup(Netigso.class);
        assertNotNull("Framework found", nf);

        class MI extends Module {

            public MI() throws IOException {
                super(null, null, null, false, false, false);
            }

            @Override
            public String getCodeNameBase() {
                return "org.test";
            }

            @Override
            public int getCodeNameRelease() {
                return 0;
            }

            @Override
            public String getCodeName() {
                return "org.test";
            }

            @Override
            public SpecificationVersion getSpecificationVersion() {
                return new SpecificationVersion("1.2");
            }

            @Override
            public boolean isEnabled() {
                return true;
            }

            @Override
            public Object getAttribute(String attr) {
                return null;
            }

            @Override
            public Object getLocalizedAttribute(String attr) {
                return null;
            }

            @Override
            public Set<Dependency> getDependencies() {
                return Collections.emptySet();
            }

            @Override
            public List<File> getAllJars() {
                return Collections.singletonList(getJarFile());
            }

            @Override
            public void setReloadable(boolean r) {
            }

            @Override
            public void reload() throws IOException {
            }

            @Override
            protected void classLoaderUp(Set<Module> parents) throws IOException {
            }

            @Override
            protected void classLoaderDown() {
            }

            @Override
            protected void cleanup() {
            }

            @Override
            protected void destroy() {
            }

            @Override
            public boolean isFixed() {
                return false;
            }

            @Override
            public Manifest getManifest() {
                return new Manifest();
            }

        }
        MI mi = new MI();
        ProxyClassLoader pcl = new ProxyClassLoader(new ClassLoader[0], false);
        nf.prepare(Lookup.getDefault(), Collections.singleton(mi));
        Set<String> set = nf.createLoader(mi, pcl, jars);

        assertTrue("org.test.pkg: " + set, set.contains("org.test.pkg"));
    }

    public static final class MockFrameworkFactory implements FrameworkFactory {
        @Override
        public Framework newFramework(Map map) {
            NetigsoArchive archive = (NetigsoArchive) map.get("netigso.archive");
            assertNotNull("archive provided", archive);
            AtomicReference<BundleContext> ar = new AtomicReference<BundleContext>();
            return (Framework) delegate(new MockFramework(archive, ar), ar, Framework.class, BundleContext.class);
        }

    }
    
    private static Object delegate(final Object inst, AtomicReference ar, Class... types) {
        class Del implements InvocationHandler {
            @Override
            public Object invoke(Object o, Method method, Object[] os) throws Throwable {
                Method myMethod = inst.getClass().getMethod(
                    method.getName(), method.getParameterTypes()
                );
                return myMethod.invoke(inst, os);
            }
        }
        Object ret = Proxy.newProxyInstance(
            NetigsoSelfQueryTest.class.getClassLoader(), 
            types, 
            new Del()
        );
        ar.set(ret);
        return ret;
    }
    
    private static final class MockFramework {
        private final NetigsoArchive archive;
        private final List<Bundle> bundles = new ArrayList<Bundle>();
        private final AtomicReference<BundleContext> handler;
        
        public MockFramework(NetigsoArchive archive, AtomicReference<BundleContext> ab) {
            this.archive = archive;
            this.handler = ab;
        }

        public void init() throws BundleException {
        }

        public FrameworkEvent waitForStop(long l) throws InterruptedException {
            return null;
        }

        public void start() throws BundleException {
        }

        public void start(int i) throws BundleException {
        }

        public void stop() throws BundleException {
        }

        public void stop(int i) throws BundleException {
        }

        public void uninstall() throws BundleException {
        }

        public void update() throws BundleException {
        }

        public void update(InputStream in) throws BundleException {
        }

        public long getBundleId() {
            return 0;
        }

        public String getLocation() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public String getSymbolicName() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public int getState() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Dictionary getHeaders() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public ServiceReference[] getRegisteredServices() {
            return new ServiceReference[0];
        }

        public ServiceReference[] getServicesInUse() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean hasPermission(Object o) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public URL getResource(String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Dictionary getHeaders(String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Class<?> loadClass(String string) throws ClassNotFoundException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Enumeration<?> getResources(String string) throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Enumeration<?> getEntryPaths(String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public URL getEntry(String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public long getLastModified() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Enumeration<URL> findEntries(String string, String string1, boolean bln) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public BundleContext getBundleContext() {
            return handler.get();
        }

        public Map<?, ?> getSignerCertificates(int i) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Version getVersion() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public String getProperty(String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Bundle getBundle() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Bundle installBundle(String url, InputStream in) throws BundleException {
            final MockBundle b = new MockBundle(url, this);
            final Bundle bundle = (Bundle) delegate(b, new AtomicReference(), Bundle.class);
            bundles.add(bundle);
            return bundle;
        }

        public Bundle installBundle(String string) throws BundleException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Bundle getBundle(long l) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Bundle[] getBundles() {
            return bundles.toArray(new Bundle[0]);
        }

        public void addServiceListener(ServiceListener sl, String string) throws InvalidSyntaxException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void addServiceListener(ServiceListener sl) {
        }

        public void removeServiceListener(ServiceListener sl) {
        }

        public void addBundleListener(BundleListener bl) {
        }

        public void removeBundleListener(BundleListener bl) {
        }

        public void addFrameworkListener(FrameworkListener fl) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void removeFrameworkListener(FrameworkListener fl) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public ServiceRegistration registerService(String[] strings, Object o, Dictionary dctnr) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public ServiceRegistration registerService(String string, Object o, Dictionary dctnr) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public ServiceReference[] getServiceReferences(String string, String string1) throws InvalidSyntaxException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public ServiceReference[] getAllServiceReferences(String string, String string1) throws InvalidSyntaxException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public ServiceReference getServiceReference(String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Object getService(ServiceReference sr) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean ungetService(ServiceReference sr) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public File getDataFile(String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Filter createFilter(String string) throws InvalidSyntaxException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Object adapt(Class type) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public int compareTo(Object o) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public ServiceRegistration registerService(Class type, Object s, Dictionary dctnr) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public ServiceReference getServiceReference(Class type) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Collection getServiceReferences(Class type, String string) throws InvalidSyntaxException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Bundle getBundle(String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }

    private static final class MockBundle implements BundleContent {
        private final String url;
        private final MockFramework f;
        private final NetigsoArchive archive;
        private transient int state = Bundle.INSTALLED;

        public MockBundle(String url, MockFramework f) {
            this.url = url;
            this.f = f;
            this.archive = f.archive.forBundle(10, this);
        }

        public int getState() {
            return state;
        }

        public void start(int i) throws BundleException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void start() throws BundleException {
            state = Bundle.ACTIVE;
        }

        public void stop(int i) throws BundleException {
        }

        public void stop() throws BundleException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void update(InputStream in) throws BundleException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void update() throws BundleException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void uninstall() throws BundleException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        private Dictionary empty = new Hashtable();
        public Dictionary getHeaders(String locale) {
            return empty;
        }
        
        public Dictionary getHeaders() {
            fail("Don't ever call me, call getHeaders(\"\")");
            return null;
        }
        
        public long getBundleId() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public String getLocation() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public ServiceReference[] getRegisteredServices() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public ServiceReference[] getServicesInUse() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean hasPermission(Object o) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public URL getResource(String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public String getSymbolicName() {
            return "org.test";
        }

        public Class<?> loadClass(String string) throws ClassNotFoundException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Enumeration<?> getResources(String string) throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Enumeration<?> getEntryPaths(String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public URL getEntry(String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public long getLastModified() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Enumeration<URL> findEntries(String string, String string1, boolean bln) {
            Set<URL> set = new HashSet<URL>();
            try {
                if (archive.fromArchive("org/test/pkg/MyClass.class") != null) {
                    set.add(new URL("file:/org/test/pkg/MyClass.class"));
                }
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
            return Collections.enumeration(set);
        }

        @Override
        public byte[] resource(String name) throws IOException {
            for (StackTraceElement e : new Exception().getStackTrace()) {
                if (e.getClassName().equals("org.netbeans.Archive")) {
                    fail("Cannot be called from archive!");
                }
            }

            if (name.equals("org/test/pkg/MyClass.class")) {
                return new byte[1];
            }
            return null;
        }

        public BundleContext getBundleContext() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Map getSignerCertificates(int i) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Version getVersion() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Object adapt(Class type) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public File getDataFile(String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public int compareTo(Object o) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
