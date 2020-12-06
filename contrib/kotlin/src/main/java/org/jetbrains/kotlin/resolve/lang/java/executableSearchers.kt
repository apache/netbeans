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

import javax.lang.model.element.ExecutableElement
import org.jetbrains.kotlin.load.java.structure.JavaType
import org.jetbrains.kotlin.load.java.structure.JavaTypeParameter
import org.jetbrains.kotlin.load.java.structure.JavaValueParameter
import org.jetbrains.kotlin.resolve.lang.java.structure.NetBeansJavaType
import org.jetbrains.kotlin.resolve.lang.java.structure.NetBeansJavaTypeParameter
import org.jetbrains.kotlin.resolve.lang.java.structure.NetBeansJavaValueParameter
import org.netbeans.api.java.source.CompilationController
import org.netbeans.api.java.source.ElementHandle
import org.netbeans.api.java.source.Task
import org.netbeans.api.java.source.TypeMirrorHandle
import org.netbeans.api.project.Project

class ReturnTypeSearcher(val handle: ElemHandle<ExecutableElement>,
                         val project: Project) : Task<CompilationController> {

    lateinit var returnType: JavaType 

    override fun run(info: CompilationController) {
        info.toResolvedPhase()
            
        val elem = handle.resolve(info)
        val typeHandle = TypeMirrorHandle.create((elem as ExecutableElement).returnType)
        returnType = NetBeansJavaType.create(typeHandle, project)
    }
}

class HasAnnotationParameterDefaultValueSearcher(val handle: ElemHandle<ExecutableElement>) : Task<CompilationController> {

    var hasAnnotationParameterDefaultValue = false

    override fun run(info: CompilationController) {
        info.toResolvedPhase()

        val elem = handle.resolve(info) ?: return
        hasAnnotationParameterDefaultValue = (elem as ExecutableElement).defaultValue != null
    }
}

class ExecutableTypeParametersSearcher(val handle: ElemHandle<ExecutableElement>,
                            val project: Project) : Task<CompilationController> {

    val typeParameters = arrayListOf<JavaTypeParameter>()

    override fun run(info: CompilationController) {
        info.toResolvedPhase()

        val elem = handle.resolve(info) ?: return
        val typeParams = (elem as ExecutableElement).typeParameters
                .map { NetBeansJavaTypeParameter(ElemHandle.create(it, project), project) }
        typeParameters.addAll(typeParams)
    }
}

class ValueParametersSearcher(val handle: ElemHandle<ExecutableElement>,
                              val project: Project) : Task<CompilationController> {
    
    val valueParameters = arrayListOf<JavaValueParameter>()
    
    override fun run(info: CompilationController) {
        info.toResolvedPhase()
        
        val elem = handle.resolve(info) ?: return
        val valueParams = (elem as ExecutableElement).parameters
        val parameterTypesCount = valueParams.size
        
        valueParams.forEachIndexed { index, it -> 
            val isLastParameter = index == parameterTypesCount - 1
            val parameterName = it.simpleName.toString()
            val elemHandle = ElemHandle.create(it, project)
            val valueParameter = NetBeansJavaValueParameter(elemHandle, project, parameterName,
                    if (isLastParameter) elem.isVarArgs else false)
            valueParameters.add(valueParameter)
        }
    }
}
  
class ElementHandleValueParametersSearcher(val handle: ElementHandle<ExecutableElement>,
                              val project: Project) : Task<CompilationController> {
    
    val valueParameters = arrayListOf<JavaValueParameter>()
    
    override fun run(info: CompilationController) {
        info.toResolvedPhase()
        
        val elem = handle.resolve(info) ?: return
        val valueParams = elem.parameters
        val parameterTypesCount = valueParams.size
        
        valueParams.forEachIndexed { index, it -> 
            val isLastParameter = index == parameterTypesCount - 1
            val parameterName = it.simpleName.toString()
            val elemHandle = ElemHandle.create(it, project)
            val valueParameter = NetBeansJavaValueParameter(elemHandle, project, parameterName,
                    if (isLastParameter) elem.isVarArgs else false)
            valueParameters.add(valueParameter)
        }
    }
}