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
package org.jetbrains.kotlin.resolve.lang.java;

import com.google.common.collect.Sets;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import org.jetbrains.kotlin.load.java.structure.JavaClass;
import org.jetbrains.kotlin.name.ClassId;
import org.jetbrains.kotlin.projectsextensions.ClassPathExtender;
import org.jetbrains.kotlin.projectsextensions.KotlinProjectHelper;
import org.jetbrains.kotlin.resolve.lang.java.Searchers.ClassIdComputer;
import org.jetbrains.kotlin.resolve.lang.java.Searchers.ElementSimpleNameSearcher;
import org.jetbrains.kotlin.resolve.lang.java.Searchers.FileObjectForFqNameSearcher;
import org.jetbrains.kotlin.resolve.lang.java.Searchers.PackageElementSearcher;
import org.jetbrains.kotlin.resolve.lang.java.Searchers.TypeElementSearcher;
import org.jetbrains.kotlin.resolve.lang.java.Searchers.TypeMirrorHandleSearcher;
import org.jetbrains.kotlin.resolve.lang.java.structure.NetBeansJavaClass;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.api.java.source.ui.ElementOpen;
import org.netbeans.api.project.Project;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
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
    
    public static TypeMirrorHandle findTypeMirrorHandle(String fqName, Project project) {
        TypeMirrorHandleSearcher searcher = new TypeMirrorHandleSearcher(fqName);
        execute(searcher, project);
        
        return searcher.getHandle();
    }
    
    public static ElementHandle<PackageElement> findPackage(String fqName, Project project) {
        PackageElementSearcher searcher = new PackageElementSearcher(fqName);
        execute(searcher, project);
        
        return searcher.getPackage();
    }
    
    public static ClassId computeClassId(ElementHandle handle, Project project) {
        ClassIdComputer computer = new ClassIdComputer(handle);
        execute(computer, project);
        
        return computer.getClassId();
    }
    
    public static String getSimpleName(ElementHandle handle, Project project) {
        ElementSimpleNameSearcher searcher = new ElementSimpleNameSearcher(handle);
        execute(searcher, project);
        
        return searcher.getSimpleName();
    }

    public static int typeMirrorHandleHashCode(TypeMirrorHandle handle, Project project) {
        ParameterSearchers.TypeMirrorHandleHashCodeSearcher searcher = new ParameterSearchers.TypeMirrorHandleHashCodeSearcher(handle);
        NBElementUtils.execute(searcher, project);
        return searcher.getHashCode();
    }

    public static boolean typeMirrorHandleEquals(TypeMirrorHandle handle1, TypeMirrorHandle handle2, Project project) {
        ParameterSearchers.TypeMirrorHandleEquals searcher = new ParameterSearchers.TypeMirrorHandleEquals(handle1, handle2);
        NBElementUtils.execute(searcher, project);
        return searcher.equals();
    }
    
    public static List<String> findFQName(Project project, String name) {
        checkJavaSource(project);
        List<String> fqNames = new ArrayList<String>();
        
        final Set<ElementHandle<TypeElement>> result = 
                CLASSPATH_INFO.get(project).getClassIndex().
                        getDeclaredTypes(name, ClassIndex.NameKind.SIMPLE_NAME, EnumSet.of(ClassIndex.SearchScope.SOURCE, ClassIndex.SearchScope.DEPENDENCIES));
        
        for (ElementHandle<TypeElement> handle : result) {
            fqNames.add(handle.getQualifiedName());
        }
        
        return fqNames;
    }
    
    public static void openElementInEditor(ElementHandle handle, Project kotlinProject){
        ElementOpen.open(CLASSPATH_INFO.get(kotlinProject), handle);
    }
    
    public static JavaClass getNetBeansJavaClassFromType(TypeMirrorHandle type, Project project) {
        ElementHandle handle = ElementHandle.from(type);
        return new NetBeansJavaClass(handle, project);
    }
    
    public static FileObject getFileObjectForFqName(String fqName, Project project) {
//        checkJavaSource(project);
//        FileObjectForFqNameSearcher searcher = new FileObjectForFqNameSearcher(fqName, CLASSPATH_INFO.get(project));
//        execute(searcher, project);
        
//        return searcher.getFileObject();

        ElementHandle<TypeElement> handle = findType(fqName, project);
        Set<FileObject> fObjects = CLASSPATH_INFO.get(project).getClassIndex().getResources(handle, 
                Sets.newHashSet(ClassIndex.SearchKind.IMPLEMENTORS), 
                Sets.newHashSet(ClassIndex.SearchScope.DEPENDENCIES), 
                Sets.newHashSet(ClassIndex.ResourceType.BINARY));
        if (fObjects.isEmpty()) {
            return null;
        } else {
            return fObjects.iterator().next();
        }
    }
    
}
