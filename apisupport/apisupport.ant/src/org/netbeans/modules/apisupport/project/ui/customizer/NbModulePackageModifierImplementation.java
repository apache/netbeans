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
package org.netbeans.modules.apisupport.project.ui.customizer;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.ProjectXMLManager;
import org.netbeans.modules.apisupport.project.api.ManifestManager;
import org.netbeans.modules.java.api.common.ant.PackageModifierImplementation;
import org.openide.ErrorManager;
import org.openide.util.Mutex;
import org.openide.util.MutexException;

/**
 *
 * @author mkozeny
 */

//@ProjectServiceProvider(service = PackageModifierImplementation.class)
public final class NbModulePackageModifierImplementation implements PackageModifierImplementation {

    private final NbModuleProject project;
    
    /**
     * Constructor
     * @param project 
     */
    public NbModulePackageModifierImplementation(NbModuleProject project) {
        this.project = project;
    }

    @Override
    public void exportPackageAction(final Collection<String> packagesToExport, final boolean export) {
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                @Override
                public Void run() throws IOException {
                    ProjectXMLManager projectXMLManager = ProjectXMLManager.getInstance(project.getProjectDirectoryFile());
                    ManifestManager.PackageExport[] previousPubs = projectXMLManager.getPublicPackages();

                    String[] friends = projectXMLManager.getFriends();
                    Set<String> newPacks = new TreeSet<String>();
                    for (ManifestManager.PackageExport exp : previousPubs) {
                        newPacks.add(exp.getPackage() + (exp.isRecursive() ? ".*" : ""));
                    }
                    for (String newP : packagesToExport) {
                        if (export) {
                            newPacks.add(newP);
                        } else {
                            boolean removed = newPacks.remove(newP);
                            //TODO if not removed we have a problem, the package falls under "subpackages" element.
                            //but I suppose subpackages is rare these days.
                        }
                    }
                    if (friends == null) {
                        projectXMLManager.replacePublicPackages(newPacks);
                    } else {
                        projectXMLManager.replaceFriends(new TreeSet<String>(Arrays.asList(friends)), newPacks);
                    }
                    ProjectManager.getDefault().saveProject(project);
                    return null;
                }
            });
        } catch (MutexException e) {
            ErrorManager.getDefault().notify((IOException) e.getException());
        }
    }

}
