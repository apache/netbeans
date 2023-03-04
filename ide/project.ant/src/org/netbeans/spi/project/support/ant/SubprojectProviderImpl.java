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

package org.netbeans.spi.project.support.ant;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.SubprojectProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Mutex;

/**
 * Translates a list of subproject names into actual subprojects
 * for an Ant-based project.
 * @author Jesse Glick
 */
final class SubprojectProviderImpl implements SubprojectProvider {
    
    private final ReferenceHelper helper;
    
    SubprojectProviderImpl(ReferenceHelper helper) {
        this.helper = helper;
    }
    
    public Set<? extends Project> getSubprojects() {
        return ProjectManager.mutex().readAccess(new Mutex.Action<Set<? extends Project>>() {
            public Set<? extends Project> run() {
                // XXX could use a special set w/ lazy isEmpty() - cf. #58639 for freeform
                Set<String> foreignProjectNames = new HashSet<String>();
                for (ReferenceHelper.RawReference ref : helper.getRawReferences()) {
                    foreignProjectNames.add(ref.getForeignProjectName());
                }
                Set<Project> foreignProjects = new HashSet<Project>();
                for (String foreignProjectName : foreignProjectNames) {
                    String prop = "project." + foreignProjectName; // NOI18N
                    AntProjectHelper h = helper.getAntProjectHelper();
                    String foreignProjectDirS = helper.eval.getProperty(prop);
                    if (foreignProjectDirS == null) {
                        // Missing for some reason. Skip it.
                        continue;
                    }
                    FileObject foreignProjectDir = h.resolveFileObject(foreignProjectDirS);
                    if (foreignProjectDir == null) {
                        // Not present on disk, erroneous property, etc. Skip it.
                        continue;
                    }
                    try {
                        Project p = ProjectManager.getDefault().findProject(foreignProjectDir);
                        if (p != null) {
                            // OK, got a real project.
                            foreignProjects.add(p);
                        }
                    } catch (IOException e) {
                        Logger.getLogger(this.getClass().getName()).log(Level.INFO, null, e);
                        // skip it
                    }
                }
                return foreignProjects;
            }
        });
    }
    
    public void addChangeListener(ChangeListener listener) {
        // XXX implement - listen to references added and removed
    }    
    
    public void removeChangeListener(ChangeListener listener) {
        // XXX
    }
    
}
