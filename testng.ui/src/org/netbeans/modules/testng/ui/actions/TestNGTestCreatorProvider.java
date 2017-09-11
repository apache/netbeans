/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
@Registration(displayName=GuiUtils.TESTNG_TEST_FRAMEWORK, identifier = TestCreatorProvider.IDENTIFIER_TESTNG)
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
