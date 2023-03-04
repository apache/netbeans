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

package org.netbeans.modules.gradle.java.classpath;

import org.netbeans.modules.gradle.spi.GradleSettings;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.netbeans.modules.gradle.api.execute.GradleDistributionManager;
import org.netbeans.modules.gradle.api.execute.GradleDistributionManager.GradleDistribution;
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
    private final Preferences prefs = GradleSettings.getDefault().getPreferences();
    private List<PathResourceImplementation> resources;

    File distDir;

    public AbstractGradleScriptClassPath() {
        changeDistDir();

        prefListener = (PreferenceChangeEvent evt) -> {
            switch (evt.getKey()) {
                case GradleSettings.PROP_GRADLE_VERSION:
                case GradleSettings.PROP_GRADLE_DISTRIBUTION:
                case GradleSettings.PROP_USE_CUSTOM_GRADLE: {
                    changeDistDir();
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
        GradleDistribution dist = GradleDistributionManager.get().defaultDistribution();
        File newDistDir = dist.getDistributionDir();
        if (distDir != null && !distDir.equals(newDistDir)) {
            distDir = newDistDir;
            resources = null;
            cs.firePropertyChange(ClassPathImplementation.PROP_RESOURCES, null, null);
        }

    }

    protected abstract List<FileObject> createPath();

}
