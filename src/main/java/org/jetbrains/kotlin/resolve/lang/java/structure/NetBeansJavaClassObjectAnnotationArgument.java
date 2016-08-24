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
package org.jetbrains.kotlin.resolve.lang.java.structure;

import javax.lang.model.element.Element;
import org.jetbrains.kotlin.resolve.lang.java.NetBeansJavaClassFinder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.load.java.structure.JavaClassObjectAnnotationArgument;
import org.jetbrains.kotlin.load.java.structure.JavaType;
import org.jetbrains.kotlin.name.FqName;
import org.jetbrains.kotlin.name.Name;
import org.netbeans.api.project.Project;
/**
 *
 * @author Александр
 */
public class NetBeansJavaClassObjectAnnotationArgument implements JavaClassObjectAnnotationArgument {

    private final Class<?> javaClass;
    private final Project kotlinProject;
    private final Name name;
    
    protected NetBeansJavaClassObjectAnnotationArgument(Class<?> javaClass,
            @NotNull Name name, @NotNull Project project){
        this.javaClass = javaClass;
        this.kotlinProject = project;
        this.name = name;
    }
    
    @Override
    public JavaType getReferencedType() {
        return null;
//        Element typeBinding = NetBeansJavaClassFinder.findType(new FqName(javaClass.getCanonicalName()), kotlinProject);
//        assert typeBinding != null;
//        return NetBeansJavaType.create(typeBinding.asType());
    }

    @Override
    public Name getName() {
        return name;
    }
    
    
    
}
