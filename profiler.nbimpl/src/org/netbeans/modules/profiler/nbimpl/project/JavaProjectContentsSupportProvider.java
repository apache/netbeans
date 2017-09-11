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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.profiler.nbimpl.project;

import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.lib.profiler.client.ClientUtils;
import org.netbeans.lib.profiler.client.ClientUtils.SourceCodeSelection;
import org.netbeans.modules.profiler.api.java.SourceClassInfo;
import org.netbeans.modules.profiler.api.java.JavaProfilerSource;
import org.netbeans.modules.profiler.projectsupport.utilities.ProjectUtilities;
import org.netbeans.modules.profiler.spi.project.ProjectContentsSupportProvider;
import org.netbeans.spi.project.LookupProvider.Registration.ProjectType;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Jaroslav Bachorik
 */
@ProjectServiceProvider(projectTypes={
                            @ProjectType(id="org-netbeans-modules-java-j2seproject"),
                            @ProjectType(id="org-netbeans-modules-ant-freeform", position=1203),
                            @ProjectType(id="org-netbeans-modules-apisupport-project"),
                            @ProjectType(id="org-netbeans-modules-apisupport-project-suite"),
                            @ProjectType(id="org-netbeans-modules-j2ee-ejbjarproject"),
                            @ProjectType(id="org-netbeans-modules-web-project"),
                            @ProjectType(id="org-netbeans-modules-maven")},
                        service=ProjectContentsSupportProvider.class)
public class JavaProjectContentsSupportProvider extends ProjectContentsSupportProvider {
    
    private final String[][] packages = new String[2][];

    private final Project project;
    
    
    public JavaProjectContentsSupportProvider(Project project) {
        this.project = project;
    }

    @Override
    public String getInstrumentationFilter(boolean profileSubprojects) {
        return ProjectUtilities.computeProjectOnlyInstrumentationFilter(project, profileSubprojects, packages);
    }

    @Override
    public ClientUtils.SourceCodeSelection[] getProfilingRoots(FileObject profiledClassFile, boolean profileSubprojects) {
        if (profiledClassFile == null) {
            return ProjectUtilities.getProjectDefaultRoots(project, packages);
        } else {
            JavaProfilerSource src = JavaProfilerSource.createFrom(profiledClassFile);
            if (src != null) {
                Set<SourceClassInfo> clzs = src.getClasses();
                SourceCodeSelection[] rslt = new SourceCodeSelection[clzs.size()];
                
                int index = 0;
                for(SourceClassInfo ci : clzs) {
                    rslt[index++] = new SourceCodeSelection(ci.getQualifiedName(), "<all>", ""); // NOI18N
                }
                return rslt;
            }
            return null;
        }
    }
    
    @Override
    public void reset() {
        packages[0] = new String[0];
        packages[1] = new String[0];
    }

}
