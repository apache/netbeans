/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Oracle, Inc.
 */
package org.netbeans.modules.netbinox;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
import org.openide.util.Exceptions;
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
