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
package org.black.kotlin.model;

import com.intellij.codeInsight.NullableNotNullManager;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiModifierListOwner;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.netbeans.api.project.Project;

/**
 *
 * @author Александр
 */
public class KotlinNullableNotNullManager extends NullableNotNullManager {
    
    Project javaProject;
    
    public KotlinNullableNotNullManager(Project javaProject){
        this.javaProject = javaProject;
        setNotNulls("NotNull");
        setNullables("Nullable");
    }
    
    @Override
    public boolean hasHardcodedContracts(PsiElement element){
        return false;
    }
    
    @Override
    public boolean isNotNull(@NotNull PsiModifierListOwner owner, boolean checkBases){
        List<String> notNullAnnotations = getNotNulls();
        PsiAnnotation[] annotations = owner.getModifierList().getAnnotations();
        for (PsiAnnotation an : annotations){
            if (notNullAnnotations.contains(an.getQualifiedName())){
                return true;
            }
        }
        return false;
    }


    @Override
    public boolean isNullable(@NotNull PsiModifierListOwner owner, boolean checkBases){
        return !isNotNull(owner,checkBases);
    }
    
}
