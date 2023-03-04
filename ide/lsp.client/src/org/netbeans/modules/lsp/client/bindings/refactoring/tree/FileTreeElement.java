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

package org.netbeans.modules.lsp.client.bindings.refactoring.tree;

import java.beans.BeanInfo;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.actions.Openable;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.refactoring.spi.ui.TreeElement;
import org.netbeans.modules.refactoring.spi.ui.TreeElementFactory;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;

/**Copied from refactoring.java, and simplified.
 *
 * @author Jan Becicka
 */
public class FileTreeElement implements TreeElement, Openable {

    private FileObject fo;
    FileTreeElement(FileObject fo) {
        this.fo = fo;
    }

    @Override
    public TreeElement getParent(boolean isLogical) {
        if (isLogical) {
            Project p = FileOwnerQuery.getOwner(fo);
            if (p != null && p.getProjectDirectory() == fo.getParent()) {
                return TreeElementFactory.getTreeElement(p);
            }
            return TreeElementFactory.getTreeElement(fo.getParent());
        } else {
            Project p = FileOwnerQuery.getOwner(fo);
            if(p != null) {
                return TreeElementFactory.getTreeElement(p);
            }
            return TreeElementFactory.getTreeElement(fo.getParent());
        }
    }

    @Override
    public Icon getIcon() {
        try {
            ImageIcon imageIcon = new ImageIcon(DataObject.find(fo).getNodeDelegate().getIcon(BeanInfo.ICON_COLOR_16x16));
            return imageIcon;
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

    @Override
    public void open() {
        try {
            if(fo.isValid() && fo.isData()) {
                DataObject od = DataObject.find(fo);
                NbDocument.openDocument(od, 0, Line.ShowOpenType.OPEN, Line.ShowVisibilityType.FOCUS);
            }
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
