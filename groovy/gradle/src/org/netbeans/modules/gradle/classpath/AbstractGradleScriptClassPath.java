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

package org.netbeans.modules.gradle.classpath;

import org.netbeans.modules.gradle.GradleDistributionManager;
import org.netbeans.modules.gradle.GradleDistributionManager.NbGradleVersion;
import org.netbeans.modules.gradle.spi.GradleSettings;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.netbeans.modules.gradle.api.execute.RunUtils;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Laszlo Kishalmi
 */
abstract class AbstractGradleScriptClassPath implements ClassPathImplementation {

    private final PropertyChangeSupport cs = new PropertyChangeSupport(this);
    private final PreferenceChangeListener prefListener;
    private final PropertyChangeListener propListener;
    private final Preferences prefs = GradleSettings.getDefault().getPreferences();
    private NbGradleVersion watchedVersion;
    private List<PathResourceImplementation> resources;

    File distDir;

    public AbstractGradleScriptClassPath() {
        distDir = RunUtils.evaluateGradleDistribution(null, false);

        prefListener = new PreferenceChangeListener() {

            @Override
            @SuppressWarnings("fallthrough")
            public void preferenceChange(PreferenceChangeEvent evt) {
                GradleSettings settings = GradleSettings.getDefault();
                switch (evt.getKey()) {
                    case GradleSettings.PROP_GRADLE_VERSION: {
                        synchronized(AbstractGradleScriptClassPath.this) {
                            if (watchedVersion != null) {
                                watchedVersion.removePropertyChangeListener(propListener);
                                watchedVersion = null;
                            }
                            GradleDistributionManager gdm = GradleDistributionManager.get(settings.getGradleUserHome());
                            NbGradleVersion version = gdm.createVersion(settings.getGradleVersion());
                            if (!version.isAvailable()) {
                                watchedVersion = version;
                                watchedVersion.addPropertyChangeListener(propListener);
                            }
                        }
                    }
                    case GradleSettings.PROP_GRADLE_DISTRIBUTION:
                    case GradleSettings.PROP_USE_CUSTOM_GRADLE: {
                    }
                }
            }
        };

        propListener = new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                resources = null;
                cs.firePropertyChange(ClassPathImplementation.PROP_RESOURCES, null, null);
                synchronized(AbstractGradleScriptClassPath.this) {
                    if (watchedVersion != null) {
                        watchedVersion.removePropertyChangeListener(propListener);
                        watchedVersion = null;
                    }
                }

            }
        };
    }

    @Override
    public final List<? extends PathResourceImplementation> getResources() {
        if (resources == null) {
            resources = new ArrayList<>();
            if ((distDir !=null) && distDir.isDirectory()) {
                for (FileObject fo : createPath()) {
                    resources.add(ClassPathSupport.createResource(fo.toURL()));
                }
            }
        }
        return resources;
    }

    @Override
    public final void addPropertyChangeListener(PropertyChangeListener l) {
        if (!cs.hasListeners(null)) {
            prefs.addPreferenceChangeListener(prefListener);
        }
        cs.addPropertyChangeListener(l);
    }

    @Override
    public final void removePropertyChangeListener(PropertyChangeListener l) {
        cs.removePropertyChangeListener(l);
        if (!cs.hasListeners(null)) {
            prefs.removePreferenceChangeListener(prefListener);
        }
    }

    private void changeDistDir() {
        File newDistDir = RunUtils.evaluateGradleDistribution(null, false);
        if (!distDir.equals(newDistDir)) {
            distDir = newDistDir;
            resources = null;
            cs.firePropertyChange(ClassPathImplementation.PROP_RESOURCES, null, null);
        }

    }

    protected abstract List<FileObject> createPath();

}
