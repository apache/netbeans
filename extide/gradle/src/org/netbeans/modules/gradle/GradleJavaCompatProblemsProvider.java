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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.api.execute.GradleDistributionManager;
import org.netbeans.modules.gradle.api.execute.GradleDistributionManager.GradleDistribution;
import org.netbeans.modules.gradle.spi.execute.GradleDistributionProvider;
import org.netbeans.modules.gradle.spi.execute.GradleJavaPlatformProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;
import static org.netbeans.spi.project.ui.ProjectProblemsProvider.PROP_PROBLEMS;
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
        "TXT_JavaVersionMismatch=<html>The Java version: {0}, that is selected for the project "
                + "is not supported by Gradle {2}."
                + "The IDE will attempt to use Gradle {3} to gather the project information.<p>"
                + "Possible solutions:"
                + "<ul><li>Upgrade your Gradle version on your project"
                + "<li>Select Java Runtime {1} (or below), on Build&nbsp;>&nbsp;Gradle&nbsp;Execution settings, to avoid this problem!"
                + "</ul>"
    })
    @Override
    public Collection<? extends ProjectProblem> getProblems() {
        GradleDistribution dist = getGradleDistribution();
        int javaVersion = getJavaVersion();
        if (!dist.isCompatibleWithJava(javaVersion)) {
            GradleDistribution compatDist = GradleDistributionManager.get(dist.getGradleUserHome()).defaultDistribution();
            ProjectProblem problem = ProjectProblem.createWarning(
                    Bundle.LBL_JavaVersionMismatch(), 
                    Bundle.TXT_JavaVersionMismatch(javaVersion, dist.lastSupportedJava(),dist.getVersion(), compatDist.getVersion()));
            return Collections.singleton(problem);
        }
        return Collections.emptySet();
    }

    private GradleDistribution getGradleDistribution() {
        GradleDistribution dist = null;
        GradleDistributionProvider pvd = project.getLookup().lookup(GradleDistributionProvider.class);
        if (pvd != null) {
            dist = pvd.getGradleDistribution();
        }
        return dist != null ? dist : GradleDistributionManager.get().defaultDistribution();
    }

    private int getJavaVersion() {
        File javaHome = null;
        GradleJavaPlatformProvider pvd = project.getLookup().lookup(GradleJavaPlatformProvider.class);
        try {
            javaHome = pvd != null ? pvd.getJavaHome() : null;
        } catch (FileNotFoundException ex) {
            // That's a broken Java Home, other Problem Provider should pick that up
        }

        if (javaHome == null) {
            String javaVersion = System.getProperty("java.specification.version");
            int dot = javaVersion.indexOf('.');
            if (dot > 0) {
                javaVersion = javaVersion.substring(0, dot);
            }
            return Integer.parseInt(javaVersion);
        } else {
            return getJavaMajorVersion(javaHome);
        }
    }

    private static int getJavaMajorVersion(File javaHome) {
        // If anything goes wrong just assume Java 8
        int ret = 8;

        // The release file was introduced in JDK 9 and provided ever since
        File release = new File(javaHome, "release"); //NOI18N
        if (release.isFile()) {
            Properties releasePros = new Properties();
            try (InputStream is = new FileInputStream(release)) {
                releasePros.load(is);
            } catch (IOException ex) {

            }
            String javaVersion = releasePros.getProperty("JAVA_VERSION"); //NOI18N
            // This should look like "17" or "17.0.9"
            //TODO: Use Runtime.Version (when we move to Java 11)
            if ((javaVersion != null) && javaVersion.startsWith("\"") && javaVersion.endsWith("\"")) {
                int dot = javaVersion.indexOf('.');
                javaVersion = dot > 0
                        ? javaVersion.substring(1, javaVersion.indexOf('.'))
                        : javaVersion.substring(1, javaVersion.length() - 1);
                try {
                    ret = Integer.parseInt(javaVersion);
                } catch (NumberFormatException ex) {
                    // Do nothing return empty
                }
            }
        }
        return ret;
    }

}
