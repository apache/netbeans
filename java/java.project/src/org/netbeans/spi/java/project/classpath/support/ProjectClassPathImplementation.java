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

package org.netbeans.spi.java.project.classpath.support;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.BaseUtilities;
import org.openide.util.WeakListeners;

/** 
 * Implementation of a single classpath that is derived from list of Ant properties.
 */
final class ProjectClassPathImplementation implements ClassPathImplementation, PropertyChangeListener, Runnable {
    
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    private final File projectFolder;
    private List<PathResourceImplementation> resources;
    private final PropertyEvaluator evaluator;
    private AtomicBoolean dirty = new AtomicBoolean ();
    private final List<String> propertyNames;

    /**
     * Construct the implementation.
     * @param projectFolder the folder containing the project, used to resolve relative paths
     * @param propertyNames the names of an Ant properties which will supply the classpath
     * @param evaluator a property evaluator used to find the value of the classpath
     */
    public ProjectClassPathImplementation(File projectFolder, String[] propertyNames, PropertyEvaluator evaluator) {
        assert projectFolder != null && propertyNames != null && evaluator != null;
        this.projectFolder = projectFolder;
        this.evaluator = evaluator;
        this.propertyNames = Arrays.asList(propertyNames);
        this.resources = this.getPath ();
        evaluator.addPropertyChangeListener(WeakListeners.propertyChange(this, evaluator));
    }

    public synchronized List<PathResourceImplementation> getResources() {
        assert this.resources != null;
        return this.resources;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener (listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener (listener);
    }


    public void propertyChange(PropertyChangeEvent evt) {        
        String prop = evt.getPropertyName();
        if (prop != null && !propertyNames.contains(evt.getPropertyName())) {
            // Not interesting to us.
            return;
        }
        // Coalesce changes; can come in fast after huge CP changes (#47910):
        if (!dirty.getAndSet(true)) {
            ProjectManager.mutex().postReadRequest(this);
        }
    }
    
    public void run() {
        dirty.set(false);
        List<PathResourceImplementation> newRoots = getPath();
        boolean fire = false;
        synchronized (this) {
            if (!this.resources.equals(newRoots)) {
                this.resources = newRoots;
                fire = true;
            }
        }
        if (fire) {
            support.firePropertyChange (PROP_RESOURCES,null,null);
        }
    }
    
    private List<PathResourceImplementation> getPath() {
        List<PathResourceImplementation> result = new ArrayList<PathResourceImplementation>();
        for (String p : propertyNames) {
            String prop = evaluator.getProperty(p);
            if (prop != null) {
                for (String piece : PropertyUtils.tokenizePath(prop)) {
                    File f = PropertyUtils.resolveFile(this.projectFolder, piece);
                    URL entry = FileUtil.urlForArchiveOrDir(f);
                    if (entry != null) {
                        try {
                            PathResourceImplementation res = null;
                            try {
                                res = ClassPathSupport.createResource(entry);
                            } catch (IllegalArgumentException e) {
                                //Try to recover from non normalized file having unbalanced path, do UNIX like normalization /../../Foo -> /Foo
                                f = BaseUtilities.toFile(BaseUtilities.normalizeURI(BaseUtilities.toURI(f)));
                                if (f != null) {
                                    String parentPattern = File.separatorChar + ".." + File.separatorChar;
                                    while (f.getAbsolutePath().startsWith(parentPattern)) {
                                        f = new File(f.getAbsolutePath().substring(parentPattern.length()-1));
                                    }
                                    res = Optional.ofNullable(FileUtil.urlForArchiveOrDir(FileUtil.normalizeFile(f)))
                                            .map(ClassPathSupport::createResource)
                                            .orElse(null);
                                }
                            }
                            if (res != null) {
                                result.add(res);
                            }
                        } catch (IllegalArgumentException iae) {
                            //Logging for issue #181155, #186213
                            String foStatus;
                            try {
                                final FileObject fo = URLMapper.findFileObject(BaseUtilities.toURI(f).toURL());
                                foStatus = fo == null ? "not exist" : fo.isValid() ? fo.isFolder() ? "valid folder" : "valid file" : "invalid";   //NOI18N
                            } catch (MalformedURLException ex) {
                                foStatus = "malformed"; //NOI18N
                            }
                            final String log = String.format("File: %s, Property value: %s, Exists: %b, FileObject: %s, Folder: %b, Size: %d, Retry: %s", //NOI18N
                                    f.getAbsolutePath(),
                                    piece,
                                    f.exists(),
                                    foStatus,
                                    f.isDirectory(),
                                    f.length(),
                                    FileUtil.urlForArchiveOrDir(f).toExternalForm());
                            throw new IllegalArgumentException(log, iae);
                        }
                    } else {
                        Logger.getLogger(ProjectClassPathImplementation.class.getName()).warning(f + " does not look like a valid archive file");   //NOI18N
                    }
                }
            }
        }
        return Collections.unmodifiableList(result);
    }

}
