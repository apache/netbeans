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

import javax.swing.Icon;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.UiUtils;
import org.netbeans.modules.python.editor.refactoring.PythonRefUtils;
import org.netbeans.modules.refactoring.spi.ui.*;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

public class FolderTreeElement implements TreeElement {
    private FileObject fo;

    FolderTreeElement(FileObject fo) {
        this.fo = fo;
    }

    @Override
    public TreeElement getParent(boolean isLogical) {
        if (isLogical) {
            SourceGroup sg = getSourceGroup(fo);
            if (sg != null) {
                return TreeElementFactory.getTreeElement(sg);
            } else {
                return null;
            }
        } else {
            Project p = FileOwnerQuery.getOwner(fo);
            if (p != null) {
                return TreeElementFactory.getTreeElement(p);
            } else {
                return null;
            }
        }
    }

    @Override
    public Icon getIcon() {
//        return UiUtils.getElementIcon(ElementKind.PACKAGE, null);
        // UGH! I need a "source folder" like icon!
        return UiUtils.getElementIcon(ElementKind.MODULE, null);
    }

    @Override
    public String getText(boolean isLogical) {
        ClassPath cp = ClassPath.getClassPath(fo, ClassPath.SOURCE);
        if (cp == null) {
            return fo.getPath();
        } else {
            if (getPythonSourceGroup(fo) != null) {
                String name = cp.getResourceName(fo).replace('/', '.');
                if ("".equals(name)) {
                    return NbBundle.getMessage(UiUtils.class, "LBL_DefaultPackage_PDU");
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
        //TODO: needs to be generified
        SourceGroup[] pythongroups = src.getSourceGroups(PythonRefUtils.SOURCES_TYPE_PYTHON);
        SourceGroup[] xmlgroups = src.getSourceGroups("xml");//NOI18N

        if (pythongroups.length == 0 && xmlgroups.length == 0) {
            // Probably used as part of some non-Python-related project refactoring operation (#106987)
            return null;
        }

        SourceGroup[] allgroups = new SourceGroup[pythongroups.length + xmlgroups.length];
        System.arraycopy(pythongroups, 0, allgroups, 0, pythongroups.length);
        System.arraycopy(xmlgroups, 0, allgroups, allgroups.length - 1, xmlgroups.length);
        for (SourceGroup group : allgroups) {
            if (group.getRootFolder().equals(file) || FileUtil.isParentOf(group.getRootFolder(), file)) {
                return group;
            }
        }
        return null;
    }

    private static SourceGroup getPythonSourceGroup(FileObject file) {
        Project prj = FileOwnerQuery.getOwner(file);
        if (prj == null) {
            return null;
        }
        Sources src = ProjectUtils.getSources(prj);
        SourceGroup[] pythongroups = src.getSourceGroups(PythonRefUtils.SOURCES_TYPE_PYTHON);

        for (SourceGroup pythongroup : pythongroups) {
            if (pythongroup.getRootFolder().equals(file) || FileUtil.isParentOf(pythongroup.getRootFolder(), file)) {
                return pythongroup;
            }
        }
        return null;
    }

    @Override
    public Object getUserObject() {
        return fo;
    }
}
