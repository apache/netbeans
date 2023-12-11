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
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
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

    private static final Logger LOGGER = Logger.getLogger(AbstractProjectClassPathImpl.class.getName());

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    private List<PathResourceImplementation> resources;
    private NbMavenProjectImpl project;
    
    protected AbstractProjectClassPathImpl(NbMavenProjectImpl proj) {
        project = proj;
        LOGGER.log(Level.FINER, "Creating {0} for project {1}, with original {2}", new Object[] { getClass(), proj, proj.getOriginalMavenProjectOrNull() });
        //TODO make weak or remove the listeners as well??
        NbMavenProject.addPropertyChangeListener(proj, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                //explicitly listing both RESOURCE and PROJECT properties, it's unclear if both are required but since some other places call addWatchedPath but don't listen it's likely required
                if (NbMavenProject.PROP_RESOURCE.equals(evt.getPropertyName()) || NbMavenProject.PROP_PROJECT.equals(evt.getPropertyName())) {
                    MavenProject mp = project.getOriginalMavenProjectOrNull();
                    LOGGER.log(Level.FINE, "{0} got change {1} from project: {2} with maven project {3}", new Object[] { getClass(), evt.getPropertyName(), project,
                        System.identityHashCode(mp == null ? this : mp) });
                    if (project.getProjectWatcher().isUnloadable()) {
                        LOGGER.log(Level.FINER, "{0} is not loadable, exiting", project);
                        return; //let's just continue with the old value, stripping classpath for broken project and re-creating it later serves no greater good.
                    }
                    List<PathResourceImplementation> newValues = getPath();
                    List<PathResourceImplementation> oldvalue;
                    boolean hasChanged;
                    synchronized (AbstractProjectClassPathImpl.this) {
                        oldvalue = resources;
                        hasChanged = hasChanged(oldvalue, newValues);
                        LOGGER.log(Level.FINER, "{0}: {1} classpath is: {2}, {3} entries", new Object[] {
                            getClass(), project, newValues, newValues.size()
                        });
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
        ArrayList<PathResourceImplementation> nl = new ArrayList<>(newValues);
        for (PathResourceImplementation res : oldValues) {
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
        return Collections.unmodifiableList(getPath(createPath(), AbstractProjectClassPathImpl.this::includes));
    }

    protected boolean includes(URL root, String resource) {
        return true;
    }

    public interface Includer {
        boolean includes(URL root, String resource);
    }
    
    public static List<PathResourceImplementation> getPath(URI[] pieces, final Includer includer) {
        List<PathResourceImplementation> result = new ArrayList<>();
        for (URI piece : pieces) {
            try {
                // XXX would be cleaner to take a File[] if that is what these all are anyway!
                final URL entry = FileUtil.urlForArchiveOrDir(Utilities.toFile(piece));
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
                Logger.getLogger(AbstractProjectClassPathImpl.class.getName()).log(Level.INFO, "Cannot use uri " + piece + " for classpath", exc);
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

    /**
     * Like {@link Artifact#getFile} but when a timestamped snapshot is locally downloaded, uses that instead.
     */
    protected static File getFile(Artifact art) {
        File f = art.getFile();
        if (f != null) {
            String baseVersion = art.getBaseVersion();
            if (art.isSnapshot() && !art.getVersion().equals(baseVersion)) {
                String name = f.getName();
                int endOfVersion = name.lastIndexOf(/* DefaultRepositoryLayout.GROUP_SEPARATOR */'.');
                String classifier = art.getClassifier();
                if (classifier != null) {
                    endOfVersion -= classifier.length() + /* "-" */1;
                }
                if (endOfVersion > 0 && name.substring(0, endOfVersion).endsWith(baseVersion)) {
                    File f2 = new File(f.getParentFile(), name.substring(0, endOfVersion - baseVersion.length()) + art.getVersion() + name.substring(endOfVersion));
                    if (f2.isFile()) {
                        LOGGER.log(Level.FINE, "swapped {0} → {1}", new Object[] {f, f2});
                        return f2;
                    } else {
                        LOGGER.log(Level.FINE, "did not find predicted {0}", f2);
                    }
                } else {
                    LOGGER.log(Level.FINE, "failed to match file pattern for {0} from {1}", new Object[] {f, art});
                }
            } else {
                LOGGER.log(Level.FINEST, "not touching {0}", f);
            }
        } else {
            LOGGER.log(Level.FINER, "no file for {0}", art);
        }
        return f;
    }

}
