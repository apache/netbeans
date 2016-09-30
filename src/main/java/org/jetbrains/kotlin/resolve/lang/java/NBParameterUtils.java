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

import java.util.Collection;
import org.jetbrains.kotlin.load.java.structure.JavaClassifierType;
import org.jetbrains.kotlin.name.Name;
import org.jetbrains.kotlin.resolve.lang.java.ParameterSearchers.ElemHandleSearcher;
import org.jetbrains.kotlin.resolve.lang.java.ParameterSearchers.Equals;
import org.jetbrains.kotlin.resolve.lang.java.ParameterSearchers.TypeParameterHashCodeSearcher;
import org.jetbrains.kotlin.resolve.lang.java.ParameterSearchers.TypeParameterNameSearcher;
import org.jetbrains.kotlin.resolve.lang.java.ParameterSearchers.UpperBoundsSearcher;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.api.project.Project;

/**
 *
 * @author Alexander.Baratynski
 */
public class NBParameterUtils {
    
    public static Name getNameOfTypeParameter(ElemHandle handle, Project project) {
        TypeParameterNameSearcher searcher = new TypeParameterNameSearcher(handle);
        NBElementUtils.execute(searcher, project);
        
        return searcher.getName();
    }
    
    public static ElemHandle getElemHandleFromTypeMirrorHandle(
            TypeMirrorHandle typeHandle, Project project) {
        ElemHandleSearcher searcher = new ElemHandleSearcher(typeHandle, project);
        NBElementUtils.execute(searcher, project);
        
        return searcher.getElemHandle();
    }
    
    public static Collection<JavaClassifierType> getUpperBounds(ElemHandle handle, Project project) {
        UpperBoundsSearcher searcher = new UpperBoundsSearcher(handle, project);
        NBElementUtils.execute(searcher, project);
        
        return searcher.getUpperBounds();
    }
    
    public static int hashCode(TypeMirrorHandle handle, Project project) {
        TypeParameterHashCodeSearcher searcher = new TypeParameterHashCodeSearcher(handle);
        NBElementUtils.execute(searcher, project);
        
        return searcher.getHashCode();
    }
    
    public static boolean equals(TypeMirrorHandle handle1, TypeMirrorHandle handle2, Project project) {
        Equals searcher = new Equals(handle1, handle2);
        NBElementUtils.execute(searcher, project);
        
        return searcher.equals();
    }
        
}
