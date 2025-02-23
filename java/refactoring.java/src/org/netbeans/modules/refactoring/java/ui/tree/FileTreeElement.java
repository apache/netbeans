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

package org.netbeans.modules.refactoring.java.ui.tree;

import java.beans.BeanInfo;
import java.net.URISyntaxException;
import java.net.URL;
import javax.swing.Icon;
import org.netbeans.api.actions.Openable;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.refactoring.spi.ui.TreeElement;
import org.netbeans.modules.refactoring.spi.ui.TreeElementFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Utilities;

/**
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
            if(FileUtil.isArchiveFile(fo)) {
                FileObject root = FileUtil.getArchiveRoot(fo);
                JavaPlatformManager manager = JavaPlatformManager.getDefault();
                for (JavaPlatform javaPlatform : manager.getInstalledPlatforms()) {
                    if(javaPlatform.getSourceFolders().contains(root) ||
                            javaPlatform.getStandardLibraries().contains(root) ||
                            javaPlatform.getBootstrapLibraries().contains(root)) {
                        return TreeElementFactory.getTreeElement(javaPlatform);
                    }
                }
//                Project p = FileOwnerQuery.getOwner(fo);
//                if(p != null) {
//                    return TreeElementFactory.getTreeElement(p);
//                }
                return null;
            } else {
                return TreeElementFactory.getTreeElement(fo.getParent());
            }
        } else {
            if(FileUtil.isArchiveFile(fo)) {
                FileObject root = FileUtil.getArchiveRoot(fo);
                JavaPlatformManager manager = JavaPlatformManager.getDefault();
                for (JavaPlatform javaPlatform : manager.getInstalledPlatforms()) {
                    if(javaPlatform.getSourceFolders().contains(root) ||
                            javaPlatform.getStandardLibraries().contains(root) ||
                            javaPlatform.getBootstrapLibraries().contains(root)) {
                        return TreeElementFactory.getTreeElement(javaPlatform);
                    }
                }
            }
            if(FileUtil.getArchiveFile(fo) != null) {
                return TreeElementFactory.getTreeElement(FileUtil.getArchiveFile(fo));
            } else if(FileUtil.isArchiveFile(fo)) {
                return null;
            }
            Project p = FileOwnerQuery.getOwner(fo);
            if(p != null) {
                return TreeElementFactory.getTreeElement(p);
            }
            Object orig = fo.getAttribute("orig-file");
            if(orig instanceof URL) {
                URL root = FileUtil.getArchiveFile((URL) orig);
                try {
                    FileObject arch = FileUtil.toFileObject(Utilities.toFile(root.toURI()));
                    return TreeElementFactory.getTreeElement(arch);
                } catch (URISyntaxException ex) {
                    return TreeElementFactory.getTreeElement(fo.getParent());
                }
            }
            return TreeElementFactory.getTreeElement(fo.getParent());
        }
    }

    @Override
    public Icon getIcon() {
        try {
            Icon icon = ImageUtilities.image2Icon(DataObject.find(fo).getNodeDelegate().getIcon(BeanInfo.ICON_COLOR_16x16));
            Boolean inTestFile = ElementGripFactory.getDefault().inTestFile(fo);
            if(Boolean.TRUE == inTestFile) {
                icon = ImageUtilities.mergeIcons(icon,
                    ImageUtilities.loadIcon("org/netbeans/modules/refactoring/java/resources/found_item_test.png"), 4, 4);
            }
            return icon;
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
            if(fo.isValid()) {
                DataObject od = DataObject.find(fo);
                NbDocument.openDocument(od, 0, Line.ShowOpenType.OPEN, Line.ShowVisibilityType.FOCUS);
            }
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
