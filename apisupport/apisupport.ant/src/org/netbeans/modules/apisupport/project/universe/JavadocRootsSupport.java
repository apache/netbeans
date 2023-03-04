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

package org.netbeans.modules.apisupport.project.universe;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import org.netbeans.spi.project.support.ant.PropertyUtils;

/**
 *
 * @author Richard Michalsky
 */
public final class JavadocRootsSupport implements JavadocRootsProvider {

    private URL[] javadocRoots;
    private JavadocRootsProvider delegate;
    private PropertyChangeSupport pcs;

    /**
     * Constructs <tt>JavadocRootsSupport</tt> object.
     * No property change is fired when setting initial source roots. If a delegate is provided,
     * <tt>getDefaultJavadocRoots()<tt> and <i>internal</i> calls to <tt>setJavadocRoots()</tt>
     * are passed to delegate. This allows to customize the behavior of JavadocRootsSupport.
     *
     * @param javadocRoots Initial source roots.
     * @param pcs Support to use to fire property changes. May be <tt>null</tt>.
     * @param delegate The delegate for routing calls. May be <tt>null</tt>.
     */
    public JavadocRootsSupport(URL[] javadocRoots, JavadocRootsProvider delegate) {
        if (javadocRoots == null)
            throw new NullPointerException("javadocRoots must not be null.");
        this.javadocRoots = javadocRoots;
        this.pcs = new PropertyChangeSupport(this);
        this.delegate = delegate;
    }

    public URL[] getDefaultJavadocRoots() {
        if (delegate == null)
            return null;
        return delegate.getDefaultJavadocRoots();
    }

    /**
     * Get associated Javadoc roots for this platform.
     * Each root may contain some Javadoc sets in the usual format as subdirectories,
     * where the subdirectory is named acc. to the code name base of the module it
     * is documenting (using '-' in place of '.').
     * @return a list of Javadoc root URLs (may be empty but not null)
     */
    public URL[] getJavadocRoots() {
        if (javadocRoots.length == 0) {
            URL[] defaults = getDefaultJavadocRoots();
            if (defaults != null)
                return defaults;
        }
        return javadocRoots;
    }

    private void maybeUpdateDefaultJavadoc() {
        if (javadocRoots.length == 0) {
            URL[] defaults = getDefaultJavadocRoots();
            if (defaults != null) {
                javadocRoots = defaults;
                pcs.firePropertyChange(JavadocRootsProvider.PROP_JAVADOC_ROOTS, null, null);
            }
        }
    }

    /**
     * Add given javadoc root to the current javadoc root list and save the
     * result into the global properties in the <em>userdir</em> (see {@link
     * PropertyUtils#putGlobalProperties})
     * @param root
     * @throws java.io.IOException
     * @throws java.lang.IllegalArgumentException When root already present in javadoc.
     */
    public void addJavadocRoot(URL root) throws IOException, IllegalArgumentException {
        org.openide.util.Parameters.notNull("root", root);    // NOI18N
        if (containsRoot(this, root))
            throw new IllegalArgumentException("Root '" + root + "' already present in javadoc.");    // NOI18N
        maybeUpdateDefaultJavadoc();
        URL[] newJavadocRoots = new URL[javadocRoots.length + 1];
        System.arraycopy(javadocRoots, 0, newJavadocRoots, 0, javadocRoots.length);
        newJavadocRoots[javadocRoots.length] = root;
        setJavadocRootsInternal(newJavadocRoots);
    }

    /**
     * Remove given javadoc roots from the current javadoc root list and save
     * the result into the global properties in the <em>userdir</em> (see
     * {@link PropertyUtils#putGlobalProperties})
     */
    public void removeJavadocRoots(URL[] urlsToRemove) throws IOException {
        maybeUpdateDefaultJavadoc();
        Collection<URL> newJavadocs = new ArrayList<URL>(Arrays.asList(javadocRoots));
        newJavadocs.removeAll(Arrays.asList(urlsToRemove));
        assert newJavadocs.size() + urlsToRemove.length >= javadocRoots.length :
            "Too many roots removed, one of " + Arrays.toString(urlsToRemove) + " was contained more than once";
        URL[] javadocs = new URL[newJavadocs.size()];
        setJavadocRootsInternal(newJavadocs.toArray(javadocs));
    }

    public void moveJavadocRootUp(int indexToUp) throws IOException {
        maybeUpdateDefaultJavadoc();
        if (indexToUp <= 0) {
            return; // nothing needs to be done
        }
        URL[] newJavadocRoots = new URL[javadocRoots.length];
        System.arraycopy(javadocRoots, 0, newJavadocRoots, 0, javadocRoots.length);
        newJavadocRoots[indexToUp - 1] = javadocRoots[indexToUp];
        newJavadocRoots[indexToUp] = javadocRoots[indexToUp - 1];
        setJavadocRootsInternal(newJavadocRoots);
    }

    public void moveJavadocRootDown(int indexToDown) throws IOException {
        maybeUpdateDefaultJavadoc();
        if (indexToDown >= (javadocRoots.length - 1)) {
            return; // nothing needs to be done
        }
        URL[] newJavadocRoots = new URL[javadocRoots.length];
        System.arraycopy(javadocRoots, 0, newJavadocRoots, 0, javadocRoots.length);
        newJavadocRoots[indexToDown + 1] = javadocRoots[indexToDown];
        newJavadocRoots[indexToDown] = javadocRoots[indexToDown + 1];
        setJavadocRootsInternal(newJavadocRoots);
    }

    private void setJavadocRootsInternal(URL[] roots) throws IOException {
        if (delegate != null)
            delegate.setJavadocRoots(roots);
        else
            setJavadocRoots(roots);
    }

    public void setJavadocRoots(URL[] roots) throws IOException {
        javadocRoots = roots;
        pcs.firePropertyChange(JavadocRootsProvider.PROP_JAVADOC_ROOTS, null, null);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    /**
     * Queries the provider if already contains given source root.
     * @param provider
     * @param root
     * @return <tt>true</tt> if provider already contains the root.
     */
    public static boolean containsRoot(JavadocRootsProvider provider, URL root) {
        org.openide.util.Parameters.notNull("provider", provider);    // NOI18N
        org.openide.util.Parameters.notNull("root", root);    // NOI18N
        for (URL r2 : provider.getJavadocRoots()) {
            if (root.equals(r2))
                return true;
        }
        return false;
    }
}
