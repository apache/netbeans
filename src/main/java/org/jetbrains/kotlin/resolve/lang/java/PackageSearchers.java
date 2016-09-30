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

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import kotlin.jvm.functions.Function1;
import org.jetbrains.kotlin.load.java.structure.JavaClass;
import org.jetbrains.kotlin.load.java.structure.JavaPackage;
import org.jetbrains.kotlin.name.FqName;
import org.jetbrains.kotlin.name.Name;
import org.jetbrains.kotlin.resolve.lang.java.structure.NetBeansJavaClass;
import org.jetbrains.kotlin.resolve.lang.java.structure.NetBeansJavaPackage;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.project.Project;

/**
 *
 * @author Alexander.Baratynski
 */
public class PackageSearchers {

    public static class SubPackagesSearcher implements Task<CompilationController> {

        private final JavaPackage pack;
        private final Project project;
        private final Collection<JavaPackage> subPackages = Lists.newArrayList();

        public SubPackagesSearcher(Project project, JavaPackage pack) {
            this.pack = pack;
            this.project = project;
        }

        public PackageElement[] findPackageFragments(String name,
                boolean partialMatch, boolean patternMatch, CompilationController info) {
            Set<String> packages = NBElementUtils.getPackages(project, name);
            List<PackageElement> subpackageElements = Lists.newArrayList();
            for (String pack : packages) {
                PackageElement subpackageElement = info.getElements().getPackageElement(name);
                if (subpackageElement == null) {
                    continue;
                }
                subpackageElements.add(subpackageElement);
            }

            if (subpackageElements.isEmpty()) {
                return null;
            }

            return subpackageElements.toArray(new PackageElement[subpackageElements.size()]);
        }

        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.RESOLVED);
            String thisPackageName = pack.getFqName().asString();
            String pattern = thisPackageName.isEmpty() ? "*" : thisPackageName + ".";

            PackageElement[] packageFragments = findPackageFragments(pattern, true, true, info);

            int thisNestedLevel = thisPackageName.split("\\.").length;
            if (packageFragments != null && packageFragments.length > 0) {
                for (PackageElement packageFragment : packageFragments) {
                    int subNestedLevel = packageFragment.getQualifiedName().toString().split("\\.").length;
                    boolean applicableForRootPackage = thisNestedLevel == 1 && thisNestedLevel == subNestedLevel;
                    if (!packageFragment.getQualifiedName().toString().isEmpty()
                            && (applicableForRootPackage || (thisNestedLevel + 1 == subNestedLevel))) {
                        subPackages.add(new NetBeansJavaPackage(ElemHandle.create(packageFragment, project), project));
                    }
                }
            }
        }

        public Collection<JavaPackage> getSubPackages() {
            return subPackages;
        }

    }

    public static class ClassesSearcher implements Task<CompilationController> {

        private final Project project;
        private final List<ElemHandle<PackageElement>> packages;
        private final Function1<? super Name, Boolean> nameFilter;
        private final Collection<JavaClass> classes = Lists.newArrayList();

        public ClassesSearcher(List<ElemHandle<PackageElement>> packages, Project project, 
                Function1<? super Name, Boolean> nameFilter) {
            this.packages = packages;
            this.project = project;
            this.nameFilter = nameFilter;
        }

        private boolean isOuterClass(TypeElement classFile){
            return !classFile.getSimpleName().toString().contains("$");
        }
        
        private List<JavaClass> getClassesInPackage(PackageElement javaPackage,
                Function1<? super Name, ? extends Boolean> nameFilter) {
            List<JavaClass> javaClasses = Lists.newArrayList();
            List<? extends Element> classes = javaPackage.getEnclosedElements();

            for (Element cl : classes) {
                if (isOuterClass((TypeElement) cl)) {
                    String elementName = cl.getSimpleName().toString();
                    if (Name.isValidIdentifier(elementName) && nameFilter.invoke(Name.identifier(elementName))) {
                        javaClasses.add(new NetBeansJavaClass(ElemHandle.create(cl, project), project));
                    }
                }
            }

            return javaClasses;
        }

        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.RESOLVED);
            
            for (ElemHandle<PackageElement> packHandle : packages) {
                Element elem = packHandle.resolve(info);
                if (elem == null) {
                    continue;
                }
                
                classes.addAll(getClassesInPackage((PackageElement) elem, nameFilter));
            }
            
        }

        public Collection<JavaClass> getClasses() {
            return classes;
        }

    }
    
    public static class FqNameSearcher implements Task<CompilationController> {

        private final ElemHandle<PackageElement> handle;
        private FqName fqName = null;
        
        public FqNameSearcher(ElemHandle<PackageElement> handle) {
            this.handle = handle;
        }
        
        @Override
        public void run(CompilationController info) throws Exception {
            info.toPhase(Phase.RESOLVED);
            Element pack = handle.resolve(info);
            if (pack == null) {
                return;
            }
            
            fqName = new FqName(((PackageElement) pack).getQualifiedName().toString());
        }
        
        public FqName getFqName() {
            return fqName;
        }
    }

}
