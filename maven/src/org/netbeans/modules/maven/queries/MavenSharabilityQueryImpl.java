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
package org.netbeans.modules.maven.queries;

import java.io.File;
import java.net.URI;
import java.util.Collection;
import org.apache.maven.model.Build;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.project.Project;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.configurations.M2ConfigProvider;
import org.netbeans.modules.maven.configurations.M2Configuration;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.queries.SharabilityQueryImplementation2;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/**
 *
 * @author  Milos Kleint
 */
@ProjectServiceProvider(service=SharabilityQueryImplementation2.class, projectType="org-netbeans-modules-maven")
public class MavenSharabilityQueryImpl implements SharabilityQueryImplementation2 {
    
    private final Project project;

    public MavenSharabilityQueryImpl(Project proj) {
        project = proj;
    }
    
    public @Override SharabilityQuery.Sharability getSharability(URI uri) {
        //#119541 for the project's root, return MIXED right away.
        File file = FileUtil.normalizeFile(Utilities.toFile(uri));
        FileObject fo = FileUtil.toFileObject(file);
        if (fo != null && fo.equals(project.getProjectDirectory())) {
            return SharabilityQuery.Sharability.MIXED;
        }
        File basedir = FileUtil.toFile(project.getProjectDirectory());
        // is this condition necessary?
        if (!file.getAbsolutePath().startsWith(basedir.getAbsolutePath())) {
            return SharabilityQuery.Sharability.UNKNOWN;
        }
        if (basedir.equals(file.getParentFile())) {
            // Interesting cases are of direct children.
            if (file.getName().equals("pom.xml")) { // NOI18N
                return SharabilityQuery.Sharability.SHARABLE;
            }
            if ("nbproject".equals(file.getName())) { //NOI18N
                // screw the netbeans profiler directory creation.
                // #98662
                return SharabilityQuery.Sharability.NOT_SHARABLE;
            }
            if (file.getName().startsWith("nbactions")) { //NOI18N
                //non shared custom configurations shall not be added to version control.
                M2ConfigProvider configs = project.getLookup().lookup(M2ConfigProvider.class);
                if (configs != null) {
                    Collection<M2Configuration> col = configs.getNonSharedConfigurations();
                    for (M2Configuration conf : col) {
                        if (file.getName().equals(M2Configuration.getFileNameExt(conf.getId()))) {
                            return SharabilityQuery.Sharability.NOT_SHARABLE;
                        }
                    }
                }
            }
            if (file.getName().equals("src")) { // NOI18N
                // hardcoding this name since Maven will only report particular subtrees
                return SharabilityQuery.Sharability.SHARABLE; // #174010
            }
        }

        //this part is slow if invoked on built project that is not opened (needs to load the embedder)
        //can it be replaced with code not touching the embedder?
        MavenProject proj = project.getLookup().lookup(NbMavenProject.class).getMavenProject();
        Build build = proj.getBuild();
        if (build != null && build.getDirectory() != null) {
            File target = new File(build.getDirectory());
            if (target.equals(file) || file.getAbsolutePath().startsWith(target.getAbsolutePath())) {
                return SharabilityQuery.Sharability.NOT_SHARABLE;
            }
        }

        // Some other subdir with potentially unknown contents.
        if (file.isDirectory()) {
            return SharabilityQuery.Sharability.MIXED;
        } else {
            return SharabilityQuery.Sharability.UNKNOWN;
        }
    }
    
}
