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

import org.jetbrains.kotlin.builtins.PrimitiveType
import org.jetbrains.kotlin.load.java.structure.JavaPrimitiveType
import org.jetbrains.kotlin.resolve.jvm.JvmPrimitiveType
import org.jetbrains.kotlin.resolve.lang.java.NBTypeUtils
import org.netbeans.api.java.source.TypeMirrorHandle
import org.netbeans.api.project.Project
import javax.lang.model.type.TypeMirror

/*

  @author Alexander.Baratynski
  Created on Sep 7, 2016
*/

class NetBeansJavaPrimitiveType(handle : TypeMirrorHandle<*>, project : Project) : 
        NetBeansJavaType(handle, project), JavaPrimitiveType {

    override val type : PrimitiveType? 
        get() {
            val text = NBTypeUtils.getName(handle, project)
            return if ("void".equals(text)) null else JvmPrimitiveType.get(text).primitiveType
        }
    
}