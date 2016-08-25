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

import java.util.Collection;
import org.jetbrains.kotlin.load.java.structure.JavaAnnotation;
import org.jetbrains.kotlin.load.java.structure.JavaAnnotationArgument;
import org.jetbrains.kotlin.load.java.structure.JavaAnnotationAsAnnotationArgument;
import org.jetbrains.kotlin.name.Name;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.api.project.Project;

/**
 *
 * @author Alexander.Baratynski
 */
public class NetBeansJavaAnnotationAsAnnotationArgument  implements JavaAnnotationAsAnnotationArgument {

    private final Collection<JavaAnnotationArgument> args;
    private final Project project;
    private final Name name;
    private final TypeMirrorHandle typeHandle;
    
    public NetBeansJavaAnnotationAsAnnotationArgument(Project project, Name name, TypeMirrorHandle typeHandle, Collection<JavaAnnotationArgument> args) {
        this.project = project;
        this.name = name;
        this.typeHandle = typeHandle;
        this.args = args;
    }
    
    @Override
    public JavaAnnotation getAnnotation() {
        return new NetBeansJavaAnnotation(project, typeHandle, args);
    }

    @Override
    public Name getName() {
        return name;
    }
    
}