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
import org.jetbrains.kotlin.load.java.structure.JavaType;
import org.jetbrains.kotlin.resolve.lang.java.TypeSearchers.BoundSearcher;
import org.jetbrains.kotlin.resolve.lang.java.TypeSearchers.ComponentTypeSearcher;
import org.jetbrains.kotlin.resolve.lang.java.TypeSearchers.IsExtendsSearcher;
import org.jetbrains.kotlin.resolve.lang.java.TypeSearchers.IsRawSearcher;
import org.jetbrains.kotlin.resolve.lang.java.TypeSearchers.TypeArgumentsSearcher;
import org.jetbrains.kotlin.resolve.lang.java.TypeSearchers.TypeNameSearcher;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.api.project.Project;

/**
 *
 * @author Alexander.Baratynski
 */
public class NBTypeUtils {
    
    public static String getName(TypeMirrorHandle handle, Project project) {
        TypeNameSearcher searcher = new TypeNameSearcher(handle);
        NBElementUtils.execute(searcher, project);
        
        return searcher.getName();
    }
    
    public static JavaType getBound(TypeMirrorHandle handle, Project project) {
        BoundSearcher searcher = new BoundSearcher(handle, project);
        NBElementUtils.execute(searcher, project);
        
        return searcher.getBound();
    }
    
    public static boolean isExtends(TypeMirrorHandle handle, Project project) {
        IsExtendsSearcher searcher = new IsExtendsSearcher(handle);
        NBElementUtils.execute(searcher, project);
        
        return searcher.isExtends();
    }
    
    public static JavaType getComponentType(TypeMirrorHandle handle, Project project) {
        ComponentTypeSearcher searcher = new ComponentTypeSearcher(handle, project);
        NBElementUtils.execute(searcher, project);
        
        return searcher.getComponentType();
    }
    
    public static boolean isRaw(TypeMirrorHandle handle, Project project) {
        IsRawSearcher searcher = new IsRawSearcher(handle);
        NBElementUtils.execute(searcher, project);
        
        return searcher.isRaw();
    }
    
    public static List<JavaType> getTypeArguments(TypeMirrorHandle handle, Project project) {
        TypeArgumentsSearcher searcher = new TypeArgumentsSearcher(handle, project);
        NBElementUtils.execute(searcher, project);
        
        return searcher.getTypeArguments();
    }
    
}
