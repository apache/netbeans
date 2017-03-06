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
package org.jetbrains.kotlin.model

import com.intellij.codeInsight.NullableNotNullManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiModifierListOwner
import org.netbeans.api.project.Project

class KotlinNullableNotNullManager(private val javaProject: Project) : NullableNotNullManager() {
    
    init {
        setNotNulls("NotNull")
        setNullables("Nullable")
    }

    override fun hasHardcodedContracts(element: PsiElement) = false
    
    override fun isNotNull(owner: PsiModifierListOwner, checkBases: Boolean): Boolean {
        val notNullAnnotations = getNotNulls().toSet()
        return owner.getModifierList()?.getAnnotations()?.any { annotation ->
            annotation.getQualifiedName() in notNullAnnotations
        } ?: false
    }

    override fun isNullable(owner: PsiModifierListOwner, checkBases: Boolean) = !isNotNull(owner, checkBases)    
}