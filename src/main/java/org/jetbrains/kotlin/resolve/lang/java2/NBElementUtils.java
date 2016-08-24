/**
 * *****************************************************************************
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
 ******************************************************************************
 */
package org.jetbrains.kotlin.resolve.lang.java2;

import java.io.IOException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import org.jetbrains.kotlin.name.ClassId;
import org.jetbrains.kotlin.projectsextensions.ClassPathExtender;
import org.jetbrains.kotlin.projectsextensions.KotlinProjectHelper;
import org.jetbrains.kotlin.resolve.lang.java2.Searchers.ClassIdComputer;
import org.jetbrains.kotlin.resolve.lang.java2.Searchers.PackageElementSearcher;
import org.jetbrains.kotlin.resolve.lang.java2.Searchers.TypeElementSearcher;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.project.Project;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.util.Exceptions;

/**
 *
 * @author Alexander.Baratynski
 */
public class NBElementUtils {

    private static final Map<Project, JavaSource> JAVA_SOURCE = new HashMap<Project, JavaSource>();
    private static final Map<Project, ClasspathInfo> CLASSPATH_INFO = new HashMap<Project, ClasspathInfo>();

    private static ClasspathInfo getClasspathInfo(Project project) {

        assert project != null : "Project cannot be null";

        ClassPathExtender extendedProvider = KotlinProjectHelper.INSTANCE.getExtendedClassPath(project);

        ClassPath boot = extendedProvider.getProjectSourcesClassPath(ClassPath.BOOT);
        ClassPath src = extendedProvider.getProjectSourcesClassPath(ClassPath.SOURCE);
        ClassPath compile = extendedProvider.getProjectSourcesClassPath(ClassPath.COMPILE);

        ClassPath bootProxy = ClassPathSupport.createProxyClassPath(boot, compile);

        return ClasspathInfo.create(bootProxy, src, compile);
    }

    public static Set<String> getPackages(Project project, String name) {
        if (!CLASSPATH_INFO.containsKey(project)) {
            CLASSPATH_INFO.put(project, getClasspathInfo(project));
        }
        return CLASSPATH_INFO.get(project).getClassIndex().
                getPackageNames(name, false, EnumSet.of(ClassIndex.SearchScope.SOURCE, ClassIndex.SearchScope.DEPENDENCIES));
    }

    public static void updateClasspathInfo(Project project) {
        CLASSPATH_INFO.put(project, getClasspathInfo(project));
        JAVA_SOURCE.put(project, JavaSource.create(CLASSPATH_INFO.get(project)));
    }

    private static void checkJavaSource(Project project) {
        if (!CLASSPATH_INFO.containsKey(project)) {
            CLASSPATH_INFO.put(project, getClasspathInfo(project));
        }
        if (!JAVA_SOURCE.containsKey(project)) {
            JAVA_SOURCE.put(project, JavaSource.create(CLASSPATH_INFO.get(project)));
        }
    }

    public static void execute(Task<CompilationController> searcher, Project project) {
        checkJavaSource(project);
        try {
            JAVA_SOURCE.get(project).runUserActionTask(searcher, true);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public static ElementHandle<TypeElement> findType(String fqName, Project project) {
        TypeElementSearcher searcher = new TypeElementSearcher(fqName);
        execute(searcher, project);
        
        return searcher.getElement();
    }
    
    public static ElementHandle<PackageElement> findPackage(String fqName, Project project) {
        PackageElementSearcher searcher = new PackageElementSearcher(fqName);
        execute(searcher, project);
        
        return searcher.getPackage();
    }
    
    public static ClassId computeClassId(ElementHandle<TypeElement> handle, Project project) {
        ClassIdComputer computer = new ClassIdComputer(handle);
        execute(computer, project);
        
        return computer.getClassId();
    }
    
}
