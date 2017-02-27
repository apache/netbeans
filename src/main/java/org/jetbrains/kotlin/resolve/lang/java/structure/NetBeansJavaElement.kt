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

import org.jetbrains.kotlin.load.java.structure.JavaElement
import org.netbeans.api.project.Project
import org.jetbrains.kotlin.resolve.lang.java.*
import javax.lang.model.element.Element

/*

  @author Alexander.Baratynski
  Created on Aug 29, 2016
*/

abstract class NetBeansJavaElement<T : Element>(val elementHandle: ElemHandle<T>,
                                                val project: Project) : JavaElement {

    override fun hashCode() = elementHandle.hashCode()

    override fun equals(other: Any?): Boolean {
        if (other !is NetBeansJavaElement<*>) return false
        return elementHandle.equals(other.elementHandle)
    }

}