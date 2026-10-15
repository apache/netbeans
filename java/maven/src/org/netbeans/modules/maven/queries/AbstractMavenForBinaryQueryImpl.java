/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.netbeans.modules.maven.queries;

import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedHashSet;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.FileUtilities;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.spi.queries.JavaLikeRootProvider;
import org.netbeans.spi.java.queries.JavadocForBinaryQueryImplementation;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation2;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;


/**
 * SourceForBinary and JavadocForBinary query impls.
 * @author  Milos Kleint 
 */
abstract class AbstractMavenForBinaryQueryImpl implements SourceForBinaryQueryImplementation2,
        JavadocForBinaryQueryImplementation {
    
   
    protected AbstractMavenForBinaryQueryImpl() {
    }

    public @Override SourceForBinaryQuery.Result findSourceRoots(URL url) {
        return findSourceRoots2(url);
    }


    static @CheckForNull String jarify(@NullAllowed String path) { // #200088
        return path != null ? path.replaceFirst("[.][^./]+$", ".jar") : null;
    }
    
    static @NonNull FileObject[] getProjectSrcRoots(Project p) {
        NbMavenProjectImpl project = p.getLookup().lookup(NbMavenProjectImpl.class);
        Collection<FileObject> toReturn = new LinkedHashSet<FileObject>();
        for (String item : project.getOriginalMavenProject().getCompileSourceRoots()) {
            FileObject fo = FileUtilities.convertStringToFileObject(item);
            if (fo != null) {
                toReturn.add(fo);
            }
        }
        for (URI genRoot : project.getGeneratedSourceRoots(false)) {
            FileObject fo = FileUtilities.convertURItoFileObject(genRoot);
            if (fo != null) {
                toReturn.add(fo);
            }
        }
        for (JavaLikeRootProvider rp : project.getLookup().lookupAll(JavaLikeRootProvider.class)) {
            FileObject fo = project.getProjectDirectory().getFileObject("src/main/" + rp.kind());
            if (fo != null) {
                toReturn.add(fo);
            }
        }

        URI[] res = project.getResources(false);
        for (int i = 0; i < res.length; i++) {
            FileObject fo = FileUtilities.convertURItoFileObject(res[i]);
            if (fo != null) {
                boolean ok = true;
                //#166655 resource root cannot contain the real java/xxx roots
                for (FileObject form : toReturn) {
                    if (FileUtil.isParentOf(fo, form)) {
                        ok = false;
                        break;
                    }
                }
                if (ok) {
                    toReturn.add(fo);
                }
            }
        }
        return toReturn.toArray(new FileObject[0]);
    }
    
    static @NonNull FileObject[] getProjectTestSrcRoots(Project p) {
        NbMavenProjectImpl project = p.getLookup().lookup(NbMavenProjectImpl.class);
        Collection<FileObject> toReturn = new LinkedHashSet<FileObject>();
        for (String item : project.getOriginalMavenProject().getTestCompileSourceRoots()) {
            FileObject fo = FileUtilities.convertStringToFileObject(item);
            if (fo != null) {
                toReturn.add(fo);
            }
        }
        for (URI genRoot : project.getGeneratedSourceRoots(true)) {
            FileObject fo = FileUtilities.convertURItoFileObject(genRoot);
            if (fo != null) {
                toReturn.add(fo);
            }
        }
        for (JavaLikeRootProvider rp : project.getLookup().lookupAll(JavaLikeRootProvider.class)) {
            FileObject fo = project.getProjectDirectory().getFileObject("src/test/" + rp.kind());
            if (fo != null) {
                toReturn.add(fo);
            }
        }

        URI[] res = project.getResources(true);
        for (int i = 0; i < res.length; i++) {
            FileObject fo = FileUtilities.convertURItoFileObject(res[i]);
            if (fo != null) {
                boolean ok = true;
                //#166655 resource root cannot contain the real java/xxx roots
                for (FileObject form : toReturn) {
                    if (FileUtil.isParentOf(fo, form)) {
                        ok = false;
                        break;
                    }
                }
                if (ok) {
                    toReturn.add(fo);
                }
            }
        }
        return toReturn.toArray(new FileObject[0]);
    }
    
}