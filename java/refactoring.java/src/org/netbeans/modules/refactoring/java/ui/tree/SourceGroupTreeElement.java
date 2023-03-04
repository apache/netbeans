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

import java.awt.Image;
import java.beans.BeanInfo;
import java.lang.ref.WeakReference;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.refactoring.spi.ui.TreeElement;
import org.netbeans.modules.refactoring.spi.ui.TreeElementFactory;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Jan Becicka
 */
public class SourceGroupTreeElement implements TreeElement {
    
    private WeakReference<SourceGroup> sg;
    private FileObject dir;
    private Icon icon;
    private String displayName;
    
    private static String PACKAGE_BADGE = "org/netbeans/spi/java/project/support/ui/packageBadge.gif"; // NOI18N

    SourceGroupTreeElement(SourceGroup sg) {
        this.sg = new WeakReference<SourceGroup>(sg);
        dir = sg.getRootFolder();
 
        icon = sg.getIcon(false);
        if ( icon == null ) {
            try {
                Image image = DataObject.find(sg.getRootFolder()).getNodeDelegate().getIcon(BeanInfo.ICON_COLOR_16x16);
                image = ImageUtilities.mergeImages( image, ImageUtilities.loadImage(PACKAGE_BADGE), 7, 7 );
                icon = new ImageIcon(image);
            } catch (DataObjectNotFoundException d) {
            }
        }
        displayName = sg.getDisplayName();
    }

    @Override
    public TreeElement getParent(boolean isLogical) {
        return TreeElementFactory.getTreeElement(FileOwnerQuery.getOwner(dir));
    }

    @Override
    public Icon getIcon() {
        return icon;
    }

    @Override
    public String getText(boolean isLogical) {
        return displayName;
    }

    @Override
    public Object getUserObject() {
        SourceGroup s = sg.get();
        if (s==null) {
            s = FolderTreeElement.getSourceGroup(dir);
        }
        return s;
    }
}

