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

package org.netbeans.modules.apisupport.project.suite;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.api.Util;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.util.ChangeSupport;

/**
 * Lists modules in a suite.
 * @author Jesse Glick
 */
final class SuiteSubprojectProviderImpl implements SubprojectProvider {
    
    private Set<NbModuleProject> projects;
    private final AntProjectHelper helper;
    private final PropertyEvaluator eval;
    
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private boolean reloadNeeded;
    
    public SuiteSubprojectProviderImpl(AntProjectHelper helper, PropertyEvaluator eval) {
        this.helper = helper;
        this.eval = eval;
        eval.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if ("modules".equals(evt.getPropertyName())) { // NOI18N
                    SuiteSubprojectProviderImpl.this.reloadNeeded = true;
                    SuiteSubprojectProviderImpl.this.changeSupport.fireChange();
                }
            }
        });
    }
    
    public Set<NbModuleProject> getSubprojects() {
        if (projects == null || reloadNeeded) {
            reloadNeeded = false;   // must set before loadProjects so that prop. change is not lost when loading from another thread
            projects = loadProjects();
        }
        return projects;
    }
    
    public final void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }
    
    public final void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }
    
    private Set<NbModuleProject> loadProjects() {
        Set<NbModuleProject> newProjects = new HashSet<NbModuleProject>();
        String modules = eval.getProperty("modules"); // NOI18N
        if (modules != null) {
            for (String piece : PropertyUtils.tokenizePath(modules)) {
                FileObject dir = helper.resolveFileObject(piece);
                if (dir != null) {
                    try {
                        Project subp = ProjectManager.getDefault().findProject(dir);
                        if (subp instanceof NbModuleProject) {
                            newProjects.add((NbModuleProject) subp);
                        }
                    } catch (IOException e) {
                        Util.err.notify(ErrorManager.INFORMATIONAL, e);
                    }
                }
            }
        }
        return Collections.unmodifiableSet(newProjects);
    }
    
}
