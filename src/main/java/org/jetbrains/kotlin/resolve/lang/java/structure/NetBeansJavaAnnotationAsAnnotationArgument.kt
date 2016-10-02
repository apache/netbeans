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
package org.jetbrains.kotlin.resolve.lang.java.structure

import org.jetbrains.kotlin.load.java.structure.JavaAnnotation
import org.jetbrains.kotlin.load.java.structure.JavaAnnotationArgument
import org.jetbrains.kotlin.load.java.structure.JavaAnnotationAsAnnotationArgument
import org.jetbrains.kotlin.name.Name
import org.netbeans.api.java.source.TypeMirrorHandle
import org.netbeans.api.project.Project
import javax.lang.model.type.DeclaredType

/*

  @author Alexander.Baratynski
  Created on Sep 7, 2016
*/

class NetBeansJavaAnnotationAsAnnotationArgument(val project : Project, name : Name, 
                                                 val typeHandle : TypeMirrorHandle<DeclaredType>, 
                                                 val args : Collection<JavaAnnotationArgument>) : 
        JavaAnnotationAsAnnotationArgument {
    override val name : Name = name
    override fun getAnnotation() : JavaAnnotation = NetBeansJavaAnnotation(project, typeHandle, args)
    
}