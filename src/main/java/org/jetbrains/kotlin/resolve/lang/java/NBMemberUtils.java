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

import org.jetbrains.kotlin.descriptors.Visibility;
import org.jetbrains.kotlin.load.java.structure.JavaType;
import org.jetbrains.kotlin.name.Name;
import org.jetbrains.kotlin.resolve.lang.java.MemberSearchers.FieldContainingClassSearcher;
import org.jetbrains.kotlin.resolve.lang.java.MemberSearchers.FieldTypeSearcher;
import org.jetbrains.kotlin.resolve.lang.java.MemberSearchers.IsAbstractSearcher;
import org.jetbrains.kotlin.resolve.lang.java.MemberSearchers.IsFinalSearcher;
import org.jetbrains.kotlin.resolve.lang.java.MemberSearchers.IsStaticSearcher;
import org.jetbrains.kotlin.resolve.lang.java.MemberSearchers.NameSearcher;
import org.jetbrains.kotlin.resolve.lang.java.MemberSearchers.VisibilitySearcher;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.project.Project;

/**
 *
 * @author Alexander.Baratynski
 */
public class NBMemberUtils {
    
    public static boolean isAbstract(ElementHandle handle, Project project) {
        IsAbstractSearcher searcher = new IsAbstractSearcher(handle);
        NBElementUtils.execute(searcher, project);
        
        return searcher.isAbstract();
    }
    
    public static boolean isStatic(ElementHandle handle, Project project) {
        IsStaticSearcher searcher = new IsStaticSearcher(handle);
        NBElementUtils.execute(searcher, project);
        
        return searcher.isStatic();
    }
    
    public static boolean isFinal(ElementHandle handle, Project project) {
        IsFinalSearcher searcher = new IsFinalSearcher(handle);
        NBElementUtils.execute(searcher, project);
        
        return searcher.isFinal();
    }
    
    public static Name getName(ElementHandle handle, Project project) {
        NameSearcher searcher = new NameSearcher(handle);
        NBElementUtils.execute(searcher, project);
        
        return searcher.getName();
    }
    
    public static Visibility getVisibility(ElementHandle handle, Project project) {
        VisibilitySearcher searcher = new VisibilitySearcher(handle);
        NBElementUtils.execute(searcher, project);
        
        return searcher.getVisibility();
    }
    
    public static JavaType getFieldType(ElementHandle handle, Project project) {
        FieldTypeSearcher searcher = new FieldTypeSearcher(handle, project);
        NBElementUtils.execute(searcher, project);
        
        return searcher.getType();
    }
    
    public static ElementHandle getContainingClass(ElementHandle handle, Project project) {
        FieldContainingClassSearcher searcher = new FieldContainingClassSearcher(handle);
        NBElementUtils.execute(searcher, project);
        
        return searcher.getContainingClass();
    }
    
}
