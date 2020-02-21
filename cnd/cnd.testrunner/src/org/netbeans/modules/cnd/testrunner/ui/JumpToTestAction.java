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

package org.netbeans.modules.cnd.testrunner.ui;

import java.awt.event.ActionEvent;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.gsf.testrunner.api.Testcase;
import org.openide.filesystems.FileObject;
import org.openide.text.PositionBounds;

/**
 * Jump to action for test methods.
 *
 */
final class JumpToTestAction extends BaseTestMethodNodeAction {

    private final boolean jumpToClass;

    JumpToTestAction(Testcase testcase, Project project, String name, boolean clazz) {
        super(testcase, project, name);
        this.jumpToClass = clazz;
    }

    protected void doActionPerformed(ActionEvent e) {
        ConfigurationDescriptorProvider cdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
        MakeConfigurationDescriptor projectDescriptor = cdp.getConfigurationDescriptor();
        if(projectDescriptor == null) {
            return;
        }
        
        Folder root = projectDescriptor.getLogicalFolders();
        Folder testRootFolder = null;
        for (Folder folder : root.getFolders()) {
            if(folder.isTestRootFolder()) {
                testRootFolder = folder;
                break;
            }
        }

        FileObject absPath = null;
        if (testRootFolder != null) {
            loop : for (Folder folder : testRootFolder.getAllTests()) {
                Item[] items = folder.getAllItemsAsArray();
                for (int k = 0; k < items.length; k++) {
                    if(items[k].getName().replaceAll("(.*)\\..*", "$1").equals(testcase.getClassName())) { // NOI18N
                        absPath = items[k].getFileObject();
                        break loop;
                    }
                }
            }
        }
        if(absPath == null) {
            return;
        }
        CsmFile file = CsmModelAccessor.getModel().findFile(FSPath.toFSPath(absPath), true, false);
        if(file == null) {
            return;
        }
        CsmOffsetableDeclaration targetDecl = null;
        for (CsmOffsetableDeclaration decl : file.getDeclarations()) {
            if(decl.getName().toString().equals(testcase.getName())) {
                targetDecl = decl;
                break;
            }
        }
        if(targetDecl == null) {
            return;
        }
        PositionBounds position = CsmUtilities.createPositionBounds(targetDecl);
        CsmUtilities.openSource(position);
    }

}
