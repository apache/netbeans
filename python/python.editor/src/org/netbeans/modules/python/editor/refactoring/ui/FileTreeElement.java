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
package org.netbeans.modules.python.editor.refactoring.ui;

import java.beans.BeanInfo;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.refactoring.spi.ui.*;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

public class FileTreeElement implements TreeElement {
    private FileObject fo;

    FileTreeElement(FileObject fo) {
        this.fo = fo;
    }

    @Override
    public TreeElement getParent(boolean isLogical) {
        if (isLogical) {
            return TreeElementFactory.getTreeElement(fo.getParent());
        } else {
            Project p = FileOwnerQuery.getOwner(fo);
            return TreeElementFactory.getTreeElement(p != null ? p : fo.getParent());
        }
    }

    @Override
    public Icon getIcon() {
        try {
            return new ImageIcon(DataObject.find(fo).getNodeDelegate().getIcon(BeanInfo.ICON_COLOR_16x16));
        } catch (DataObjectNotFoundException ex) {
            return null;
        }
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
