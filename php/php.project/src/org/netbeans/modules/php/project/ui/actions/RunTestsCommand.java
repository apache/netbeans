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
package org.netbeans.modules.php.project.ui.actions;

import java.util.Enumeration;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ui.actions.support.CommandUtils;
import org.netbeans.modules.php.project.ui.actions.support.ConfigAction;
import org.netbeans.modules.php.project.ui.actions.support.Displayable;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 * Run all tests in the selected folder.
 */
public class RunTestsCommand extends Command implements Displayable {

    public static final String ID = "runTestsInFolder"; // NOI18N
    @NbBundle.Messages("RunTestsCommand.label=Run Tests")
    public static final String DISPLAY_NAME = Bundle.RunTestsCommand_label();


    public RunTestsCommand(PhpProject project) {
        super(project);
    }

    @Override
    public String getCommandId() {
        return ID;
    }

    @Override
    public void invokeActionInternal(Lookup context) {
        FileObject folder = findFolderWithTest(context);
        if (folder == null) {
            logger.warning("Folder should be found for running tests");
            return;
        }
        ConfigAction.get(ConfigAction.Type.TEST, getProject()).runFile(Lookups.fixed(folder));
    }

    @Override
    public boolean isActionEnabledInternal(Lookup context) {
        return findFolderWithTest(context) != null;
    }

    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    private FileObject findFolderWithTest(Lookup context) {
        FileObject[] files = CommandUtils.filesForContextOrSelectedNodes(context);
        if (files.length != 1) {
            return null;
        }
        FileObject file = files[0];
        if (!file.isFolder()) {
            return null;
        }
        Enumeration<? extends FileObject> children = file.getChildren(true);
        while (children.hasMoreElements()) {
            FileObject child = children.nextElement();
            if (child.isData()
                    && isTestFile(child)
                    && FileUtils.isPhpFile(child)) {
                return file;
            }
        }
        return null;
    }

}
