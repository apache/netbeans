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
package org.netbeans.modules.testng.ui.actions;

import java.util.*;
import java.util.logging.Logger;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gsf.testrunner.api.TestCreatorProvider;
import org.netbeans.modules.gsf.testrunner.api.TestCreatorProvider.Context;
import org.netbeans.modules.gsf.testrunner.api.TestCreatorProvider.Registration;
import org.netbeans.modules.gsf.testrunner.plugin.CommonPlugin;
import org.netbeans.modules.java.testrunner.CommonTestUtil;
import org.netbeans.modules.java.testrunner.GuiUtils;
import org.netbeans.modules.java.testrunner.ui.api.UIJavaUtils;
import org.netbeans.modules.testng.api.TestNGSupport;
import org.netbeans.modules.testng.api.TestNGUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Theofanis Oikonomou
 */
@Registration(displayName=GuiUtils.TESTNG_TEST_FRAMEWORK, identifier = TestCreatorProvider.IDENTIFIER_TESTNG, position = 1000)
public class TestNGTestCreatorProvider extends TestCreatorProvider {

    private static final Logger LOGGER = Logger.getLogger(TestNGTestCreatorProvider.class.getName());

    @Override
    public boolean enable(FileObject[] activatedFOs) {
        if (activatedFOs == null || activatedFOs.length == 0) {
            return false;
        }
        if (activatedFOs[0] != null && activatedFOs[0].isValid()) {
            Project p = FileOwnerQuery.getOwner(activatedFOs[0]);
            return TestNGSupport.isActionSupported(TestNGSupport.Action.CREATE_TEST, p) || TestNGSupport.isSupportEnabled(activatedFOs);
        }
        return false;
    }

    @Override
    public void createTests(Context context) {
        final FileObject[] filesToTest = context.getActivatedFOs();
        if (filesToTest == null) {
            return;     //XXX: display some message
        }

        if (!TestNGUtils.createTestActionCalled(filesToTest)) {
            return;
        }

        /*
         * Store the configuration data:
         */
        final boolean singleClass = context.isSingleClass();
        final Map<CommonPlugin.CreateTestParam, Object> params = CommonTestUtil.getSettingsMap(!singleClass);
        if (singleClass) {
            String name = context.getTestClassName();
            params.put(CommonPlugin.CreateTestParam.CLASS_NAME, name);
        }
        
        final FileObject targetFolder = context.getTargetFolder();
        TestNGSupport.findTestNGSupport(FileOwnerQuery.getOwner(targetFolder)).configureProject(targetFolder);
        RequestProcessor.getDefault().post(new Runnable() {

            @Override
            public void run() {
                /*
                 * Now create the tests:
                 */
                final FileObject[] testFileObjects = TestNGUtils.createTests(filesToTest, targetFolder, params);

                /*
                 * Open the created/updated test class if appropriate:
                 */
                if (testFileObjects.length == 1) {
                    UIJavaUtils.openFile(testFileObjects[0], 1);
                }
            }
        });
    }
    
}
