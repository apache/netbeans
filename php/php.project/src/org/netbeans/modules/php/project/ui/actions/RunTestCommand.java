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

import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.ui.actions.support.CommandUtils;
import org.netbeans.modules.php.project.ui.actions.support.Displayable;
import org.netbeans.modules.php.project.ui.actions.tests.GoToTest;
import org.netbeans.spi.gototest.TestLocator.LocationResult;
import org.netbeans.spi.project.ActionProvider;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 * @author Tomas Mysik
 */
public class RunTestCommand extends Command implements Displayable {
    public static final String ID = ActionProvider.COMMAND_TEST_SINGLE;
    public static final String DISPLAY_NAME = NbBundle.getMessage(RunTestCommand.class, "LBL_TestFile");

    public RunTestCommand(PhpProject project) {
        super(project);
    }

    @Override
    public void invokeActionInternal(Lookup context) {
        FileObject testClass = findTest(context);
        if (testClass == null) {
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(RunTestCommand.class, "MSG_TestNotFound"));
            return;
        }
        getProject().getLookup().lookup(ActionProvider.class).invokeAction(RunFileCommand.ID, Lookups.fixed(testClass));
    }

    @Override
    public boolean isActionEnabledInternal(Lookup context) {
        return true;
    }

    @Override
    public String getCommandId() {
        return ID;
    }

    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    private FileObject findTest(Lookup context) {
        FileObject fo = CommandUtils.fileForContextOrSelectedNodes(context, ProjectPropertiesSupport.getSourcesDirectory(getProject()));
        if (fo == null) {
            return null;
        }
        LocationResult locationResult = GoToTest.findTest(getProject(), fo);
        if (locationResult == null) {
            return null;
        }
        return locationResult.getFileObject();
    }
}
