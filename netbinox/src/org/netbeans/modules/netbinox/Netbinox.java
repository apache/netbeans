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
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import org.eclipse.osgi.framework.internal.core.FrameworkProperties;
import org.eclipse.osgi.launch.Equinox;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
class Netbinox extends Equinox {
    private final String installArea;

    public Netbinox(Map configuration) {
        super(configuration);
        Object ia = configuration.get("osgi.install.area"); // NOI18N
        if (ia instanceof String) {
            installArea = (String)ia;
        } else {
            installArea = null;
        }
    }
    
    @Override
    public void init() throws BundleException {
        super.init();
        if (Boolean.getBoolean("osgi.framework.useSystemProperties")) {
            Properties prev = FrameworkProperties.getProperties();
            try {
                Field f = FrameworkProperties.class.getDeclaredField("properties"); // NOI18N
                f.setAccessible(true);
                f.set(null, null);
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
            Properties newP = FrameworkProperties.getProperties();
            for (Map.Entry en : prev.entrySet()) {
                if (en.getKey() instanceof String && en.getValue() instanceof String) {
                    newP.setProperty((String)en.getKey(), (String)en.getValue());
                }
            }
            assert System.getProperties() == FrameworkProperties.getProperties();
        }
    }
    
    @Override
    public BundleContext getBundleContext() {
        return new Context(super.getBundleContext(), installArea);
    }
    
    private static final class Context implements BundleContext {
        private final BundleContext delegate;
        private final String installArea;

        public Context(BundleContext delegate, String installArea) {
            this.delegate = delegate;
            this.installArea = installArea;
        }
        
        public boolean ungetService(ServiceReference sr) {
            return delegate.ungetService(sr);
        }

        public void removeServiceListener(ServiceListener sl) {
            delegate.removeServiceListener(sl);
        }

        public void removeFrameworkListener(FrameworkListener fl) {
            delegate.removeFrameworkListener(fl);
        }

        public void removeBundleListener(BundleListener bl) {
            delegate.removeBundleListener(bl);
        }

        public ServiceRegistration registerService(String string, Object o, Dictionary dctnr) {
            return delegate.registerService(string, o, dctnr);
        }

        public ServiceRegistration registerService(String[] strings, Object o, Dictionary dctnr) {
            return delegate.registerService(strings, o, dctnr);
        }

        public Bundle installBundle(String string) throws BundleException {
            return installBundle(string, null);
        }

        @Override
        public Bundle installBundle(String url, InputStream in) throws BundleException {
            final String pref = "reference:";
            if (url.startsWith(pref)) {
                // workaround for problems with space in path
                url = url.replaceAll("%20", " ");
                String filePart = url.substring(pref.length());
                if (installArea != null && filePart.startsWith(installArea)) {
                    String relPath = filePart.substring(installArea.length());
                    if (relPath.startsWith("/")) { // NOI18N
                        relPath = relPath.substring(1);
                    }
                    url = pref + "file:" + relPath;
                    NetbinoxFactory.LOG.log(Level.FINE, "Converted to relative {0}", url);
                } else {
                    NetbinoxFactory.LOG.log(Level.FINE, "Kept absolute {0}", url);
                }
            }
            return delegate.installBundle(url, in);
        }
        
        @Override
        public Collection getServiceReferences(Class type, String string) throws InvalidSyntaxException {
            return delegate.getServiceReferences(type, string);
        }
        
        @Override
        public ServiceReference getServiceReference(Class type) {
            return delegate.getServiceReference(type);
        }

        @Override
        public ServiceRegistration registerService(Class type, Object s, Dictionary dctnr) {
            return delegate.registerService(type, s, dctnr);
        }

        @Override
        public ServiceReference[] getServiceReferences(String string, String string1) throws InvalidSyntaxException {
            return delegate.getServiceReferences(string, string1);
        }

        public ServiceReference getServiceReference(String string) {
            return delegate.getServiceReference(string);
        }

        public Object getService(ServiceReference sr) {
            return delegate.getService(sr);
        }

        public String getProperty(String string) {
            return delegate.getProperty(string);
        }

        public File getDataFile(String string) {
            return delegate.getDataFile(string);
        }

        public Bundle[] getBundles() {
            return delegate.getBundles();
        }

        public Bundle getBundle(long l) {
            return delegate.getBundle(l);
        }

        public Bundle getBundle() {
            return delegate.getBundle();
        }
        
        public Bundle getBundle(String s) {
            return delegate.getBundle(s);
        }

        public ServiceReference[] getAllServiceReferences(String string, String string1) throws InvalidSyntaxException {
            return delegate.getAllServiceReferences(string, string1);
        }

        public Filter createFilter(String string) throws InvalidSyntaxException {
            return delegate.createFilter(string);
        }

        public void addServiceListener(ServiceListener sl) {
            delegate.addServiceListener(sl);
        }

        public void addServiceListener(ServiceListener sl, String string) throws InvalidSyntaxException {
            delegate.addServiceListener(sl, string);
        }

        public void addFrameworkListener(FrameworkListener fl) {
            delegate.addFrameworkListener(fl);
        }

        public void addBundleListener(BundleListener bl) {
            delegate.addBundleListener(bl);
        }
    } // end of Context
}
