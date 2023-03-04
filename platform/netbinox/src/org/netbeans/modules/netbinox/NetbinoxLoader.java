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
import java.net.URI;
import java.net.URISyntaxException;
import java.security.ProtectionDomain;
import java.util.logging.Level;
import org.eclipse.osgi.baseadaptor.BaseData;
import org.eclipse.osgi.baseadaptor.bundlefile.BundleEntry;
import org.eclipse.osgi.baseadaptor.bundlefile.BundleFile;
import org.eclipse.osgi.baseadaptor.loader.BaseClassLoader;
import org.eclipse.osgi.baseadaptor.loader.ClasspathEntry;
import org.eclipse.osgi.baseadaptor.loader.ClasspathManager;
import org.eclipse.osgi.framework.adaptor.ClassLoaderDelegate;
import org.eclipse.osgi.internal.baseadaptor.DefaultClassLoader;
import org.osgi.framework.FrameworkEvent;

/** Classloader that eliminates some unnecessary disk touches.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class NetbinoxLoader extends DefaultClassLoader {
    static {
        registerAsParallelCapable();
    }
    
    public NetbinoxLoader(ClassLoader parent, ClassLoaderDelegate delegate, ProtectionDomain domain, BaseData bd, String[] classpath) {
        super(parent, delegate, domain, bd, classpath);
        this.manager = new NoTouchCPM(bd, classpath, this);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        assert !Thread.holdsLock(this) : 
            "Classloading while holding classloader lock!";
        return super.loadClass(name, resolve);
    }

    @Override
    public String toString() {
        return "NetbinoxLoader delegating to " + delegate;
    }
    
    private static class NoTouchCPM extends ClasspathManager {
        public NoTouchCPM(BaseData data, String[] classpath, BaseClassLoader classloader) {
            super(data, classpath, classloader);
        }

        @Override
        public ClasspathEntry getClasspath(String cp, BaseData sourcedata, ProtectionDomain sourcedomain) {
            BundleFile bundlefile = null;
            File file;
            BundleEntry cpEntry = sourcedata.getBundleFile().getEntry(cp);
            // check for internal library directories in a bundle jar file
            if (cpEntry != null && cpEntry.getName().endsWith("/")) //$NON-NLS-1$
            {
                bundlefile = createBundleFile(cp, sourcedata);
            } // check for internal library jars
            else if ((file = sourcedata.getBundleFile().getFile(cp, false)) != null) {
                bundlefile = createBundleFile(file, sourcedata);
            }
            if (bundlefile != null) {
                return createClassPathEntry(bundlefile, sourcedomain, sourcedata);
            }
            return null;
        }

        @Override
        public ClasspathEntry getExternalClassPath(String cp, BaseData sourcedata, ProtectionDomain sourcedomain) {
            File file;
            if (cp.startsWith("file:")) { // NOI18N
                try {
                    file = org.openide.util.Utilities.toFile(new URI(cp));
                } catch (URISyntaxException ex) {
                    NetbinoxFactory.LOG.log(Level.SEVERE, "Cannot convert to file: " + cp, ex); // NOI18N
                    return null;
                }
            } else {
                file = new File(cp);
            }
            if (!file.isAbsolute() && !cp.startsWith("file:")) { // NOI18N
                return null;
            }
            BundleFile bundlefile = createBundleFile(file, sourcedata);
            if (bundlefile != null) {
                return createClassPathEntry(bundlefile, sourcedomain, sourcedata);
            }
            return null;
        }

        private static BundleFile createBundleFile(Object content, BaseData sourcedata) {
            try {
                return sourcedata.getAdaptor().createBundleFile(content, sourcedata);
            } catch (IOException e) {
                sourcedata.getAdaptor().getEventPublisher().publishFrameworkEvent(FrameworkEvent.ERROR, sourcedata.getBundle(), e);
            }
            return null;
        }
        private ClasspathEntry createClassPathEntry(BundleFile bundlefile, ProtectionDomain cpDomain, final BaseData data) {
            return new ClasspathEntry(bundlefile, createProtectionDomain(bundlefile, cpDomain)) {
                @Override
                public BaseData getBaseData() {
                    return data;
                }
            };
        }
    } // end of NoTouchCPM
}
