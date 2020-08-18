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
import java.util.Arrays;
import java.util.List;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gradle.GradleDistributionManager;
import org.netbeans.modules.gradle.api.GradleBaseProject;
import org.netbeans.modules.gradle.api.NbGradleProject;
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
public class GradleDistributionProviderImpl implements GradleDistributionProvider{

    private static final List<String> AFFECTING_PROPS = Arrays.asList(
            PROP_GRADLE_USER_HOME,
            PROP_USE_CUSTOM_GRADLE,
            PROP_GRADLE_DISTRIBUTION,
            PROP_PREFER_WRAPPER,
            PROP_GRADLE_VERSION
    );

    final Project project;
    final Result res;

    public GradleDistributionProviderImpl(Project project) {
        this.project = project;
        res = new Res();
    }
    
    @Override
    public Result getGradleDistribution() {
        return res;
    }

    private class Res implements Result {

        private GradleDistributionManager.NbGradleVersion dist;

        private final ChangeSupport support = new ChangeSupport(this);
        private final PreferenceChangeListener listener = (PreferenceChangeEvent evt) -> {
            if (AFFECTING_PROPS.contains(evt.getKey())) {
                dist = null;
                support.fireChange();
            }
        };

        Res() {
        }

        @Override
        public File getGradleInstall() {
            GradleDistributionManager.NbGradleVersion d = getGradleDistribution();
            return d != null ? d.distributionDir() : null;
        }

        @Override
        public File getGradleHome() {
            return GradleSettings.getDefault().getGradleUserHome();
        }

        @Override
        public boolean isCompatibleWithSystemJava() {
            GradleDistributionManager.NbGradleVersion d = getGradleDistribution();
            return d != null ? d.isCompatibleWithSystemJava() : true;
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

        private synchronized GradleDistributionManager.NbGradleVersion getGradleDistribution() {
            if (dist == null) {
                GradleSettings settings = GradleSettings.getDefault();

                GradleDistributionManager mgr = GradleDistributionManager.get(getGradleHome());

                GradleBaseProject gbp = GradleBaseProject.get(project);

                if ((gbp != null) && settings.isWrapperPreferred()) {
                    dist = mgr.evaluateGradleWrapperDistribution(gbp.getRootDir());
                }

                if ((dist == null) && settings.useCustomGradle() && !settings.getDistributionHome().isEmpty()) {
                    //TODO: Add support for file based Gradle Distribution
                }
                if (dist == null) {
                    dist = mgr.createVersion(settings.getGradleVersion());
                }
                
            }
            return dist;
        }
    }
}
