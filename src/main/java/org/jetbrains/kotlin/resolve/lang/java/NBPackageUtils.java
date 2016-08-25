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

import java.util.Collection;
import java.util.List;
import javax.lang.model.element.PackageElement;
import kotlin.jvm.functions.Function1;
import org.jetbrains.kotlin.load.java.structure.JavaClass;
import org.jetbrains.kotlin.load.java.structure.JavaPackage;
import org.jetbrains.kotlin.name.FqName;
import org.jetbrains.kotlin.name.Name;
import org.jetbrains.kotlin.resolve.lang.java.PackageSearchers.ClassesSearcher;
import org.jetbrains.kotlin.resolve.lang.java.PackageSearchers.FqNameSearcher;
import org.jetbrains.kotlin.resolve.lang.java.PackageSearchers.SubPackagesSearcher;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.project.Project;

/**
 *
 * @author Alexander.Baratynski
 */
public class NBPackageUtils {
    
    public static Collection<JavaPackage> getSubPackages(Project project, JavaPackage pack) {
        SubPackagesSearcher searcher = new SubPackagesSearcher(project, pack);
        NBElementUtils.execute(searcher, project);
        
        return searcher.getSubPackages();
    }
 
    public static Collection<JavaClass> getClasses(Project project, 
            Function1<? super Name, Boolean> nameFilter, List<ElementHandle<PackageElement>> packages) {
        ClassesSearcher searcher = new ClassesSearcher(packages, project, nameFilter);
        NBElementUtils.execute(searcher, project);
        
        return searcher.getClasses();
    }
    
    public static FqName getFqName(Project project, ElementHandle<PackageElement> handle) {
        FqNameSearcher searcher = new FqNameSearcher(handle);
        NBElementUtils.execute(searcher, project);
        
        return searcher.getFqName();
    }
    
}
