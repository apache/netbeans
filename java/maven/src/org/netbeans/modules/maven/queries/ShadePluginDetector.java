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

package org.netbeans.modules.maven.queries;

import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.modules.maven.api.problem.ProblemReport;
import org.netbeans.modules.maven.api.problem.ProblemReporter;
import org.netbeans.modules.maven.spi.queries.ForeignClassBundler;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.util.NbBundle;
import static org.netbeans.modules.maven.queries.Bundle.*;

/**
 * Indicates that a shaded JAR should be consulted in preference to sources.
 */
@ProjectServiceProvider(service=ForeignClassBundler.class, projectType="org-netbeans-modules-maven")
@NbBundle.Messages({
    "PRBL_Name=Project's main artifact is processed through maven-shade-plugin",
    "PRBL_DESC=When the final artifact jar contains classes not originating in current project, NetBeans internal compiler cannot use the sources of the project for compilation. Then changes done in project's source code only appears in depending projects when project is recompiled. Also applies to features like Refactoring which will not be able to find usages in depending projects."
})

public class ShadePluginDetector implements ForeignClassBundler { // #155091
    private static final ProblemReport PROBLEM_REPORT = new ProblemReport(ProblemReport.SEVERITY_MEDIUM, 
            PRBL_Name(), PRBL_DESC(), null);

    private final Project project;

    public ShadePluginDetector(Project project) {
        this.project = project;
    }

    private boolean calculateValue() {
        ProblemReporter pr = project.getLookup().lookup(ProblemReporter.class);
        if (pr != null) {
            pr.removeReport(PROBLEM_REPORT);
        }
        
        NbMavenProject nbmp = project.getLookup().lookup(NbMavenProject.class);
        if (nbmp == null) {
            return true;
        }
        if (PluginPropertyUtils.getPluginVersion(nbmp.getMavenProject(), Constants.GROUP_APACHE_PLUGINS, "maven-shade-plugin") == null) {
            return true;
        }
        boolean toret = Boolean.parseBoolean(PluginPropertyUtils.getPluginProperty(project, Constants.GROUP_APACHE_PLUGINS, "maven-shade-plugin", "shadedArtifactAttached", "shade", null));
        if (!toret) {
            if (pr != null) {
                pr.addReport(PROBLEM_REPORT);
            }
        }
        return toret;
    }
    
    private boolean calculated = false;
    private boolean calculatedValue = false;


    @Override 
    public synchronized boolean preferSources() {
        if (calculated) {
            return calculatedValue;
        }
        calculatedValue = calculateValue(); 
        calculated = true;
        return calculatedValue; 
    }

    @Override
    public synchronized void resetCachedValue() {
        calculated = false;
    }

}
