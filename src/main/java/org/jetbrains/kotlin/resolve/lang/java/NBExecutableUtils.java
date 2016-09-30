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

import java.util.List;
import javax.lang.model.element.ExecutableElement;
import org.jetbrains.kotlin.load.java.structure.JavaType;
import org.jetbrains.kotlin.load.java.structure.JavaTypeParameter;
import org.jetbrains.kotlin.load.java.structure.JavaValueParameter;
import org.jetbrains.kotlin.resolve.lang.java.ExecutableSearchers.HasAnnotationParameterDefaultValueSearcher;
import org.jetbrains.kotlin.resolve.lang.java.ExecutableSearchers.ReturnTypeSearcher;
import org.jetbrains.kotlin.resolve.lang.java.ExecutableSearchers.TypeParametersSearcher;
import org.jetbrains.kotlin.resolve.lang.java.ExecutableSearchers.ValueParametersSearcher;
import org.netbeans.api.project.Project;

/**
 *
 * @author Alexander.Baratynski
 */
public class NBExecutableUtils {
    
    public static JavaType getReturnType(ElemHandle handle, Project project) {
        ReturnTypeSearcher searcher = new ReturnTypeSearcher(handle, project);
        NBElementUtils.execute(searcher, project);
        
        return searcher.getReturnType();
    }
    
    public static boolean hasAnnotationParameterDefaultValue(ElemHandle handle, Project project) {
        HasAnnotationParameterDefaultValueSearcher searcher = new HasAnnotationParameterDefaultValueSearcher(handle);
        NBElementUtils.execute(searcher, project);
        
        return searcher.hasAnnotationParameterDefaultValue();
    }
    
    public static List<JavaTypeParameter> getTypeParameters(ElemHandle handle, Project project) {
        TypeParametersSearcher searcher = new TypeParametersSearcher(handle, project);
        NBElementUtils.execute(searcher, project);
        
        return searcher.getTypeParameters();
    }
    
    public static List<JavaValueParameter> getValueParameters(ElemHandle handle, Project project) {
        ValueParametersSearcher searcher = new ValueParametersSearcher(handle, project);
        NBElementUtils.execute(searcher, project);
        
        return searcher.getValueParameters();
    }
    
}
