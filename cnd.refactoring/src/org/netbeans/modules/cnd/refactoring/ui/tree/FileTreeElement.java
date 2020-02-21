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

import java.beans.BeanInfo;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.refactoring.support.CsmRefactoringUtils;
import org.netbeans.modules.refactoring.spi.ui.*;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
 * TreeElement to represent Files
 * 
 */
public class FileTreeElement implements TreeElement {

    private final FileObject fo;
    private final CsmUID<CsmProject> csmProject;
    private final Icon icon;
    FileTreeElement(FileObject fo, CsmFile csmFile) {
        this.fo = fo;
        Icon readIcon = null;
        try {
            readIcon = new ImageIcon(DataObject.find(fo).getNodeDelegate().getIcon(BeanInfo.ICON_COLOR_16x16));
        } catch (DataObjectNotFoundException ex) {
            readIcon = null;
        }
        this.icon = readIcon;
        this.csmProject = CsmRefactoringUtils.getHandler(csmFile.getProject());
    }

    @Override
    public TreeElement getParent(boolean isLogical) {
        CsmProject prj = csmProject.getObject();
        if (prj == null) {
            return null;
        } else {
            return TreeElementFactory.getTreeElement(prj);
        }
    }

    @Override
    public Icon getIcon() {
        return this.icon;
    }

    @Override
    public String getText(boolean isLogical) {
        return fo.getNameExt();
    }

    @Override
    public Object getUserObject() {
        return fo;
    }
}
