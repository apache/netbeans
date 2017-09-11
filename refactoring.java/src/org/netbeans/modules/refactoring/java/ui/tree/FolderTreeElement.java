/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.refactoring.java.ui.tree;

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
public class FolderTreeElement implements TreeElement {
    
    private FileObject fo;
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
        //TODO: needs to be generified
        SourceGroup[] javagroups = src.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        SourceGroup[] xmlgroups = src.getSourceGroups("xml");//NOI18N
        
        SourceGroup[] allgroups =  new SourceGroup[javagroups.length + xmlgroups.length];

        if (allgroups.length < 1) {
            // Unknown project group
            Logger.getLogger(FolderTreeElement.class.getName()).severe("Cannot find SourceGroup for " + file.getPath()); // NOI18N
            return null;
       }
        System.arraycopy(javagroups,0,allgroups,0,javagroups.length);
        System.arraycopy(xmlgroups,0,allgroups,allgroups.length-1,xmlgroups.length);
        for(int i=0; i<allgroups.length; i++) {
            if (allgroups[i].getRootFolder().equals(file) || FileUtil.isParentOf(allgroups[i].getRootFolder(), file)) {
                return allgroups[i];
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
        
        for(int i=0; i<javagroups.length; i++) {
            if (javagroups[i].getRootFolder().equals(file) || FileUtil.isParentOf(javagroups[i].getRootFolder(), file)) {
                return javagroups[i];
            }
        }
        return null;
    }
    

    @Override
    public Object getUserObject() {
        return fo;
    }
}
