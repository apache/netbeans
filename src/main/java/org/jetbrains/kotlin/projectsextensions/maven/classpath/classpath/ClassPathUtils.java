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
package org.jetbrains.kotlin.projectsextensions.maven.classpath.classpath;

import java.lang.reflect.Method;
import java.net.URI;
import org.netbeans.api.project.Project;
import org.openide.util.Exceptions;

public class ClassPathUtils {
    
    public static URI[] getSourceRoots(Project project, boolean test) {
        Class clazz = project.getClass();
        try {
            Method getSourceRoots = clazz.getMethod("getSourceRoots", boolean.class);
            return (URI[]) getSourceRoots.invoke(project, test);
        } catch (ReflectiveOperationException ex) {
            Exceptions.printStackTrace(ex);
        } 
        
        return new URI[0];
    }
    
    public static URI[] getGeneratedSourceRoots(Project project, boolean test) {
        Class clazz = project.getClass();
        try {
            Method getSourceRoots = clazz.getMethod("getGeneratedSourceRoots", boolean.class);
            return (URI[]) getSourceRoots.invoke(project, test);
        } catch (ReflectiveOperationException ex) {
            Exceptions.printStackTrace(ex);
        } 
        
        return new URI[0];
    }
    
    public static URI[] getResources(Project project, boolean test) {
        Class clazz = project.getClass();
        try {
            Method getResources = clazz.getMethod("getResources", boolean.class);
            return (URI[]) getResources.invoke(project, test);
        } catch (ReflectiveOperationException ex) {
            Exceptions.printStackTrace(ex);
        } 
        
        return new URI[0];
    }
    
    public static URI getWebAppDirectory(Project project) {
        Class clazz = project.getClass();
        try {
            Method getWebAppDirectory = clazz.getMethod("getWebAppDirectory");
            return (URI) getWebAppDirectory.invoke(project);
        } catch (ReflectiveOperationException ex) {
            Exceptions.printStackTrace(ex);
        } 
        
        return null;
    }
    
}
