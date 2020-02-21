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
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.gsf.testrunner.api.Testcase;
import org.netbeans.spi.project.ActionProvider;
import org.openide.util.lookup.Lookups;

/**
 * An action for running/debugging a singe test method.
 *
 */
class RunTestMethodAction extends BaseTestMethodNodeAction {

    private static final Logger LOGGER = Logger.getLogger(RunTestMethodAction.class.getName());
    private final boolean debug;

    public RunTestMethodAction(Testcase testcase, Project project, String name, boolean debug) {
        super(testcase, project, name);
        this.debug = debug;
    }

    protected void doActionPerformed(ActionEvent e) {
        ActionProvider ap = project.getLookup().lookup(ActionProvider.class);
        if (ap != null) {
            Folder targetFolder = findTestFolder();
            if(targetFolder != null) {
                if(debug) {
                    ap.invokeAction(ActionProvider.COMMAND_DEBUG_TEST_SINGLE, Lookups.fixed(new Object[]{project, targetFolder}));
                } else {
                    ap.invokeAction(ActionProvider.COMMAND_TEST_SINGLE, Lookups.fixed(new Object[]{project, targetFolder}));
                }
            }
        }
    }

    private Folder findTestFolder() {
        MakeConfigurationDescriptor mcd = MakeConfigurationDescriptor.getMakeConfigurationDescriptor(project);
        Folder root = mcd.getLogicalFolders();
        Folder testRootFolder = null;
        for (Folder folder : root.getFolders()) {
            if (folder.isTestRootFolder()) {
                testRootFolder = folder;
                break;
            }
        }
        if (testRootFolder != null) {
            for (Folder folder : testRootFolder.getAllTests()) {
                Item[] items = folder.getAllItemsAsArray();
                for (int k = 0; k < items.length; k++) {
                    if(items[k].getName().replaceFirst("\\..*", "").equals(testcase.getClassName())) { // NOI18N
                        return folder;
                    }
                }
            }
        }
        return null;
    }
}
