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

import java.io.IOException;
import java.net.URL;
import java.security.ProtectionDomain;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import org.eclipse.osgi.baseadaptor.BaseData;
import org.eclipse.osgi.baseadaptor.bundlefile.BundleEntry;
import org.eclipse.osgi.baseadaptor.bundlefile.BundleFile;
import org.eclipse.osgi.baseadaptor.loader.BaseClassLoader;
import org.eclipse.osgi.baseadaptor.loader.ClasspathEntry;
import org.eclipse.osgi.baseadaptor.loader.ClasspathManager;
import org.eclipse.osgi.framework.adaptor.BundleData;
import org.eclipse.osgi.framework.adaptor.BundleProtectionDomain;
import org.eclipse.osgi.framework.adaptor.ClassLoaderDelegate;
import org.osgi.framework.Bundle;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
class NetigsoBaseLoader extends ClassLoader implements BaseClassLoader {
    private final BundleProtectionDomain bpd;
    private final ClassLoaderDelegate delegate;
    private final BaseData bd;

    public NetigsoBaseLoader(
        ClassLoader parent, ClassLoaderDelegate delegate, 
        BundleProtectionDomain bpd, BaseData bd
    ) {
        super(parent);
        this.delegate = delegate;
        this.bpd = bpd;
        this.bd = bd;
    }

    @Override
    public ProtectionDomain getDomain() {
        return bpd;
    }

    @Override
    public ClasspathEntry createClassPathEntry(BundleFile bf, ProtectionDomain pd) {
        return null;
    }

    @Override
    public Class defineClass(String string, byte[] bytes, ClasspathEntry ce, BundleEntry be) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Class publicFindLoaded(String name) {
        return super.findLoadedClass(name);
    }

    @Override
    public Object publicGetPackage(String name) {
        return super.getPackage(name);
    }

    @Override
    public Object publicDefinePackage(String s1, String s2, String s3, String s4, String s5, String s6, String s7, URL url) {
        return super.definePackage(s1, s2, s3, s4, s5, s6, s7, url);
    }

    @Override
    public ClasspathManager getClasspathManager() {
        return new ClasspathManager(bd, null, this) {

            @Override
            public BundleEntry findLocalEntry(String path, int classPathIndex) {
                if (classPathIndex > 0) {
                    return null;
                }
                URL u = delegate.findResource(path);
                return u == null ? null : new ModuleEntry(u, path);
            }
            
        };
    }

    @Override
    public void initialize() {
    }

    @Override
    public URL findLocalResource(String name) {
        return null;
        /*
        ProxyClassLoader pcl = (ProxyClassLoader)getParent();
        return pcl.findResource(name);
         *
         */
    }

    @Override
    public Enumeration<URL> findLocalResources(String name) {
        return null;
        /*
        ProxyClassLoader pcl = (ProxyClassLoader)getParent();
        try {
        return pcl.findResources(name);
        } catch (IOException ex) {
        return Enumerations.empty();
        }
         */
    }

    @Override
    protected URL findResource(String name) {
        return findLocalResource(name);
    }

    @Override
    protected Enumeration<URL> findResources(String name) throws IOException {
        return findLocalResources(name);
    }

    @Override
    public Class findLocalClass(String name) throws ClassNotFoundException {
        return getParent().loadClass(name);
    }

    @Override
    public void close() {
    }

    @Override
    public void attachFragment(BundleData bd, ProtectionDomain pd, String[] strings) {
    }

    @Override
    public ClassLoaderDelegate getDelegate() {
        return delegate;
    }

    @Override
    public Bundle getBundle() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<URL> findEntries(String string, String string1, int i) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Collection<String> listResources(String string, String string1, int i) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Collection<String> listLocalResources(String string, String string1, int i) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
