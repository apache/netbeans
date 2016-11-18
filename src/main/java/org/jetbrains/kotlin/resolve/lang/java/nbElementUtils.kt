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
package org.jetbrains.kotlin.resolve.lang.java

import javax.lang.model.element.PackageElement
import javax.lang.model.element.TypeElement
import org.jetbrains.kotlin.load.java.structure.JavaClass
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.projectsextensions.ClassPathExtender
import org.jetbrains.kotlin.projectsextensions.KotlinProjectHelper
import org.jetbrains.kotlin.resolve.lang.java.Searchers.ClassIdComputer
import org.jetbrains.kotlin.resolve.lang.java.Searchers.ElementSimpleNameSearcher
import org.jetbrains.kotlin.resolve.lang.java.Searchers.IsDeprecatedSearcher
import org.jetbrains.kotlin.resolve.lang.java.Searchers.PackageElementSearcher
import org.jetbrains.kotlin.resolve.lang.java.Searchers.TypeElementSearcher
import org.jetbrains.kotlin.resolve.lang.java.Searchers.TypeMirrorHandleSearcher
import org.jetbrains.kotlin.resolve.lang.java.structure.NetBeansJavaClass
import org.netbeans.api.java.classpath.ClassPath
import org.netbeans.api.java.source.ClassIndex
import org.netbeans.api.java.source.ClasspathInfo
import org.netbeans.api.java.source.CompilationController
import org.netbeans.api.java.source.ElementHandle
import org.netbeans.api.java.source.JavaSource
import org.netbeans.api.java.source.Task
import org.netbeans.api.java.source.TypeMirrorHandle
import org.netbeans.api.java.source.ui.ElementOpen
import org.netbeans.api.project.Project
import org.netbeans.spi.java.classpath.support.ClassPathSupport
import org.openide.filesystems.FileObject
import javax.lang.model.type.DeclaredType
import org.jetbrains.kotlin.resolve.lang.java.Searchers.JavaDocSearcher
import org.netbeans.api.java.source.ScanUtils
import org.netbeans.api.java.source.CancellableTask
import org.netbeans.api.java.source.WorkingCopy
import org.netbeans.api.java.source.ModificationResult
import org.netbeans.api.java.platform.JavaPlatform

object JavaEnvironment {
    val JAVA_SOURCE = hashMapOf<Project, JavaSource>()
    val CLASSPATH_INFO = hashMapOf<Project, ClasspathInfo>()

    fun getClasspathInfo(project: Project): ClasspathInfo {
        val extendedProvider = KotlinProjectHelper.INSTANCE.getExtendedClassPath(project)
        val boot = extendedProvider.getProjectSourcesClassPath(ClassPath.BOOT)
        val src = extendedProvider.getProjectSourcesClassPath(ClassPath.SOURCE)
        val compile = extendedProvider.getProjectSourcesClassPath(ClassPath.COMPILE)
        
        return ClasspathInfo.create(boot, compile, src)
    }

    fun updateClasspathInfo(project: Project) {
        CLASSPATH_INFO.put(project, getClasspathInfo(project))
        JAVA_SOURCE.put(project, JavaSource.create(CLASSPATH_INFO.get(project)))
    }

    fun checkJavaSource(project: Project) {
        if (!CLASSPATH_INFO.containsKey(project)) {
            CLASSPATH_INFO.put(project, getClasspathInfo(project))
        }
        if (!JAVA_SOURCE.containsKey(project)) {
            JAVA_SOURCE.put(project, JavaSource.create(CLASSPATH_INFO.get(project)))
        }
    }

}

fun String.getPackages(project: Project): Set<String> {
    if (!JavaEnvironment.CLASSPATH_INFO.containsKey(project)) {
        JavaEnvironment.CLASSPATH_INFO.put(project, JavaEnvironment.getClasspathInfo(project))
    }
    return JavaEnvironment.CLASSPATH_INFO.get(project)!!.classIndex.
            getPackageNames(this, false, hashSetOf(ClassIndex.SearchScope.SOURCE, ClassIndex.SearchScope.DEPENDENCIES))
}

