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

package org.netbeans.modules.refactoring.java.ui.tree;

import java.lang.ref.WeakReference;
import javax.swing.Icon;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.refactoring.spi.ui.TreeElement;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Jan Becicka
 */
public class ProjectTreeElement implements TreeElement {

    private String name;
    private Icon icon;
    private WeakReference<Project> prj;
    private FileObject prjDir;
    /** Creates a new instance of ProjectTreeElement */
    public ProjectTreeElement(Project prj) {
        ProjectInformation pi = ProjectUtils.getInformation(prj);
        name = pi.getDisplayName();
        icon = pi.getIcon();
        this.prj = new WeakReference<Project>(prj);
        prjDir = prj.getProjectDirectory();
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
        Project p = prj.get();
        if (p==null) {
            p = FileOwnerQuery.getOwner(prjDir);
        }
        return p;
    }
    
}
