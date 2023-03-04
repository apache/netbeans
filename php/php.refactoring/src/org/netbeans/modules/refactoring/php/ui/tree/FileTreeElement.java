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
package org.netbeans.modules.refactoring.php.ui.tree;

import java.beans.BeanInfo;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.refactoring.spi.ui.TreeElement;
import org.netbeans.modules.refactoring.spi.ui.TreeElementFactory;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class FileTreeElement implements TreeElement {

    private final FileObject fileObject;

    FileTreeElement(FileObject fileObject) {
        this.fileObject = fileObject;
    }

    @Override
    public TreeElement getParent(boolean isLogical) {
        if (isLogical) {
            return TreeElementFactory.getTreeElement(fileObject.getParent());
        } else {
            Project project = FileOwnerQuery.getOwner(fileObject);
            return TreeElementFactory.getTreeElement(project != null ? project : fileObject.getParent());
        }
    }

    @Override
    public Icon getIcon() {
        Icon result = null;
        try {
            result = new ImageIcon(DataObject.find(fileObject).getNodeDelegate().getIcon(BeanInfo.ICON_COLOR_16x16));
        } catch (DataObjectNotFoundException ex) {
            //no-op
        }
        return result;
    }

    @Override
    public String getText(boolean isLogical) {
        return fileObject.getNameExt();
    }

    @Override
    public Object getUserObject() {
        return fileObject;
    }
}
