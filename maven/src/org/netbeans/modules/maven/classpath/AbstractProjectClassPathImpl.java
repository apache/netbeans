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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.classpath;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.FilteringPathResourceImplementation;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/**
 *
 * @author mkleint
 */
public abstract class AbstractProjectClassPathImpl implements ClassPathImplementation {
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    private List<PathResourceImplementation> resources;
    private NbMavenProjectImpl project;
    
    protected AbstractProjectClassPathImpl(NbMavenProjectImpl proj) {
        project = proj;
        //TODO make weak or remove the listeners as well??
        NbMavenProject.addPropertyChangeListener(proj, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                //explicitly listing both RESOURCE and PROJECT properties, it's unclear if both are required but since some other places call addWatchedPath but don't listen it's likely required
                if (NbMavenProject.PROP_RESOURCE.equals(evt.getPropertyName()) || NbMavenProject.PROP_PROJECT.equals(evt.getPropertyName())) {
                    if (project.getProjectWatcher().isUnloadable()) {
                        return; //let's just continue with the old value, stripping classpath for broken project and re-creating it later serves no greater good.
                    }
                    List<PathResourceImplementation> newValues = getPath();
                    List<PathResourceImplementation> oldvalue;
                    boolean hasChanged;
                    synchronized (AbstractProjectClassPathImpl.this) {
                        oldvalue = resources;
                        hasChanged = hasChanged(oldvalue, newValues);
//                        System.out.println("checking=" + AbstractProjectClassPathImpl.this.getClass());
                        if (hasChanged) {
                            resources = newValues;
//                            System.out.println("old=" + oldvalue);
//                            System.out.println("new=" + newValues);
//                            System.out.println("firing change=" + AbstractProjectClassPathImpl.this.getClass());
                        }
                    }
                    if (hasChanged) {
                        support.firePropertyChange(ClassPathImplementation.PROP_RESOURCES, oldvalue, newValues);
                    }
                }
            }
        });
    }
    
    protected NbMavenProjectImpl getProject() {
        return project;
    }
    
    private boolean hasChanged(List<PathResourceImplementation> oldValues,
                               List<PathResourceImplementation> newValues) {
        if (oldValues == null) {
            return (newValues != null);
        }
        Iterator<PathResourceImplementation> it = oldValues.iterator();
        ArrayList<PathResourceImplementation> nl = new ArrayList<PathResourceImplementation>();
        nl.addAll(newValues);
        while (it.hasNext()) {
            PathResourceImplementation res = it.next();
            URL oldUrl = res.getRoots()[0];
            boolean found = false;
            if (nl.isEmpty()) {
                return true;
            }
            Iterator<PathResourceImplementation> inner = nl.iterator();
            while (inner.hasNext()) {
                PathResourceImplementation res2 = inner.next();
                URL newUrl = res2.getRoots()[0];
                if (newUrl.equals(oldUrl)) {
                    inner.remove();
                    found = true;
                    break;
                }
            }
            if (!found) {
                return true;
            }
        }
        if (!nl.isEmpty()) {
            return true;
        }
        return false;
    }
    
    protected final NbMavenProjectImpl getMavenProject() {
        return project;
    }

    protected final void firePropertyChange(String propName, Object oldValue, Object newValue) {
        support.firePropertyChange(propName, oldValue, newValue);
    }
    
    @Override
    public synchronized List<PathResourceImplementation> getResources() {
        if (resources == null) {
            resources = this.getPath();
        }
        return resources;
    }
    
    
    abstract URI[] createPath();
    
    private List<PathResourceImplementation> getPath() {
        List<PathResourceImplementation> base = getPath(createPath(), new Includer() {
            @Override public boolean includes(URL root, String resource) {
                return AbstractProjectClassPathImpl.this.includes(root, resource);
            }
        });
        return Collections.<PathResourceImplementation>unmodifiableList(base);
    }

    protected boolean includes(URL root, String resource) {
        return true;
    }

    public interface Includer {
        boolean includes(URL root, String resource);
    }
    
    public static  List<PathResourceImplementation> getPath(URI[] pieces, final Includer includer) {
        List<PathResourceImplementation> result = new ArrayList<PathResourceImplementation>();
        for (int i = 0; i < pieces.length; i++) {
            try {
                // XXX would be cleaner to take a File[] if that is what these all are anyway!
                final URL entry = FileUtil.urlForArchiveOrDir(Utilities.toFile(pieces[i]));
                if (entry != null) {
                    result.add(new FilteringPathResourceImplementation() {
                        @Override public boolean includes(URL root, String resource) {
                            return includer != null ? includer.includes(root, resource) : true;
                        }
                        @Override public URL[] getRoots() {
                            return new URL[] {entry};
                        }
                        @Override public ClassPathImplementation getContent() {
                            return null;
                        }
                        @Override public void addPropertyChangeListener(PropertyChangeListener listener) {}
                        @Override public void removePropertyChangeListener(PropertyChangeListener listener) {}
                    });
                }
            } catch (IllegalArgumentException exc) {
                Logger.getLogger(AbstractProjectClassPathImpl.class.getName()).log(Level.INFO, "Cannot use uri " + pieces[i] + " for classpath", exc);
            }
        }
        return result;
    }
    
    @Override
    public void addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        synchronized (support) {
            support.addPropertyChangeListener(propertyChangeListener);
        }
    }
    
    @Override
    public void removePropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        synchronized (support) {
            support.removePropertyChangeListener(propertyChangeListener);
        }
    }

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override public final boolean equals(Object obj) {
        return getClass().isInstance(obj) && project.equals(((AbstractProjectClassPathImpl) obj).project);
    }

    @Override public final int hashCode() {
        return project.hashCode() ^ getClass().hashCode();
    }

}
