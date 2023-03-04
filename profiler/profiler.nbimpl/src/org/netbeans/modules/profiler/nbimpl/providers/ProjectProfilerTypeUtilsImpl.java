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
package org.netbeans.modules.profiler.nbimpl.providers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.*;
import org.netbeans.api.project.Project;
import org.netbeans.modules.profiler.api.java.SourceClassInfo;
import org.netbeans.modules.profiler.nbimpl.javac.ClasspathInfoFactory;
import org.netbeans.modules.profiler.nbimpl.javac.JavacClassInfo;
import org.netbeans.modules.profiler.projectsupport.utilities.ProjectUtilities;
import org.netbeans.modules.profiler.spi.java.ProfilerTypeUtilsProvider;
import org.netbeans.spi.project.LookupProvider.Registration.ProjectType;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Jaroslav Bachorik
 */
@ProjectServiceProvider(service=ProfilerTypeUtilsProvider.class, projectTypes={
    @ProjectType(id="org-netbeans-modules-java-j2seproject"), // NOI18N
    @ProjectType(id="org-netbeans-modules-j2ee-ejbjarproject"),  // NOI18N
    @ProjectType(id="org-netbeans-modules-j2ee-earproject"),  // NOI18N
    @ProjectType(id="org-netbeans-modules-apisupport-project"), // NOI18N
    @ProjectType(id="org-netbeans-modules-apisupport-project-suite"), // NOI18N
    @ProjectType(id="org-netbeans-modules-web-project"), // NOI18N
    @ProjectType(id="org-netbeans-modules-ant-freeform", position=1230), // NOI18N, 
    @ProjectType(id="org-netbeans-modules-maven/jar"), // NOI18N
    @ProjectType(id="org-netbeans-modules-maven/war"), // NOI18N
    @ProjectType(id="org-netbeans-modules-maven/ejb"), // NOI18N
    @ProjectType(id="org-netbeans-modules-maven/nbm"), // NOI18N
    @ProjectType(id="org-netbeans-modules-maven/nbm-application"), // NOI18N
    @ProjectType(id="org-netbeans-modules-maven/bundle") // NOI18N
})

public class ProjectProfilerTypeUtilsImpl extends BaseProfilerTypeUtilsImpl {
    private Project project;
    
    public ProjectProfilerTypeUtilsImpl(Project prj) {
        this.project = prj;
    }
    
    @Override
    public Collection<SourceClassInfo> getMainClasses() {
        List<SourceClassInfo> classes = new ArrayList<SourceClassInfo>();
        FileObject[] srcRoots = ProjectUtilities.getSourceRoots(project, false);
        for(ElementHandle<TypeElement> handle : SourceUtils.getMainClasses(srcRoots)) {
            SourceClassInfo ci = resolveClass(handle);
            classes.add(ci);
        }
        
        return classes;
    }

    @Override
    protected ClasspathInfo getClasspathInfo() {
        if (project != null) {
            return ClasspathInfoFactory.infoFor(project);
        }
        return null;
    }

    @Override
    protected ClasspathInfo getClasspathInfo(boolean subprojects, boolean source, boolean deps) {
        return ClasspathInfoFactory.infoFor(project, subprojects, source, deps);
    }
    
    private SourceClassInfo resolveClass(ElementHandle<TypeElement> h) {
        ClasspathInfo cpInfo = getClasspathInfo();
        if (cpInfo != null) {
            return new JavacClassInfo(h, cpInfo);
        }
        return null;
    }
}