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

package org.netbeans.modules.maven.profiler;

import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.profiler.api.JavaPlatform;
import org.netbeans.modules.profiler.api.java.JavaProfilerSource;
import org.netbeans.modules.profiler.nbimpl.project.JavaProjectProfilingSupportProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Jiri Sedlacek
 */
@ProjectServiceProvider(service=org.netbeans.modules.profiler.spi.project.ProjectProfilingSupportProvider.class, 
                        projectType="org-netbeans-modules-maven") // NOI18N
public class MavenProjectProfilingSupportProvider extends JavaProjectProfilingSupportProvider {
    
    final private Set<String> supportedJ2eePTypes = new HashSet<String>() {
        {
            add(NbMavenProject.TYPE_WAR);
            add(NbMavenProject.TYPE_EJB);
        }
    };
    
    final private Set<String> supportedPTypes = new HashSet<String>() {
        {
            add(NbMavenProject.TYPE_JAR);
            addAll(supportedJ2eePTypes);
            add(NbMavenProject.TYPE_NBM);
            add(NbMavenProject.TYPE_NBM_APPLICATION);
            add(NbMavenProject.TYPE_OSGI);
        }
    };
    
    
    @Override
    public JavaPlatform resolveProjectJavaPlatform() {
        return JavaPlatform.getDefaultPlatform();
    }

    @Override
    public boolean isProfilingSupported() {
        NbMavenProject mproject = getMavenProject();
        return mproject == null ? false : supportedPTypes.contains(mproject.getPackagingType());
    }
    
    @Override
    public boolean checkProjectCanBeProfiled(FileObject file) {
        if (isJ2EEProject(getMavenProject())) {
            // Java EE project
            return true;
        } else {
            // Java SE project
            return super.checkProjectCanBeProfiled(file);
        }
    }
    
    @Override
    public boolean isFileObjectSupported(FileObject file) {
        if (isJ2EEProject(getMavenProject())) {
            // Java EE project
            return isHttpServlet(file) || isJSP(file);
        } else {
            // Java SE project
            return super.isFileObjectSupported(file);
        }
    }
        
    
    private boolean isHttpServlet(FileObject fo) {
        JavaProfilerSource src = JavaProfilerSource.createFrom(fo);
        return src != null && src.isInstanceOf("javax.servlet.http.HttpServlet"); // NOI18N
    }

    private boolean isJSP(FileObject fo) {
        return "jsp".equals(fo.getExt()); // NOI18N
    }
    
    private boolean isJ2EEProject(NbMavenProject mproject) {
        return supportedJ2eePTypes.contains(mproject.getPackagingType());
    }
    
    private NbMavenProject getMavenProject() {
        return getProject().getLookup().lookup(NbMavenProject.class);
    }
    
    
    public MavenProjectProfilingSupportProvider(Project project) {
        super(project);
    }
}
