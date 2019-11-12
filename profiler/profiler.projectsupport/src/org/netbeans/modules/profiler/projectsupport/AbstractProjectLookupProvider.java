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

package org.netbeans.modules.profiler.projectsupport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.DataFilesProviderImplementation;
import org.netbeans.spi.project.LookupProvider;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Jaroslav Bachorik
 */
public abstract class AbstractProjectLookupProvider implements LookupProvider {
    private DataFilesProviderImplementation getDataFilesProviderImplementation(final Project project) {
        return new DataFilesProviderImplementation() {
                public List<FileObject> getMetadataFiles() {
                    List<FileObject> metadataFilesList = new ArrayList<FileObject>();
                    FileObject buildBackupFile = (project == null) ? null
                                                                   : project.getProjectDirectory()
                                                                            .getFileObject("build-before-profiler.xml"); // NOI18N

                    if ((buildBackupFile != null) && buildBackupFile.isValid()) {
                        metadataFilesList.add(buildBackupFile);
                    }

                    return metadataFilesList;
                }

                public List<FileObject> getDataFiles() {
                    return Collections.<FileObject>emptyList();
                }
            };
    }
    
    protected abstract List getAdditionalLookups(Project project);
    
    public Lookup createAdditionalLookup(Lookup baseContext) {
        List lookUps = new ArrayList();
        Project project = baseContext.lookup(Project.class);
        lookUps.add(getDataFilesProviderImplementation(project));
        lookUps.addAll(getAdditionalLookups(project));
        return Lookups.fixed(lookUps.toArray());
    }
}
