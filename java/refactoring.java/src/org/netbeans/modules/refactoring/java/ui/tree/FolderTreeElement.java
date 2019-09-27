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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import javax.lang.model.element.ElementKind;
import javax.swing.Icon;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.ui.ElementIcons;
import org.netbeans.api.project.*;
import org.netbeans.modules.refactoring.spi.ui.TreeElement;
import org.netbeans.modules.refactoring.spi.ui.TreeElementFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Becicka
 */
public final class FolderTreeElement implements TreeElement {
    
    private final FileObject fo;
    FolderTreeElement(FileObject fo) {
        this.fo = fo;
    }
        
    
    @Override
    public TreeElement getParent(boolean isLogical) {
        if (isLogical) {
            SourceGroup sg = getSourceGroup(fo);
            if (sg!=null) {
                return TreeElementFactory.getTreeElement(sg);
            } else {
                FileObject arch = FileUtil.getArchiveFile(fo);
                if(arch != null) {
                    return TreeElementFactory.getTreeElement(arch);
                }
                return null;
            }
        } else {
            Project p = FileOwnerQuery.getOwner(fo);
            if (p!=null) {
                return TreeElementFactory.getTreeElement(p);
            } else {
                FileObject arch = FileUtil.getArchiveFile(fo);
                if(arch != null) {
                    return TreeElementFactory.getTreeElement(arch);
                }
                return null;
            }
        }
    }

    @Override
    public Icon getIcon() {
        return ElementIcons.getElementIcon(ElementKind.PACKAGE, null);
    }

    @Override
    public String getText(boolean isLogical) {
        ClassPath cp = ClassPath.getClassPath(fo, ClassPath.SOURCE);
        if (cp==null) {
            return fo.getPath();
        } else {
            if (getJavaSourceGroup(fo)!=null) {
                String resourceName = cp.getResourceName(fo);
                if (resourceName == null) {
                    return fo.getPath();
                }
                String name = resourceName.replace('/','.');
                if ("".equals(name)) {
                    return NbBundle.getMessage(FolderTreeElement.class, "LBL_DefaultPackage_PDU");
                }
                return name;
            } else {
                return fo.getPath();
            }
        }
    }

    static SourceGroup getSourceGroup(FileObject file) {
        Project prj = FileOwnerQuery.getOwner(file);
        if (prj == null) {
            return null;
        }
        Sources src = ProjectUtils.getSources(prj);
        List<SourceGroup> allgroups = new ArrayList<>();
        //TODO: needs to be generified
        String[] sourceTypes = new String[] {
            JavaProjectConstants.SOURCES_TYPE_JAVA,
            "xml", //NOI18N
        };
        for (String sourceType : sourceTypes) {
            allgroups.addAll(Arrays.asList(src.getSourceGroups(sourceType)));
        }
        if (allgroups.isEmpty()) {
            // Unknown project group
            Logger.getLogger(FolderTreeElement.class.getName()).severe("Cannot find SourceGroup for " + file.getPath()); // NOI18N
            return null;
        }
        for (SourceGroup group : allgroups) {
            if (group.getRootFolder().equals(file) || FileUtil.isParentOf(group.getRootFolder(), file)) {
                return group;
            }
        }
        return null;
    }
    
    private static SourceGroup getJavaSourceGroup(FileObject file) {
        Project prj = FileOwnerQuery.getOwner(file);
        if (prj == null) {
            return null;
        }
        Sources src = ProjectUtils.getSources(prj);
        SourceGroup[] javagroups = src.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);

        for (SourceGroup group : javagroups) {
            if (group.getRootFolder().equals(file) || FileUtil.isParentOf(group.getRootFolder(), file)) {
                return group;
            }
        }
        return null;
    }
    

    @Override
    public Object getUserObject() {
        return fo;
    }
}
