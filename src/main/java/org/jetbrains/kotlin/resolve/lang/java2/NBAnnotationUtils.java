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
import org.jetbrains.kotlin.load.java.structure.JavaAnnotation;
import org.jetbrains.kotlin.name.FqName;
import org.jetbrains.kotlin.resolve.lang.java2.AnnotationSearchers.AnnotationForTypeMirrorHandleSearcher;
import org.jetbrains.kotlin.resolve.lang.java2.AnnotationSearchers.AnnotationSearcher;
import org.jetbrains.kotlin.resolve.lang.java2.AnnotationSearchers.AnnotationsForTypeMirrorHandleSearcher;
import org.jetbrains.kotlin.resolve.lang.java2.AnnotationSearchers.AnnotationsSearcher;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.api.project.Project;

/**
 *
 * @author Alexander.Baratynski
 */
public class NBAnnotationUtils {
    
    public static Collection<JavaAnnotation> getAnnotations(ElementHandle handle, Project project) {
        AnnotationsSearcher searcher = new AnnotationsSearcher(handle, project);
        NBElementUtils.execute(searcher, project);
        
        return searcher.getAnnotations();
    }
    
    public static JavaAnnotation getAnnotation(ElementHandle handle, Project project, FqName fqName) {
        AnnotationSearcher searcher = new AnnotationSearcher(handle, project, fqName);
        NBElementUtils.execute(searcher, project);
        
        return searcher.getAnnotation();
    }
    
    public static Collection<JavaAnnotation> getAnnotations(TypeMirrorHandle handle, Project project) {
        AnnotationsForTypeMirrorHandleSearcher searcher = new AnnotationsForTypeMirrorHandleSearcher(handle, project);
        NBElementUtils.execute(searcher, project);
        
        return searcher.getAnnotations();
    }
    
    public static JavaAnnotation getAnnotation(TypeMirrorHandle handle, Project project, FqName fqName) {
        AnnotationForTypeMirrorHandleSearcher searcher = new AnnotationForTypeMirrorHandleSearcher(handle, project, fqName);
        NBElementUtils.execute(searcher, project);
        
        return searcher.getAnnotation();
    }
    
}
