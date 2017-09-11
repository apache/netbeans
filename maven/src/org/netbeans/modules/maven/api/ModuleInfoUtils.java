/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 */
package org.netbeans.modules.maven.api;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.ModuleInfoSupport;

/**
 *
 * @author Tomas Stupka
 */
public final class ModuleInfoUtils {
    private static final String MODULE_INFO = "module-info.java"; // NOI18N
    
    /**
     * Adds the given artifacts as require entries into module-info.java.
     * The artifact scope determines the target module-info.java file - main or test.
     * If there is no module-info.java file then nothing happens.
     * 
     * @param project
     * @param artifacts 
     */
    public static void addRequires(NbMavenProject project, Collection<? extends Artifact> artifacts) {
        ModuleInfoSupport.addRequires(
            project.getMavenProject(), 
            artifacts.stream()
                .filter((a) -> Artifact.SCOPE_COMPILE.equals(a.getScope()) || Artifact.SCOPE_TEST.equals(a.getScope()))
                .collect(Collectors.toList()));
    }
    
    /**
     * Determines if the configured maven-compiler-plugin isn't too old in case 
     * there is a main module-info.java in the given project (has to be >= 3.6).
     * 
     * @param prj the project to be checked
     * @return <code>true</code> if there is no mofule-info file or if the m-c-p version is new enough (>= 3.6). Otherwise <code>false</code>
     */
    public static boolean checkModuleInfoAndCompilerFit(Project prj) {
        NbMavenProject nbprj = prj.getLookup().lookup(NbMavenProject.class);
        if (nbprj == null) {
            return true;
        }
        
        if (!hasModuleInfo(nbprj)) {
            return true;
        }
        
        String version = PluginPropertyUtils.getPluginVersion(nbprj.getMavenProject(), Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_COMPILER);
        if (version == null) {
            return true;
        }
        
        return new ComparableVersion(version).compareTo(new ComparableVersion(Constants.PLUGIN_COMPILER_VERSION_SUPPORTING_JDK9)) >= 0;
    }    
    
    private static boolean hasModuleInfo(NbMavenProject nbprj) {
        MavenProject mavenProject = nbprj.getMavenProject();        
        return hasModuleInfoInSourceRoot(mavenProject.getCompileSourceRoots()) || 
               hasModuleInfoInSourceRoot(mavenProject.getTestCompileSourceRoots());
    }

    private static boolean hasModuleInfoInSourceRoot(List<String> sourceRoots) {        
        for (String sourceRoot : sourceRoots) {
            if (new File(sourceRoot, MODULE_INFO).exists()) {
                return true;
            }
        }
        return false;
    }
}
