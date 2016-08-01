/*******************************************************************************
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *******************************************************************************/
package org.black.kotlin.projectsextensions.maven.classpath;

import java.util.ArrayList;
import java.util.List;
import org.black.kotlin.projectsextensions.KotlinProjectHelper;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.classpath.ClassPathProviderImpl;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;

@ProjectServiceProvider(service={ClassPathProvider.class}, projectType="org-netbeans-modules-maven")
public final class MavenClassPathProviderImpl implements ClassPathProvider{

    private final Project project;
    private ClassPathProviderImpl impl;
    
    public MavenClassPathProviderImpl(Project project){
        this.project = project;
        this.impl = new ClassPathProviderImpl(project);
    }
    
    @Override
    public ClassPath findClassPath(FileObject fo, String type) {
        ClassPath cp = impl.findClassPath(fo, type);
        if (cp == null) {
            return null;
        }
        
        List<PathResourceImplementation> resources = new ArrayList<PathResourceImplementation>();
        if (type.equals(ClassPath.BOOT)){
            resources.add(ClassPathSupport.createResource(KotlinProjectHelper.INSTANCE.getLightClassesDirectory(project).toURL()));
        }
        ClassPath lightClasses = ClassPathSupport.createClassPath(resources);
        
        return ClassPathSupport.createProxyClassPath(cp, lightClasses);
    }
    
    public void updateClassPath() {
        impl = new ClassPathProviderImpl(project);
    }
   
}
