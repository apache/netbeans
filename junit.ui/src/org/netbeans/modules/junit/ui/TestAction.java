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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.junit.ui;

import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.actions.NodeAction;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.junit.api.JUnitTestUtil;
import org.openide.filesystems.FileUtil;

/**
 * 
 * @author  Marian Petras
 */
abstract class TestAction extends NodeAction { 
    
    /** Creates a new instance of TestAction */
    TestAction() {
    }
    
    @Override
    public boolean asynchronous() {
        return false;
    }
    
    /**
     * Perform special enablement check in addition to the normal one.
     * 
     *     protected boolean enable (Node[] nodes) {
     *         if (!super.enable(nodes)) {
     *             return false;
     *         }
     *     }
     * 
     *     if (...) {
     *         ...
     *     }
     */
    @Override
    protected boolean enable(Node[] nodes) {
        if (nodes.length == 0) {
            return false;
        }
        
        for (Node node : nodes) {
            DataObject dataObj = node.getCookie(DataObject.class);
            if (dataObj != null) {
                FileObject fileObj = dataObj.getPrimaryFile();
                if ((fileObj == null) || !fileObj.isValid()) {
                    continue;
                }
                
                Project prj = FileOwnerQuery.getOwner(fileObj);
                if ((prj == null) || (getSourceGroup(fileObj, prj) == null)) {
                    continue;
                }

                if (JUnitTestUtil.isJavaFile(fileObj) 
                        || (node.getCookie(DataFolder.class) != null)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     */
    static SourceGroup getSourceGroup(FileObject file, Project prj) {
        Sources src = ProjectUtils.getSources(prj);
        SourceGroup[] srcGrps = src.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        for (SourceGroup srcGrp : srcGrps) {
            FileObject rootFolder = srcGrp.getRootFolder();
            if (((file == rootFolder) || FileUtil.isParentOf(rootFolder, file)) 
                    && srcGrp.contains(file)) {
                return srcGrp;
            }
        }
        return null;
    }    
    
    
}
