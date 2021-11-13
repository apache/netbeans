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
package org.netbeans.modules.gradle;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.Collections;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.api.execute.GradleDistributionManager;
import org.netbeans.modules.gradle.api.execute.GradleDistributionManager.GradleDistribution;
import org.netbeans.modules.gradle.spi.execute.GradleDistributionProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;
import static org.netbeans.spi.project.ui.ProjectProblemsProvider.PROP_PROBLEMS;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author lkishalmi
 */
@ProjectServiceProvider(service = ProjectProblemsProvider.class, projectType = NbGradleProject.GRADLE_PROJECT_TYPE)
public final class GradleJavaCompatProblemsProvider implements ProjectProblemsProvider {

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    private final Project project;
    private final PropertyChangeListener listener;

    public GradleJavaCompatProblemsProvider(Project project) {
        this.project = project;
        listener = (PropertyChangeEvent evt) -> {
            if (NbGradleProject.PROP_PROJECT_INFO.equals(evt.getPropertyName())) {
                support.firePropertyChange(PROP_PROBLEMS, null, null);
            }
        };
        NbGradleProject.addPropertyChangeListener(project, listener);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    @Messages({
        "LBL_JavaVersionMismatch=Unsupported Java Runtime",
        "# {0} - Java Version",
        "# {1} - Supported Java Version",
        "# {2} - Required Gradle Version",
        "# {3} - Forced Gradle Version",
        "TXT_JavaVersionMismatch=The IDE is running on Java {0} that is not supported by Gradle {2}.\n"
                + "The IDE will attempt to use Gradle {3} to gather the project information.\n\n"
                + "Either upgrade your Gradle version on your project or run the IDE on "
                + "Java {1} to avoid this problem!"
    })
    @Override
    public Collection<? extends ProjectProblem> getProblems() {
        GradleDistributionProvider pvd = project.getLookup().lookup(GradleDistributionProvider.class);
        if (pvd != null) {
            GradleDistribution dist = pvd.getGradleDistribution();
            if ((dist != null) && !dist.isCompatibleWithSystemJava()) {
                String javaVersion = System.getProperty("java.specification.version", System.getProperty("java.version")); //NOI18N
                GradleDistribution compatDist = GradleDistributionManager.get(dist.getGradleUserHome()).defaultDistribution();
                ProjectProblem problem = ProjectProblem.createWarning(
                        Bundle.LBL_JavaVersionMismatch(), 
                        Bundle.TXT_JavaVersionMismatch(javaVersion, dist.lastSupportedJava(),dist.getVersion(), compatDist.getVersion()));
                return Collections.singleton(problem);
            }
        }
        return Collections.emptySet();
    }

}
