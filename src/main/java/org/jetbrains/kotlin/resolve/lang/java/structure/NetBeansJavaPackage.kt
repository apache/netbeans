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

import java.util.Collections
import javax.lang.model.element.PackageElement
import org.jetbrains.kotlin.load.java.structure.JavaElement
import org.jetbrains.kotlin.load.java.structure.JavaPackage
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.netbeans.api.project.Project
import org.jetbrains.kotlin.resolve.lang.java.*

/*

  @author Alexander.Baratynski
  Created on Aug 29, 2016
*/

class NetBeansJavaPackage(val packages: List<ElemHandle<PackageElement>>, val project: Project) :
        JavaPackage, JavaElement {

    constructor(pack: ElemHandle<PackageElement>, project: Project) : this(Collections.singletonList(pack), project)

    override val fqName: FqName
        get() = packages.first().getFqName(project)

    override val subPackages: Collection<JavaPackage>
        get() = getSubPackages(project)

    override fun getClasses(nameFilter: (Name) -> Boolean) =
            packages.getClasses(project, nameFilter)

}