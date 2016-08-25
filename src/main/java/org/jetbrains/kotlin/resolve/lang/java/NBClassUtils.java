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
import javax.lang.model.element.TypeElement;
import org.jetbrains.kotlin.load.java.structure.JavaClass;
import org.jetbrains.kotlin.load.java.structure.JavaClassifierType;
import org.jetbrains.kotlin.load.java.structure.JavaConstructor;
import org.jetbrains.kotlin.load.java.structure.JavaField;
import org.jetbrains.kotlin.load.java.structure.JavaMethod;
import org.jetbrains.kotlin.load.java.structure.JavaTypeParameter;
import org.jetbrains.kotlin.name.Name;
import org.jetbrains.kotlin.resolve.lang.java.ClassSearchers.ConstructorsSearcher;
import org.jetbrains.kotlin.resolve.lang.java.ClassSearchers.FieldsSearcher;
import org.jetbrains.kotlin.resolve.lang.java.ClassSearchers.InnerClassesSearcher;
import org.jetbrains.kotlin.resolve.lang.java.ClassSearchers.MethodsSearcher;
import org.jetbrains.kotlin.resolve.lang.java.ClassSearchers.NameSearcher;
import org.jetbrains.kotlin.resolve.lang.java.ClassSearchers.OuterClassSearcher;
import org.jetbrains.kotlin.resolve.lang.java.ClassSearchers.SuperTypesSearcher;
import org.jetbrains.kotlin.resolve.lang.java.ClassSearchers.TypeParametersSearcher;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.project.Project;

/**
 *
 * @author Alexander.Baratynski
 */
public class NBClassUtils {
    
    public static Name getName(ElementHandle<TypeElement> handle, Project project) {
        NameSearcher searcher = new NameSearcher(handle);
        NBElementUtils.execute(searcher, project);
        
        return searcher.getName();
    }
    
    public static Collection<JavaClassifierType> getSuperTypes(ElementHandle<TypeElement> handle, Project project) {
        SuperTypesSearcher searcher = new SuperTypesSearcher(handle, project);
        NBElementUtils.execute(searcher, project);
        
        return searcher.getSuperTypes();
    }
    
    public static Collection<JavaClass> getInnerClasses(ElementHandle<TypeElement> handle, Project project) {
        InnerClassesSearcher searcher = new InnerClassesSearcher(handle, project);
        NBElementUtils.execute(searcher, project);
        
        return searcher.getInnerClasses();
    }
    
    public static JavaClass getOuterClass(ElementHandle<TypeElement> handle, Project project) {
        OuterClassSearcher searcher = new OuterClassSearcher(handle, project);
        NBElementUtils.execute(searcher, project);
        
        return searcher.getOuterClass();
    }
    
    public static Collection<JavaMethod> getMethods(ElementHandle<TypeElement> handle, Project project, JavaClass javaClass) {
        MethodsSearcher searcher = new MethodsSearcher(handle, project, javaClass);
        NBElementUtils.execute(searcher, project);
        
        return searcher.getMethods();
    }
    
    public static Collection<JavaConstructor> getConstructors(ElementHandle<TypeElement> handle, Project project, JavaClass javaClass) {
        ConstructorsSearcher searcher = new ConstructorsSearcher(handle, project, javaClass);
        NBElementUtils.execute(searcher, project);
        
        return searcher.getConstructors();
    }
    
    public static Collection<JavaField> getFields(ElementHandle<TypeElement> handle, Project project, JavaClass javaClass) {
        FieldsSearcher searcher = new FieldsSearcher(handle, project, javaClass);
        NBElementUtils.execute(searcher, project);
        
        return searcher.getFields();
    }
    
    public static List<JavaTypeParameter> getTypeParameters(ElementHandle<TypeElement> handle, Project project) {
        TypeParametersSearcher searcher = new TypeParametersSearcher(handle, project);
        NBElementUtils.execute(searcher, project);
        
        return searcher.getTypeParameters();
    }
    
}