fun Project.findType(fqName: String): ElemHandle<TypeElement>? {
    JavaEnvironment.checkJavaSource(this)
    val classIndex = JavaEnvironment.JAVA_SOURCE[this]!!.getClasspathInfo().getClassIndex()
    
    val name = fqName.substringAfterLast(".", fqName)
    
    val declaredTypes = classIndex.getDeclaredTypes(name, ClassIndex.NameKind.SIMPLE_NAME,
            setOf(ClassIndex.SearchScope.DEPENDENCIES, ClassIndex.SearchScope.SOURCE))
    
    val elementHandle = declaredTypes.filter { it.qualifiedName == fqName }
            .firstOrNull() ?: return null
    
    return ElemHandle.create(elementHandle, this)
}

fun <T : Task<CompilationController>> T.execute(project: Project): T {
    JavaEnvironment.checkJavaSource(project)
    JavaEnvironment.JAVA_SOURCE[project]!!.runUserActionTask(this, true)
    
    return this
}

fun <T : CancellableTask<WorkingCopy>> T.modify(file: FileObject): ModificationResult {
    val javaSource = JavaSource.forFileObject(file)
    return javaSource.runModificationTask(this)
}

fun CompilationController.toResolvedPhase() = this.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED)

fun Project.findTypeMirrorHandle(name: String) =
        TypeMirrorHandleSearcher(name).execute(this).handle

fun Project.findPackage(name: String) =
        PackageElementSearcher(name, this).execute(this).`package`

fun ElemHandle<TypeElement>.computeClassId(project: Project) =
        ClassIdComputer(this).execute(project).classId

fun ElemHandle<*>.getSimpleName(project: Project) =
        ElementSimpleNameSearcher(this).execute(project).simpleName

fun TypeMirrorHandle<*>.getHashCode(project: Project) =
        TypeMirrorHandleHashCodeSearcher(this).execute(project).hashCode

fun TypeMirrorHandle<*>.isEqual(handle: TypeMirrorHandle<*>, project: Project) =
        TypeMirrorHandleEquals(this, handle).execute(project).equals

fun Project.findFQName(name: String): List<String> {
    JavaEnvironment.checkJavaSource(this)

    return JavaEnvironment.CLASSPATH_INFO[this]!!.classIndex.
            getDeclaredTypes(name, ClassIndex.NameKind.SIMPLE_NAME,
                    setOf(ClassIndex.SearchScope.SOURCE,
                            ClassIndex.SearchScope.DEPENDENCIES))
            .map { it.qualifiedName }
}

fun Project.findTypes(prefix: String): List<ElementHandle<TypeElement>> {
    JavaEnvironment.checkJavaSource(this)
    
    return JavaEnvironment.CLASSPATH_INFO[this]!!.classIndex.
            getDeclaredTypes(prefix, ClassIndex.NameKind.CASE_INSENSITIVE_PREFIX,
                    setOf(ClassIndex.SearchScope.SOURCE,
                            ClassIndex.SearchScope.DEPENDENCIES)).toList()
}

fun ElementHandle<*>.openInEditor(project: Project) =
        ElementOpen.open(JavaEnvironment.CLASSPATH_INFO[project], this)

fun TypeMirrorHandle<*>.getJavaClass(project: Project) =
        NetBeansJavaClass(ElemHandle.from(this, project), project)

fun TypeMirrorHandle<DeclaredType>.computeClassId(project: Project) =
        ElemHandle.from(this, project).computeClassId(project)

fun ElemHandle<*>.isDeprecated(project: Project) =
        IsDeprecatedSearcher(this).execute(project).isDeprecated

fun Project.findClassUsages(className: String): Set<FileObject> {
    val handle = this.findType(className) ?: return emptySet()

    return JavaEnvironment.CLASSPATH_INFO[this]!!.classIndex.getResources(handle.elementHandle,
            ClassIndex.SearchKind.values().toSet(), hashSetOf(ClassIndex.SearchScope.SOURCE))
}

fun Project.getFileObjectForFqName(fqName: String): FileObject? {
    val handle = this.findType(fqName) ?: return null

    val fObjects = JavaEnvironment.CLASSPATH_INFO[this]!!.classIndex.getResources(handle.elementHandle,
            setOf(ClassIndex.SearchKind.IMPLEMENTORS), setOf(ClassIndex.SearchScope.DEPENDENCIES),
            setOf(ClassIndex.ResourceType.BINARY))

    return fObjects.elementAt(0) ?: null
}

fun ElemHandle<*>.getJavaDoc(project: Project) =
        JavaDocSearcher(this).execute(project).javaDoc