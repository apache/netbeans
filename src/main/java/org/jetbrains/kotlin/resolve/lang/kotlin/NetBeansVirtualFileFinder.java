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
package org.jetbrains.kotlin.resolve.lang.kotlin;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import javax.lang.model.element.TypeElement;
import org.jetbrains.kotlin.model.KotlinEnvironment;
import org.jetbrains.kotlin.projectsextensions.KotlinProjectHelper;
import org.jetbrains.kotlin.resolve.lang.java.NetBeansJavaClassFinder;
import org.jetbrains.kotlin.load.kotlin.JvmVirtualFileFinder;
import org.jetbrains.kotlin.load.kotlin.VirtualFileKotlinClassFinder;
import org.jetbrains.kotlin.load.kotlin.JvmVirtualFileFinderFactory;
import org.jetbrains.kotlin.name.ClassId;
import org.netbeans.api.project.Project;
import org.jetbrains.kotlin.load.java.structure.JavaClass;
import org.jetbrains.kotlin.load.kotlin.KotlinBinaryClassCache;
import org.jetbrains.kotlin.load.kotlin.KotlinJvmBinaryClass;
import org.jetbrains.kotlin.name.FqName;
import org.jetbrains.kotlin.resolve.lang.java.structure.NetBeansJavaClassifier;
import org.jetbrains.kotlin.resolve.lang.java.NbElementUtilsKt;
import org.netbeans.api.java.classpath.ClassPath;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Александр
 */
public class NetBeansVirtualFileFinder extends VirtualFileKotlinClassFinder implements JvmVirtualFileFinderFactory {

    Project project;
    
    public NetBeansVirtualFileFinder(Project project){
        this.project = project;
    }
    
    private boolean isClassFileName(String name){
        char[] suffixClass = ".class".toCharArray();
        char[] suffixClass2 = ".CLASS".toCharArray();
        int nameLength = name == null ? 0 : name.length();
        int suffixLength = suffixClass.length;
        if (nameLength < suffixLength) return false;
        
        for (int i = 0; i < suffixLength; i++){
            char c = name.charAt(nameLength - i - 1);
            int suffixIndex = suffixLength - i - 1;
            if (c != suffixClass[suffixIndex] && c != suffixClass2[suffixIndex]){
                return false;
            }
        }
        
        return true;
    }
    
    @Override
    public VirtualFile findVirtualFileWithHeader(ClassId classId) {
        ClassPath proxy = KotlinProjectHelper.INSTANCE.getFullClassPath(project);
        String rPath; 
        if (classId.isNestedClass()) {
            String className = classId.getShortClassName().asString();
            String fqName = classId.asSingleFqName().asString();
            StringBuilder rightPath = new StringBuilder(fqName.substring(0, fqName.length() - className.length() - 1).replace(".", "/"));
            rightPath.append("$").append(className).append(".class");
            rPath = rightPath.toString();
        } else {
            rPath = classId.asSingleFqName().asString().replace(".", "/")+".class";
        }
        
        FileObject resource = proxy.findResource(rPath);
        
        String path;
        if (resource != null){
            path = resource.toURL().getPath();
        } else {
            path = rPath;
        }
        
        String[] splittedPath = path.split("!/");
        if (splittedPath.length == 2){
            String pathToJar = splittedPath[0].replace("file:/", "");
            String classFile = splittedPath[1];
            return KotlinEnvironment.getEnvironment(project).getVirtualFileInJar(pathToJar, classFile);
        } else if (isClassFileName(path)){
            return KotlinEnvironment.getEnvironment(project).getVirtualFile(path);
        } else{
            throw new IllegalArgumentException("Virtual file not found for "+path);
        }
    }

    @Override
    public JvmVirtualFileFinder create(GlobalSearchScope gss) {
        return new NetBeansVirtualFileFinder(project); 
    }

    private boolean isBinaryKotlinClass(TypeElement type){
        return !NetBeansJavaClassFinder.isInKotlinBinFolder(type);
    }
    
    private String classFileName(JavaClass jClass){
        JavaClass outerClass = jClass.getOuterClass();
        if (outerClass == null)
            return jClass.getName().asString();
        return classFileName(outerClass) + "$" + jClass.getName().asString();
    }
    
    @Override
    public KotlinJvmBinaryClass findKotlinClass(JavaClass javaClass){
        FqName fqName = javaClass.getFqName();
        if (fqName == null){
            return null;
        }
        
        ClassId classId = NbElementUtilsKt.computeClassId(((NetBeansJavaClassifier)javaClass).getElementHandle(), project);
        
        if (classId == null){
            return null;
        }
        
        VirtualFile file = findVirtualFileWithHeader(classId);
        if (file == null){
            return null;
        }
        
        if (javaClass.getOuterClass() != null){
            String classFileName = classFileName(javaClass)+".class";
            file = file.getParent().findChild(classFileName);
            if (file != null){
                throw new IllegalStateException("Virtual file not found");
            }
        }
        
        return KotlinBinaryClassCache.Companion.getKotlinBinaryClass(file, null);
    }
    
}
