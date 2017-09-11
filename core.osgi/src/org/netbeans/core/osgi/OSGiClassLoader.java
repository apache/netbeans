/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.core.osgi;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import org.openide.util.Enumerations;
import org.openide.util.NbCollections;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * Delegates to all loaded bundles, or one bundle only.
 */
class OSGiClassLoader extends ClassLoader {

    private final BundleContext context;
    private final Set<? extends Bundle> loadedBundles;
    private final Bundle bundle;

    public OSGiClassLoader(BundleContext context, Set<? extends Bundle> loadedBundles) {
        super(ClassLoader.getSystemClassLoader().getParent());
        this.context = context;
        this.loadedBundles = loadedBundles;
        bundle = null;
    }

    public OSGiClassLoader(Bundle bundle) {
        super(ClassLoader.getSystemClassLoader().getParent());
        context = null;
        loadedBundles = null;
        this.bundle = bundle;
    }

    private Iterable<Bundle> bundles() {
        if (context != null) {
            Bundle[] bundles;
            try {
                bundles = context.getBundles();
            } catch (IllegalStateException x) {
                // Thrown sometimes by Felix during shutdown. Not clear how to avoid this.
                return Collections.emptySet();
            }
            // Sort framework last so since in Felix 4 its loadClass will search app classpath, causing test failures.
            // (Tried to disable this using various framework config properties without success.)
            return NbCollections.iterable(Enumerations.concat(Enumerations.filter(Enumerations.array(bundles), new Enumerations.Processor<Bundle,Bundle>() {
                public @Override Bundle process(Bundle b, Collection<Bundle> _) {
                    if (b.getBundleId() == 0) {
                        return null;
                    }
                    if (b.getState() == Bundle.INSTALLED) {
                        return null;
                    }
                    if (!loadedBundles.contains(b)) {
                        return null;
                    }
                    return b;
                }
            }), Enumerations.singleton(context.getBundle(0))));
        } else {
            return Collections.singleton(bundle);
        }
    }

    protected @Override Class<?> findClass(String name) throws ClassNotFoundException {
        for (Bundle b : bundles()) {
            try {
                return b.loadClass(name);
            } catch (ClassNotFoundException x) {
                // normal, try next one
            }
        }
        return super.findClass(name);
    }

    protected @Override URL findResource(String name) {
        for (Bundle b : bundles()) {
            URL resource = b.getResource(name);
            if (resource != null) {
                return resource;
            }
        }
        return super.findResource(name);
    }

    protected @Override Enumeration<URL> findResources(String name) throws IOException {
        List<Enumeration<URL>> resourcess = new ArrayList<Enumeration<URL>>();
        for (Bundle b : bundles()) {
            Enumeration<?> resourcesRaw = b.getResources(name);
            if (resourcesRaw == null) {
                // Oddly, this is permitted.
                continue;
            }
            Enumeration<URL> resources = NbCollections.checkedEnumerationByFilter(resourcesRaw, URL.class, true);
            if (resources != null) {
                resourcess.add(resources);
            }
        }
        return Enumerations.concat(Collections.enumeration(resourcess));
    }

    public @Override String toString() {
        if (context != null) {
            return "OSGiClassLoader[all bundles]"; // NOI18N
        } else {
            return "OSGiClassLoader[" + bundle.getSymbolicName() + "]"; // NOI18N
        }
    }

}
