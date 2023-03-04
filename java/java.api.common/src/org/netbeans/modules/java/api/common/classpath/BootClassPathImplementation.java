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
package org.netbeans.modules.java.api.common.classpath;

import java.beans.PropertyChangeEvent;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.api.common.util.CommonProjectUtils;
import org.netbeans.spi.java.project.support.ProjectPlatform;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.util.WeakListeners;


/**
 * Represent a boot class path. It is possible to listen to the changes of
 * {@link ClassPathImplementation#PROP_RESOURCES}.
 * @author Tomas Zezula
 */
final class BootClassPathImplementation implements ClassPathImplementation, PropertyChangeListener {

    private static final String PLATFORM_ACTIVE = "platform.active"; // NOI18N

    private final Project project;
    private final PropertyEvaluator evaluator;
    private final String platformType;
    private JavaPlatformManager platformManager;
    // name of project active platform
    private String activePlatformName;
    // active platform is valid (not broken reference)
    private boolean isActivePlatformValid;
    private List<PathResourceImplementation> resourcesCache;
    private long eventId;
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    private final ClassPath endorsedClassPath;

    BootClassPathImplementation(
            @NullAllowed final Project project,
            @NonNull final PropertyEvaluator evaluator,
            @NullAllowed final ClassPath endorsedClassPath,
            @NullAllowed final String platformType) {
        assert evaluator != null;
        this.project = project;
        this.endorsedClassPath = endorsedClassPath;
        this.evaluator = evaluator;
        this.platformType = platformType;
        evaluator.addPropertyChangeListener(WeakListeners.propertyChange(this, evaluator));
        if (endorsedClassPath != null) {
            endorsedClassPath.addPropertyChangeListener(this);
        }
    }
    
    /**
     * @see ClassPathImplementation#getResources()
     */
    @Override
    public List<PathResourceImplementation> getResources() {
        long currentId;
        synchronized (this) {
            if (resourcesCache != null) {
                return resourcesCache;
            }
            currentId = eventId;
        }

        final List<PathResourceImplementation> result = new ArrayList<>();
        if (endorsedClassPath != null) {
            for (ClassPath.Entry entry : endorsedClassPath.entries()) {
                result.add(ClassPathSupport.createResource(entry.getURL()));
            }
        }
        JavaPlatform jp = findActivePlatform();
        if (jp != null) {
            // TODO: may also listen on CP, but from Platform it should be fixed
            final ClassPath cp = jp.getBootstrapLibraries();
            assert cp != null : jp;
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
            return Collections.unmodifiableList(result);
        }
    }

    /**
     * Add {@link PropertyChangeListener}, see class description for more information.
     * @param listener a listener to add.
     */
    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    /**
     * Remove {@link PropertyChangeListener}, see class description for more information.
     * @param listener a listener to remove.
     */
    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    /**
     * @see PropertyChangeListener#propertyChange()
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() == evaluator && evt.getPropertyName().equals(PLATFORM_ACTIVE)) {
            // active platform was changed
            resetCache();
        } else if (evt.getSource() == platformManager
                && JavaPlatformManager.PROP_INSTALLED_PLATFORMS.equals(evt.getPropertyName())
                && activePlatformName != null) {
            // platform definitions were changed, check if the platform was not resolved or deleted
            if (isActivePlatformValid) {
                if (CommonProjectUtils.getActivePlatform(activePlatformName, platformType) == null) {
                    // the platform was not removed
                    resetCache();
                }
            } else {
                if (CommonProjectUtils.getActivePlatform(activePlatformName, platformType) != null) {
                    resetCache();
                }
            }
        } else if (endorsedClassPath != null && evt.getSource() == endorsedClassPath) {
            resetCache();
        }
    }

    private JavaPlatform findActivePlatform() {
        if (platformManager == null) {
            platformManager = JavaPlatformManager.getDefault();
            platformManager.addPropertyChangeListener(WeakListeners.propertyChange(this, platformManager));
        }
        activePlatformName = evaluator.getProperty(PLATFORM_ACTIVE);
        JavaPlatform activePlatform = CommonProjectUtils.getActivePlatform(activePlatformName, platformType);
        if (activePlatform != null) {
            isActivePlatformValid = true;
        } else {
            activePlatform = createPerProjectPlatform();
            isActivePlatformValid = false;
        }
        return activePlatform;
    }
    
    @CheckForNull
    private JavaPlatform createPerProjectPlatform() {
        if (project == null) {
            return null;
        }
        return ProjectPlatform.forProject(project, evaluator, platformType);
    }

    private void resetCache() {
        synchronized (this) {
            resourcesCache = null;
            eventId++;
        }
        support.firePropertyChange(PROP_RESOURCES, null, null);
    }
}
