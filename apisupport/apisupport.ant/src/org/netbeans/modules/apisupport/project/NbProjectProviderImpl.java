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

package org.netbeans.modules.apisupport.project;

import java.io.File;
import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.spi.NbProjectProvider;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.modules.apisupport.project.suite.SuiteType;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author mkozeny
 */
public class NbProjectProviderImpl implements NbProjectProvider{
    
    private final Project prj;

    NbProjectProviderImpl(Project prj) {
        this.prj = prj;
    }

    @Override
    public boolean isNbPlatformApplication() {
        NbModuleProject target = prj.getLookup().lookup(NbModuleProject.class);
        if(target != null && target.getModuleType() == NbModuleType.SUITE_COMPONENT) {
            File suiteDirectory = target.getLookup().lookup(SuiteProvider.class).getSuiteDirectory();
            if(suiteDirectory!=null) {
                FileObject suiteDirectoryFO = FileUtil.toFileObject(suiteDirectory);
                if(suiteDirectoryFO != null) {
                    try {
                        Project suitePrj = ProjectManager.getDefault().findProject(suiteDirectoryFO);
                        if(suitePrj != null) {
                            SuiteProject suiteProject = suitePrj.getLookup().lookup(SuiteProject.class);
                            if(suiteProject != null && suiteProject.getSuiteType() == SuiteType.APPLICATION) {
                                return true;
                            }
                        }
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    } catch (IllegalArgumentException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }
        return false;
    }
    
    public boolean isSuiteComponent() {
        NbModuleProject target = prj.getLookup().lookup(NbModuleProject.class);
        if(target != null && target.getModuleType() == NbModuleType.SUITE_COMPONENT) {
            return true;
        }
        return false;
    }
    
}
