/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.execute;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.maven.api.FileUtilities;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.api.execute.PrerequisitesChecker;
import org.netbeans.modules.maven.configurations.M2ConfigProvider;
import org.netbeans.modules.maven.configurations.M2Configuration;
import org.netbeans.modules.maven.embedder.MavenEmbedder;
import org.netbeans.modules.maven.embedder.MavenEmbedder.ModelDescription;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

@ProjectServiceProvider(service=PrerequisitesChecker.class, projectType="org-netbeans-modules-maven")
public class ReactorChecker implements PrerequisitesChecker {
    
    public @Override boolean checkRunConfig(RunConfig config) {
        if (config.getPreExecution() != null && !checkRunConfig(config.getPreExecution())) {
            return false;
        }
        if (config.getReactorStyle() == RunConfig.ReactorStyle.NONE) {
            return true;
        }
        File dir = config.getExecutionDirectory();
        FileObject fo = FileUtil.toFileObject(dir);
        Project p = config.getProject();
        if (p == null || fo != p.getProjectDirectory()) {
            // Custom <basedir> perhaps? Skip.
            return true;
        }
        NbMavenProject mavenprj = p.getLookup().lookup(NbMavenProject.class);
        if (mavenprj == null) {
            // Unloadable?
            return true;
        }
        if (NbMavenProject.isErrorPlaceholder(mavenprj.getMavenProject())) {
            return true; // broken project
        }
        NbMavenProject reactor = findReactor(mavenprj);
        File reactorRoot = reactor.getMavenProject().getBasedir();
        if (reactor != mavenprj) {
            try {
                M2Configuration cfg = ProjectManager.getDefault().findProject(FileUtil.toFileObject(reactorRoot)).getLookup().lookup(M2ConfigProvider.class).getActiveConfiguration();
                if (cfg != null) {
                    List<String> reactorProfiles = cfg.getActivatedProfiles();
                    if (!reactorProfiles.isEmpty()) {
                        List<String> profiles = new ArrayList<String>(config.getActivatedProfiles());
                        profiles.addAll(reactorProfiles);
                        config.setActivatedProfiles(profiles);
                    }
                }
            } catch (IOException x) {
                Exceptions.printStackTrace(x);
            }
        }
        config.setExecutionDirectory(reactorRoot);
        return true;
    }
    
    /**
     * Tries to find the reactor root starting from what may be just a submodule.
     * The intent is that running {@code mvn -f $reactor/pom.xml --projects $module} would work.
     * @param module a project to start the search at
     * @return its apparent reactor root; maybe just the same project
     */
    public static @NonNull NbMavenProject findReactor(@NonNull NbMavenProject module) { // #197232
        MavenProject prj = module.getMavenProject();
        List<ModelDescription> models = MavenEmbedder.getModelDescriptors(prj);
        File moduleDir = prj.getBasedir();
        File current = moduleDir;
        if (current != null && models != null) { //models are null for totally broken projects..
            boolean first = true;
            for (ModelDescription model : models) {
                if (first) { //ignore the first value, it's the current project
                    first = false;
                    continue;
                }
                File loc = model.getLocation();
                if (loc == null || loc.getName().endsWith(".pom")) {
                    break;
                }
                File modelDir = loc.getParentFile();
                if (listsModule(modelDir, current, model.getModules())) {
                    current = modelDir;
                } else {
                    break;
                }
            }
            if (!moduleDir.equals(current)) {
                NbMavenProject loaded = load(current);
                if (loaded != null) {
                    return findReactor(loaded);
                }
            }
        }
        NbMavenProject p = load(prj.getBasedir().getParentFile());
        if (p != null && listsModule(moduleDir.getParentFile(), moduleDir, p.getMavenProject().getModules())) {
            return findReactor(p);
        }
        return module;
    }
    private static boolean listsModule(File parentDir, File moduleDir, List<String> modules) {
        for (String module : modules) {
            if (moduleDir.equals(FileUtilities.resolveFilePath(parentDir, module))) {
                return true;
            }
        }
        return false;
    }
    private static @CheckForNull NbMavenProject load(File parentDir) {
        if (parentDir == null) {
            return null;
        }
        FileObject d = FileUtil.toFileObject(parentDir);
        if (d != null) {
            try {
                Project p = ProjectManager.getDefault().findProject(d);
                if (p != null) {
                    return p.getLookup().lookup(NbMavenProject.class);
                }
            } catch (IOException x) {
                Exceptions.printStackTrace(x);
            }
        }
        return null;
    }

}
