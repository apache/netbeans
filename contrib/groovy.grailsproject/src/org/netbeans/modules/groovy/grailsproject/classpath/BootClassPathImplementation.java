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
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.classpath.ClassPath;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.groovy.grails.api.GrailsProjectConfig;
import org.netbeans.modules.groovy.grails.api.GrailsPlatform;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.openide.util.WeakListeners;

final class BootClassPathImplementation implements ClassPathImplementation {

    private static final Logger LOGGER = Logger.getLogger(BootClassPathImplementation.class.getName());

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    private final ProjectConfigListener projectConfigListener = new ProjectConfigListener();

    private final GrailsProjectConfig config;

    private List<PathResourceImplementation> resourcesCache;

    private long eventId;

    private BootClassPathImplementation(GrailsProjectConfig config) {
        this.config = config;
    }

    public static BootClassPathImplementation forProject(Project project) {
        GrailsProjectConfig config = GrailsProjectConfig.forProject(project);
        BootClassPathImplementation impl = new BootClassPathImplementation(config);

        config.addPropertyChangeListener(WeakListeners.propertyChange(impl.projectConfigListener, config));
        return impl;
    }

    public List<PathResourceImplementation> getResources() {
        long currentId;
        synchronized (this) {
            if (resourcesCache != null) {
                return resourcesCache;
            }
            currentId = eventId;
        }

        JavaPlatform jp = config.getJavaPlatform();
        final List<PathResourceImplementation> result = new ArrayList<PathResourceImplementation>();
        if (jp != null) {
            //TODO: May also listen on CP, but from Platform it should be fixed.
            final ClassPath cp = jp.getBootstrapLibraries();
            assert cp != null : jp;
            for (ClassPath.Entry entry : cp.entries()) {
                result.add(ClassPathSupport.createResource(entry.getURL()));
            }
        }

        GrailsPlatform gp = config.getGrailsPlatform();
        if (gp != null) {
            final ClassPath cp = gp.getClassPath();
            assert cp != null : gp;
            for (ClassPath.Entry entry : cp.entries()) {
                result.add(ClassPathSupport.createResource(entry.getURL()));
            }
        }

        synchronized (this) {
            if (currentId == eventId) {
                if (resourcesCache == null) {
                    resourcesCache = Collections.unmodifiableList(result);
                }
                return resourcesCache;
            }
            return Collections.unmodifiableList (result);
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.support.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.support.removePropertyChangeListener(listener);
    }

    private class ProjectConfigListener implements PropertyChangeListener {

        public void propertyChange(PropertyChangeEvent evt) {
            if (GrailsProjectConfig.GRAILS_JAVA_PLATFORM_PROPERTY.equals(evt.getPropertyName())
                    || GrailsProjectConfig.GRAILS_PLATFORM_PROPERTY.equals(evt.getPropertyName())) {

                LOGGER.log(Level.FINE, "Boot classpath changed due to change in {0}", evt.getPropertyName());

                synchronized (BootClassPathImplementation.this) {
                    resourcesCache = null;
                    eventId++;
                }
                support.firePropertyChange(ClassPathImplementation.PROP_RESOURCES, null, null);
            }
        }
    }
}
