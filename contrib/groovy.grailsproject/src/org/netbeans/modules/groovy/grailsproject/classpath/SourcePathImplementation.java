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
package org.netbeans.modules.groovy.grailsproject.classpath;

import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.URL;
import org.netbeans.modules.groovy.grails.api.GrailsProjectConfig;
import org.netbeans.modules.groovy.grailsproject.GrailsProject;
import org.netbeans.modules.groovy.grailsproject.config.BuildConfig;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.util.WeakListeners;

final class SourcePathImplementation implements ClassPathImplementation, PropertyChangeListener {

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    private final SourceRoots sourceRoots;

    private List<PathResourceImplementation> resources;

    /**
     * Construct the implementation.
     * @param sourceRoots used to get the roots information and events
     */
    private SourcePathImplementation(SourceRoots sourceRoots) {
        assert sourceRoots != null;
        this.sourceRoots = sourceRoots;
    }

    public static SourcePathImplementation forProject(GrailsProject project, SourceRoots sourceRoots) {
        GrailsProjectConfig config = GrailsProjectConfig.forProject(project);
        SourcePathImplementation impl = new SourcePathImplementation(sourceRoots);

        BuildConfig build = ((GrailsProject) config.getProject()).getBuildConfig();
        build.addPropertyChangeListener(WeakListeners.propertyChange(impl, config));

        return impl;
    }

    public List<PathResourceImplementation> getResources() {
        synchronized (this) {
            if (this.resources != null) {
                return this.resources;
            }
        }
        List<URL> roots = this.sourceRoots.getRootURLs();
        synchronized (this) {
            if (this.resources == null) {
                List<PathResourceImplementation> result = new ArrayList<PathResourceImplementation>(roots.size());
                for (URL url : roots) {
                    PathResourceImplementation res = ClassPathSupport.createResource(url);
                    result.add(res);
                }
                this.resources = Collections.unmodifiableList(result);
            }
            return this.resources;
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (BuildConfig.BUILD_CONFIG_PLUGINS.equals(evt.getPropertyName())) {
            synchronized (this) {
                this.resources = null;
            }
            this.support.firePropertyChange(ClassPathImplementation.PROP_RESOURCES, null, null);
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

}
