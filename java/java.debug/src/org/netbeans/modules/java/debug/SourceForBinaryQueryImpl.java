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
package org.netbeans.modules.java.debug;

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.queries.SourceForBinaryQuery.Result;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Mutex.Action;

/**
 *
 * @author Jan Lahoda
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation.class, position=1000)
public class SourceForBinaryQueryImpl implements SourceForBinaryQueryImplementation {

    private Map<URL,Reference<Result>> url2Result = new WeakHashMap<URL, Reference<Result>>();
    private Map<Result, URL> result2URL = new WeakHashMap<Result, URL>();
    
    public SourceForBinaryQueryImpl() {
    }
    
    public Result findSourceRoots(final URL binaryRoot) {
        return ProjectManager.mutex().readAccess(new Action<Result>() {
            public Result run() {
                return findSourceRootsImpl(binaryRoot);
            }
        });
    }

    private synchronized Result findSourceRootsImpl(URL binaryRoot) {
        Reference<Result> ref = url2Result.get(binaryRoot);
        Result r = ref != null ? ref.get() : null;

        if (r != null) {
            return r;
        }
        
        String binaryRootS = binaryRoot.toExternalForm();
        URL url = null;
        if (binaryRootS.startsWith("jar:file:")) { // NOI18N
            if ((url = checkForBinaryRoot(binaryRootS, "/libs.javacapi/external/nb-javac-api")) == null) { // NOI18N
                url = checkForBinaryRoot(binaryRootS, "/libs.javacimpl/external/nb-javac-impl"); // NOI18N
            }
            FileObject projectFO = url != null ? URLMapper.findFileObject(url) : null;
            if (projectFO != null) {
                try {
                    Project project = ProjectManager.getDefault().findProject(projectFO);
                    if (project != null) {
                        SourceGroup[] sourceGroups = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
                        final FileObject[] roots = new FileObject[sourceGroups.length];
                        for (int i = 0; i < sourceGroups.length; i++) {
                            roots[i] = sourceGroups[i].getRootFolder();
                        }
                        Result result = new Result() {
                            public FileObject[] getRoots() {
                                return roots;
                            }

                            public void addChangeListener(ChangeListener l) {
                            }

                            public void removeChangeListener(ChangeListener l) {
                            }
                        };

                        url2Result.put(binaryRoot, new WeakReference<Result>(result));
                        result2URL.put(result, binaryRoot);

                        return result;
                    }
                } catch (IOException ex) {
                    Logger.getLogger(SourceForBinaryQueryImpl.class.getName()).log(Level.FINE, null, ex);
                } catch (IllegalArgumentException ex) {
                    Logger.getLogger(SourceForBinaryQueryImpl.class.getName()).log(Level.FINE, null, ex);
                }                
            }
        }
        return null;
    }

    private URL checkForBinaryRoot(String ext, String prefix) {
        if (ext.endsWith(prefix + ".jar!/")) { // NOI18N
            try {
                String part = ext.substring("jar:".length(), ext.length() - prefix.length() - ".jar!/".length()); // NOI18N                
                return new URL(part + "/nb-javac/make/netbeans/nb-javac"); // NOI18N
            } catch (MalformedURLException ex) {
                Logger.getLogger("global").log(Level.INFO, null, ex); //NOI18N
            }
        }
        
        return null;
    }
    
}
