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
package org.netbeans.modules.spring.api.beans.model;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.openide.filesystems.FileObject;

/**
 * Holds compile, boot and source paths of project.
 * 
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class ModelUnit {

    private final ClasspathInfo classPathInfo;
    private final ClassPath bootPath;
    private final ClassPath compilePath;
    private final ClassPath sourcePath;

    private ModelUnit(ClassPath bootPath, ClassPath compilePath, ClassPath sourcePath) {
        this.bootPath = bootPath;
        this.compilePath = compilePath;
        this.sourcePath = sourcePath;
        this.classPathInfo = ClasspathInfo.create(bootPath, compilePath, sourcePath);
    }


    public ClassPath getBootPath() {
        return bootPath;
    }

    public ClassPath getCompilePath() {
        return compilePath;
    }

    public ClassPath getSourcePath() {
        return sourcePath;
    }
    
    public ClasspathInfo getClassPathInfo() {
        return classPathInfo;
    }

    public static ModelUnit create(ClassPath bootPath, ClassPath compilePath, ClassPath sourcePath) {
        return new ModelUnit(bootPath, compilePath, sourcePath);
    }

    FileObject getSourceFileObject() {
        FileObject[] roots = sourcePath.getRoots();
        if (roots != null && roots.length > 0) {
            return roots[0];
        }
        return null;
    }

    /**
     * Returns hashCode computed from {@link ModelUnit}'s {@link ClassPath}'s entries.
     */
    @Override
    public int hashCode() {       
        return computeModelUnitHash(this);
    }
    
    /**
     * Equivalent state is based on the same {@link ClassPath}s 
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ModelUnit) {
            ModelUnit unit = (ModelUnit) obj;
            return hasClassPathesIdenticEntries(unit.getBootPath(), bootPath)
                    && hasClassPathesIdenticEntries(unit.compilePath, compilePath)
                    && hasClassPathesIdenticEntries(unit.sourcePath, sourcePath);
        } else {
            return false;
        }
    }
    
    private static boolean hasClassPathesIdenticEntries(ClassPath cp1, ClassPath cp2) {
        if (cp1.entries().size() != cp2.entries().size()) {
            return false;
        }
        for (int i = 0; i < cp1.entries().size(); i++) {
            if (!cp1.entries().get(i).getURL().sameFile(cp2.entries().get(i).getURL())) {
                return false;
            }
        }
        return true;
    }
    
    private static int computeModelUnitHash(ModelUnit unit) {
        return computeClassPathHash(unit.getBootPath()) + computeClassPathHash(unit.getCompilePath()) +
                computeClassPathHash(unit.getSourcePath());
    }
     
    private static int computeClassPathHash(ClassPath classPath) {
        int hashValue = 0;
        for (ClassPath.Entry entry : classPath.entries()) {
            hashValue += entry.getURL().getPath().hashCode();
        }
        return hashValue;
    }
}
