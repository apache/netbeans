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
package org.netbeans.modules.gradle.execute;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gradle.api.GradleBaseProject;
import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.api.execute.GradleDistributionManager;
import org.netbeans.modules.gradle.api.execute.GradleDistributionManager.GradleDistribution;
import org.netbeans.modules.gradle.spi.GradleSettings;
import org.netbeans.modules.gradle.spi.execute.GradleDistributionProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.util.ChangeSupport;

import static org.netbeans.modules.gradle.spi.GradleSettings.*;

/**
 *
 * @author lkishalmi
 */
@ProjectServiceProvider(service = GradleDistributionProvider.class, projectType = NbGradleProject.GRADLE_PROJECT_TYPE)
public class GradleDistributionProviderImpl implements GradleDistributionProvider {

    private final static Logger LOGGER = Logger.getLogger(GradleDistributionProviderImpl.class.getName());

    private static final List<String> AFFECTING_PROPS = Arrays.asList(
            PROP_GRADLE_USER_HOME,
            PROP_USE_CUSTOM_GRADLE,
            PROP_GRADLE_DISTRIBUTION,
            PROP_PREFER_WRAPPER,
            PROP_GRADLE_VERSION
    );

    private final ChangeSupport support = new ChangeSupport(this);
    private final PreferenceChangeListener listener = (PreferenceChangeEvent evt) -> {
        if (AFFECTING_PROPS.contains(evt.getKey())) {
            dist = null;
            support.fireChange();
        }
    };

    final Project project;
    private GradleDistribution dist;

    public GradleDistributionProviderImpl(Project project) {
        this.project = project;
    }

    @Override
    public GradleDistribution getGradleDistribution() {
        if (dist == null) {
            GradleSettings settings = GradleSettings.getDefault();

            GradleDistributionManager mgr = GradleDistributionManager.get(settings.getGradleUserHome());

            GradleBaseProject gbp = GradleBaseProject.get(project);

            if ((gbp != null) && settings.isWrapperPreferred()) {
                try {
                    dist = mgr.distributionFromWrapper(gbp.getRootDir());
                } catch (Exception ex) {
                    LOGGER.log(Level.FINE, "Cannot evaulate Gradle Wrapper", ex); //NOI18N
                }
            }

            if ((dist == null) && settings.useCustomGradle() && !settings.getDistributionHome().isEmpty()) {
                try {
                    dist = mgr.distributionFromDir(new File(settings.getDistributionHome()));
                } catch (IOException ex) {
                    LOGGER.log(Level.FINE, "Cannot evaulate Gradle Distribution", ex); //NOI18N
                }
            }
            if (dist == null) {
                dist = mgr.distributionFromVersion(settings.getGradleVersion());
            }
            LOGGER.log(Level.INFO, "Gradle Distribution for {0} is {1}", new Object[]{project, dist}); //NOI18N
        }
        return dist;
    }

    @Override
    public void addChangeListener(ChangeListener l) {
        if (!support.hasListeners()) {
            GradleSettings.getDefault().getPreferences().addPreferenceChangeListener(listener);
        }
        support.addChangeListener(l);
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        support.removeChangeListener(l);
        if (!support.hasListeners()) {
            GradleSettings.getDefault().getPreferences().removePreferenceChangeListener(listener);
        }
    }

}
