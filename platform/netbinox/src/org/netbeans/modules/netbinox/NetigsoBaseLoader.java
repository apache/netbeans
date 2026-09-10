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
package org.netbeans.modules.netbinox;

import java.io.IOException;
import java.net.URL;
import java.security.ProtectionDomain;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import org.eclipse.osgi.internal.loader.classpath.ClasspathManager;
import org.eclipse.osgi.storage.BundleInfo;
import org.osgi.framework.Bundle;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
class NetigsoBaseLoader extends ClassLoader {
    private final ProtectionDomain bpd;
    // private final ClassLoaderDelegate delegate;
    private final BundleInfo bd;

    public NetigsoBaseLoader(
        ClassLoader parent, ClassLoader delegate,
        ProtectionDomain bpd, BundleInfo bd
    ) {
        super(parent);
        // this.delegate = delegate;
        this.bpd = bpd;
        this.bd = bd;
    }

    public ProtectionDomain getDomain() {
        return bpd;
    }

    /*
    public ClasspathEntry createClassPathEntry(BundleFile bf, ProtectionDomain pd) {
        return null;
    }

    public Class defineClass(String string, byte[] bytes, ClasspathEntry ce, BundleEntry be) {
        throw new UnsupportedOperationException();
    }
    */

    public Class publicFindLoaded(String name) {
        return super.findLoadedClass(name);
    }

    public Object publicGetPackage(String name) {
        return super.getPackage(name);
    }

    public Object publicDefinePackage(String s1, String s2, String s3, String s4, String s5, String s6, String s7, URL url) {
        return super.definePackage(s1, s2, s3, s4, s5, s6, s7, url);
    }

    public ClasspathManager getClasspathManager() {
        throw new UnsupportedOperationException();
        /*
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
        */
    }

    public void initialize() {
    }

    public URL findLocalResource(String name) {
        return null;
        /*
        ProxyClassLoader pcl = (ProxyClassLoader)getParent();
        return pcl.findResource(name);
         *
         */
    }

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

    public Class findLocalClass(String name) throws ClassNotFoundException {
        return getParent().loadClass(name);
    }

    public void close() {
    }

    /*
    public void attachFragment(BundleData bd, ProtectionDomain pd, String[] strings) {
    }

    public ClassLoaderDelegate getDelegate() {
        return delegate;
    }
    */

    public Bundle getBundle() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public List<URL> findEntries(String string, String string1, int i) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Collection<String> listResources(String string, String string1, int i) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Collection<String> listLocalResources(String string, String string1, int i) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
