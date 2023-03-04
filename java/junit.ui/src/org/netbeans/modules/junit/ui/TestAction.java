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
