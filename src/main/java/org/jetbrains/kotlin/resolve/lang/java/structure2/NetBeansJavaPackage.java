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
package org.jetbrains.kotlin.resolve.lang.java.structure2;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.PackageElement;
import kotlin.jvm.functions.Function1;
import org.jetbrains.kotlin.load.java.structure.JavaClass;
import org.jetbrains.kotlin.load.java.structure.JavaElement;
import org.jetbrains.kotlin.load.java.structure.JavaPackage;
import org.jetbrains.kotlin.name.FqName;
import org.jetbrains.kotlin.name.Name;
import org.jetbrains.kotlin.resolve.lang.java2.NBPackageUtils;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.project.Project;

/**
 *
 * @author Alexander.Baratynski
 */
public class NetBeansJavaPackage implements JavaPackage, JavaElement {

    private final List<ElementHandle<PackageElement>> packages = Lists.newArrayList();
    private final Project project;
    
    public NetBeansJavaPackage(List<ElementHandle<PackageElement>> packages, Project project){
        this.packages.addAll(packages);
        this.project = project; 
    }
    
    public NetBeansJavaPackage(ElementHandle<PackageElement> pack, Project project){
        this(Collections.singletonList(pack), project);
    }
    
    @Override
    public FqName getFqName() {
        return new FqName(packages.get(0).getQualifiedName());
    }

    @Override
    public Collection<JavaPackage> getSubPackages() {
        return NBPackageUtils.getSubPackages(project, this);
    }

    @Override
    public Collection<JavaClass> getClasses(Function1<? super Name, Boolean> nameFilter) {
        return NBPackageUtils.getClasses(project, nameFilter, packages);
    }
    
}
