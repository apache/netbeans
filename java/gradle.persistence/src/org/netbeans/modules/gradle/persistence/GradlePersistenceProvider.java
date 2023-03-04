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

package org.netbeans.modules.gradle.persistence;

import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.java.api.GradleJavaProject;
import org.netbeans.modules.gradle.spi.WatchedResourceProvider;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Collection;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScope;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScopes;
import org.netbeans.modules.j2ee.persistence.spi.PersistenceLocationProvider;
import org.netbeans.modules.j2ee.persistence.spi.PersistenceScopeProvider;
import org.netbeans.modules.j2ee.persistence.spi.PersistenceScopesProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.WeakListeners;

/**
 *
 * @author Daniel Mohni
 */
@ProjectServiceProvider(
        service = {
            PersistenceLocationProvider.class,
            PersistenceScopeProvider.class,
            PersistenceScopesProvider.class,
            WatchedResourceProvider.class
        },
        projectType = NbGradleProject.GRADLE_PLUGIN_TYPE + "/java-base"
)
public class GradlePersistenceProvider implements PersistenceLocationProvider,
        PersistenceScopeProvider, PersistenceScopesProvider, WatchedResourceProvider {

    public static final String PROP_PERSISTENCE = "GradlePersistence"; //NOI18N

    private PersistenceLocationProviderImpl locProvider = null;
    private PersistenceScopesProviderImpl scopesProvider = null;
    private PersistenceScopeProviderImpl scopeProvider = null;

    private final PropertyChangeSupport propChangeSupport = new PropertyChangeSupport(this);

    private final ResourceListener res = new ResourceListener();

    final Project project;

    /**
     * Creates a new instance of GradlePersistenceProvider
     */
    public GradlePersistenceProvider(Project proj, Lookup lkp) {
        this.project = proj;
        NbGradleProject watcher = lkp.lookup(NbGradleProject.class);
        locProvider = new PersistenceLocationProviderImpl(proj);
        scopeProvider = new PersistenceScopeProviderImpl(locProvider, proj);
        scopesProvider = new PersistenceScopesProviderImpl(scopeProvider);

        propChangeSupport.addPropertyChangeListener(locProvider);
        propChangeSupport.addPropertyChangeListener(scopesProvider);

        //TODO add FileChangeListener on persistence.xml
        watcher.addPropertyChangeListener(WeakListeners.propertyChange(res, watcher));
    }

    /*
     * PersistenceLocationProvider methods
     */
    @Override
    public FileObject getLocation() {
        return locProvider.getLocation();
    }

    @Override
    public FileObject createLocation() throws IOException {
        return locProvider.createLocation();
    }

    /*
     * PersistenceScopeProvider methodes
     */
    @Override
    public PersistenceScope findPersistenceScope(FileObject fileObject) {
        return scopeProvider.findPersistenceScope(fileObject);
    }

    /*
     * PersistenceScopesProvider methodes
     */
    @Override
    public PersistenceScopes getPersistenceScopes() {
        return scopesProvider.getPersistenceScopes();
    }

    @Override
    public Set<File> getWatchedResources() {
        GradleJavaProject gjp = GradleJavaProject.get(project);
        if (gjp != null && gjp.getMainSourceSet() != null) {
            Collection<File> roots = gjp.getMainSourceSet().getAllDirs();
            Set<File> ret = new LinkedHashSet<>();
            for (File root : roots) {
                ret.add(new File(root, PersistenceLocationProviderImpl.REL_PERSISTENCE));
            }
            return ret;
        } else {
            return Collections.<File>emptySet();
        }

    }

    //TODO rewrite..
    private class ResourceListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent event) {
            if (NbGradleProject.PROP_RESOURCES.equals(event.getPropertyName())) {
                URI newval = (URI) event.getNewValue();
                if (newval.getPath().endsWith(PersistenceLocationProviderImpl.REL_PERSISTENCE)) {
                    propChangeSupport.firePropertyChange(PROP_PERSISTENCE, null, null);

                }
            }
        }
    }
}
