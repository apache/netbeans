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
package org.jetbrains.kotlin.resolve.lang.java2;

import java.util.Collection;
import org.jetbrains.kotlin.load.java.structure.JavaClassifierType;
import org.jetbrains.kotlin.name.Name;
import org.jetbrains.kotlin.resolve.lang.java2.ParameterSearchers.TypeParameterNameSearcher;
import org.jetbrains.kotlin.resolve.lang.java2.ParameterSearchers.UpperBoundsSearcher;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.api.project.Project;

/**
 *
 * @author Alexander.Baratynski
 */
public class NBParameterUtils {
    
    public static Name getNameOfTypeParameter(TypeMirrorHandle handle, Project project) {
        TypeParameterNameSearcher searcher = new TypeParameterNameSearcher(handle);
        NBElementUtils.execute(searcher, project);
        
        return searcher.getName();
    }
    
    public static Collection<JavaClassifierType> getUpperBounds(TypeMirrorHandle handle, Project project) {
        UpperBoundsSearcher searcher = new UpperBoundsSearcher(handle, project);
        NBElementUtils.execute(searcher, project);
        
        return searcher.getUpperBounds();
    }
    
}
