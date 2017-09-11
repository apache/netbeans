/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
        Boolean toret = Boolean.valueOf(PluginPropertyUtils.getPluginProperty(project, Constants.GROUP_APACHE_PLUGINS, "maven-shade-plugin", "shadedArtifactAttached", "shade", null));
        if (toret == Boolean.FALSE) {
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
