/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.web.beans.api.model;

import java.net.URISyntaxException;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;


/**
 * @author ads
 *
 */
public class ModelUnit {
    
    private ModelUnit( ClassPath bootPath, ClassPath compilePath, 
            ClassPath sourcePath, Project project)
    {
        myBootPath= bootPath;
        myCompilePath = compilePath;
        mySourcePath = sourcePath;
        myProject = project;
        myClassPathInfo = ClasspathInfo.create(bootPath, 
                compilePath, sourcePath);
    }
    
    public ClassPath getBootPath() {
        return myBootPath;
    }

    public ClassPath getCompilePath() {
        return myCompilePath;
    }

    public ClassPath getSourcePath() {
        return mySourcePath;
    }

    public Project getProject() {
        return myProject;
    }
    
    @Override
    public int hashCode() {       
        return 37*(37*myBootPath.hashCode() + myCompilePath.hashCode()) 
            +mySourcePath.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ModelUnit) {
            ModelUnit unit = (ModelUnit) obj;
            return myBootPath.equals( unit.myBootPath ) && myCompilePath.equals(
                    unit.myCompilePath ) && mySourcePath.equals( mySourcePath );
        } 
        else {
            return false;
        }
    }

    public static ModelUnit create(ClassPath bootPath, ClassPath compilePath, 
            ClassPath sourcePath, Project project)
    {
        return new ModelUnit(bootPath, compilePath, sourcePath, project);
    }
    
    public ClasspathInfo getClassPathInfo(){
        return myClassPathInfo;
    }
    
    private static boolean equals(ClassPath cp1, ClassPath cp2) {
        if (cp1.entries().size() != cp2.entries().size()) {
            return false;
        }
        for (int i = 0; i < cp1.entries().size(); i++) {
            try {
                if (!cp1.entries().get(i).getURL().toURI()
                        .equals(cp2.entries().get(i).getURL().toURI()))
                {
                    return false;
                }
            }
            catch (URISyntaxException e) {
                if ( !cp1.entries().get(i).equals(cp2.entries().get(i)) ){
                    return false;
                }
            }
        }
        return true;
    }

    private static int computeClassPathHash(ClassPath classPath) {
        int hashCode = 0;
        for (ClassPath.Entry entry : classPath.entries()) {
            hashCode = 37*hashCode + entry.getURL().getPath().hashCode();
        }
        return hashCode;
    }
    
    FileObject getSourceFileObject(){
        FileObject[] roots = mySourcePath.getRoots();
        if ( roots!= null && roots.length >0 ){
            return roots[0];
        }
        return null;
    }
    
    private final ClasspathInfo myClassPathInfo;
    private final ClassPath myBootPath;
    private final ClassPath myCompilePath;
    private final ClassPath mySourcePath;
    private final Project myProject;
    
}
