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
