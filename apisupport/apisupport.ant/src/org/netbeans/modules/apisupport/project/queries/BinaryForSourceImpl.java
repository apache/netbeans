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
package org.netbeans.modules.apisupport.project.queries;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.queries.BinaryForSourceQuery;
import org.netbeans.api.java.queries.BinaryForSourceQuery.Result;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.spi.java.queries.BinaryForSourceQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;
import org.openide.util.Utilities;

/**
 *
 * @author Tomas Zezula
 */
public class BinaryForSourceImpl implements BinaryForSourceQueryImplementation {
    
    private final NbModuleProject project;
    private final ConcurrentMap<URI, Result> cache;
    
    public BinaryForSourceImpl(@NonNull final NbModuleProject project) {
        Parameters.notNull("project", project); //NOI18N
        this.project = project;
        this.cache = new ConcurrentHashMap<URI, Result>();
    }

    @Override
    public Result findBinaryRoots(@NonNull final URL sourceRoot) {
        try {
            final URI key = sourceRoot.toURI();
            Result res = cache.get(key);
            if (res == null) {
                URL binRoot = null;
                
                //Try project sources
                final FileObject srcDir = project.getSourceDirectory();                
                if (srcDir != null && srcDir.toURI().equals(key)) {
                    binRoot = Utilities.toURI(project.getClassesDirectory()).toURL();
                }
                
                //Try project generated sources
                if (binRoot == null) {
                    final File genSrcDir = project.getGeneratedClassesDirectory();
                    if (genSrcDir != null && Utilities.toURI(genSrcDir).equals(key)) {
                        binRoot = Utilities.toURI(project.getClassesDirectory()).toURL();
                    }
                }
                
                //Try unit tests
                if (binRoot == null) {
                    for (final String testKind : project.supportedTestTypes()) {
                        final FileObject testSrcDir = project.getTestSourceDirectory(testKind);
                        if (testSrcDir != null && testSrcDir.toURI().equals(key)) {
                            binRoot = Utilities.toURI(project.getTestClassesDirectory(testKind)).toURL();
                            break;
                        }
                        final File testGenSrcDir = project.getTestGeneratedClassesDirectory(testKind);
                        if (testGenSrcDir != null && Utilities.toURI(testGenSrcDir).equals(key)) {
                            binRoot = Utilities.toURI(project.getTestClassesDirectory(testKind)).toURL();
                            break;
                        }
                    }
                }
                
                if (binRoot != null) {
                    res = new ResImpl(binRoot);
                    final Result oldRes = cache.putIfAbsent(key, res);
                    if (oldRes != null) {
                        res = oldRes;
                    }
                }
            }
            return res;
        } catch (MalformedURLException mue) {
            Exceptions.printStackTrace(mue);
        } catch (URISyntaxException use) {
            Exceptions.printStackTrace(use);
        }
        return null;
    }
    
    
    private class ResImpl implements BinaryForSourceQuery.Result {
        
        private final URL binDir;
        
        private ResImpl(
                @NonNull final URL binDir) {
            this.binDir = binDir;
        }

        @Override
        @NonNull
        public URL[] getRoots() {
            return new URL[] {binDir};
        }

        @Override
        public void addChangeListener(ChangeListener l) {
            //Not needed, do not suppose the source root to be changed in nbproject
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
            //Not needed, do not suppose the source root to be changed in nbproject
        }
        
    }
    
}
