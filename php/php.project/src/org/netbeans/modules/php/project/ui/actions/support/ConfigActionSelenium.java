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

package org.netbeans.modules.php.project.ui.actions.support;

import java.util.Collections;
import java.util.List;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.php.api.testing.PhpTesting;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.ui.testrunner.UnitTestRunner;
import org.netbeans.modules.php.spi.testing.PhpTestingProvider;
import org.netbeans.modules.php.spi.testing.run.TestRunInfo;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * Action implementation for SELENIUM TEST configuration.
 * It means running and debugging Selenium tests.
 * @author Tomas Mysik
 */
class ConfigActionSelenium extends ConfigActionTest {

    private static final String PHP_UNIT_IDENT = "PhpUnit"; // NOI18N


    protected ConfigActionSelenium(PhpProject project) {
        super(project);
    }

    @Override
    protected List<FileObject> getTestDirectories(boolean showCustomizer) {
        FileObject seleniumDirectory = ProjectPropertiesSupport.getSeleniumDirectory(project, showCustomizer);
        if (seleniumDirectory == null) {
            return Collections.emptyList();
        }
        return Collections.singletonList(seleniumDirectory);
    }

    @Override
    protected void runJsTests() {
        // noop
    }

    @Override
    void run(TestRunInfo testRunInfo) {
        PhpTestingProvider phpUnit = findPhpUnit();
        if (phpUnit == null) {
            informUser();
            return;
        }
        new UnitTestRunner(project, testRunInfo, new ConfigActionTest.RerunUnitTestHandler(testRunInfo), Collections.singletonList(phpUnit))
                .run();
    }

    @CheckForNull
    private PhpTestingProvider findPhpUnit() {
        for (PhpTestingProvider provider : PhpTesting.getTestingProviders()) {
            if (PHP_UNIT_IDENT.equals(provider.getIdentifier())) {
                return provider;
            }
        }
        return null;
    }

    @NbBundle.Messages("ConfigActionSelenium.phpunit.missing=PHPUnit support is not installed (use Tools > Plugins).")
    private void informUser() {
        DialogDisplayer.getDefault().notifyLater(
                new NotifyDescriptor.Message(Bundle.ConfigActionSelenium_phpunit_missing(), NotifyDescriptor.INFORMATION_MESSAGE));
    }

}
