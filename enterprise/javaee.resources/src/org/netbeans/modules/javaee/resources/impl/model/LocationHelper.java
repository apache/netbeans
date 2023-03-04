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
package org.netbeans.modules.javaee.resources.impl.model;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.javaee.resources.api.model.Location;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public final class LocationHelper {

    private LocationHelper() {
    }

    /**
     * Obtains class location.
     * @param helper helper with classpaths where to search
     * @param fqn FQN name of the searched class
     * @return location of the class
     */
    public static Location getClassLocation(AnnotationModelHelper helper, String fqn) {
        String cpBase = fqn.replace('.', '/'); //NOI18N
        ClassPath sourceCP = helper.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.SOURCE);
        FileObject classFO = sourceCP.findResource(cpBase + ".java"); //NOI18N

        if (classFO == null) {
            ClassPath compileCP = helper.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.COMPILE);
            classFO = searchForFile(compileCP, cpBase);
        }

        if (classFO != null) {
            return new LocationImpl(classFO);
        }
        return null;
    }

    private static FileObject searchForFile(ClassPath cp, String cpBase) {
        FileObject file = getFileFromClasspath(cp, cpBase + ".java"); //NOI18N
        if (file == null) {
            return getFileFromClasspath(cp, cpBase + ".class"); //NOI18N
        } else {
            return file;
        }
    }

    private static FileObject getFileFromClasspath(ClassPath cp, String classRelativePath) {
        for (ClassPath.Entry entry : cp.entries()) {
            FileObject[] roots;
            if (entry.isValid()) {
                roots = new FileObject[]{entry.getRoot()};
            } else {
                SourceForBinaryQuery.Result res = SourceForBinaryQuery.findSourceRoots(entry.getURL());
                roots = res.getRoots();
            }
            for (FileObject root : roots) {
                FileObject metaInf = root.getFileObject(classRelativePath);
                if (metaInf != null) {
                    return metaInf;
                }
            }
        }

        return null;
    }

}
