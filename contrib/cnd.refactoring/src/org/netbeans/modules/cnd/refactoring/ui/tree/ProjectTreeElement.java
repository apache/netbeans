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

package org.netbeans.modules.cnd.refactoring.ui.tree;

import javax.swing.Icon;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.modelutil.CsmImageLoader;
import org.netbeans.modules.cnd.refactoring.support.CsmRefactoringUtils;
import org.netbeans.modules.refactoring.spi.ui.*;

/**
 * presentation of element for Project in C/C++ refactorings
 * 
 */
public class ProjectTreeElement implements TreeElement {

    private final String name;
    private final Icon icon;
    private final CsmUID<CsmProject> prjUID;
    public ProjectTreeElement(CsmProject csmPrj) {
        Object prj = csmPrj.getPlatformProject();
        if (prj instanceof NativeProject && (((NativeProject)prj).getProject() instanceof Project)) {
            Project p = (Project) ((NativeProject)prj).getProject();
            ProjectInformation pi = ProjectUtils.getInformation(p);
            this.name = pi.getDisplayName();
            this.icon = pi.getIcon();
        } else {
            this.icon = CsmImageLoader.getProjectIcon(csmPrj, false);
            this.name = csmPrj.getName().toString();
        }
        prjUID = CsmRefactoringUtils.getHandler(csmPrj);
    }
    
    @Override
    public TreeElement getParent(boolean isLogical) {
        return null;
    }

    @Override
    public Icon getIcon() {
        return icon;
    }

    @Override
    public String getText(boolean isLogical) {
        return name;
    }

    @Override
    public Object getUserObject() {
        return prjUID.getObject();
    }
    
}
