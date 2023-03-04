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
package org.netbeans.modules.php.codeception.create;

import java.awt.Component;
import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.gsf.testrunner.ui.spi.TestCreatorConfiguration;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.codeception.CodeceptionTestingProvider;
import org.netbeans.modules.php.codeception.commands.Codecept;
import org.netbeans.modules.php.codeception.ui.CodeceptionCreateTestPanel;
import org.netbeans.modules.php.spi.testing.create.CreateTestsSupport;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.Pair;

public final class CodeceptionTestCreatorConfiguration extends TestCreatorConfiguration {

    private final CreateTestsSupport createTestsSupport;

    // @GuardedBy("EDT")
    private CodeceptionCreateTestPanel panel;
    // @GuardedBy("this")
    private List<String> suites;


    CodeceptionTestCreatorConfiguration(FileObject[] activatedFileObjects) {
        assert activatedFileObjects != null;
        assert activatedFileObjects.length > 0;
        createTestsSupport = CreateTestsSupport.create(CodeceptionTestingProvider.getInstance(), activatedFileObjects);
    }

    @Override
    public boolean canHandleProject(String framework) {
        return CodeceptionTestingProvider.IDENTIFIER.equals(framework);
    }

    @Override
    public Component getConfigurationPanel(Context context) {
        assert EventQueue.isDispatchThread();
        if (panel == null) {
            panel = new CodeceptionCreateTestPanel(TestCreator.TEST_COMMANDS, getSuites());
        }
        return panel;
    }

    @Override
    public boolean isValid() {
        return getErrorMessage() == null;
    }

    @NbBundle.Messages("CodeceptionTestCreatorConfiguration.error.suites.none=No test suites available in project.")
    @Override
    public String getErrorMessage() {
        if (getSuites().isEmpty()) {
            return Bundle.CodeceptionTestCreatorConfiguration_error_suites_none();
        }
        return null;
    }

    @Override
    public void persistConfigurationPanel(Context context) {
        assert EventQueue.isDispatchThread();
        context.getProperties().put(TestCreator.GENERATE_COMMAND_PARAM, panel.getSelectedCommand());
        context.getProperties().put(TestCreator.SUITE_PARAM, panel.getSelectedSuite());
    }

    @Override
    public Object[] getTestSourceRoots(Collection<SourceGroup> createdSourceRoots, FileObject fo) {
        return createTestsSupport.getTestSourceRoots(createdSourceRoots, fo);
    }

    @Override
    public boolean showClassNameInfo() {
        return false;
    }

    @Override
    public boolean showClassToTestInfo() {
        return false;
    }

    @Override
    public Pair<String, String> getSourceAndTestClassNames(FileObject fo, boolean isTestNG, boolean isSelenium) {
        // XXX this causes validation to fail but no error message is displayed!
        //return new String[] {"", ""};
        // XXX this causes AIOOBE in CommonTestsCfgOfCreate.fillFormData(CommonTestsCfgOfCreate.java:990)
        //return new String[0];
        // XXX this causes NPE in ClassNameTextField$SpaceIgnoringDocumentFilter.removeSpaces(ClassNameTextField.java:477)
        //return new String[] {null, null};
        // XXX in this case, OK button is disabled as well and I have no idea why, no error anywhere...
        //return new String[] {"whatever", "donotcare"};
        //  update: aha, the test class must end with Test - it does not, trust me ;)
        return Pair.of("whatever", "donotcareTest");
    }

    private synchronized List<String> getSuites() {
        if (suites == null) {
            PhpModule phpModule = createTestsSupport.getPhpModule();
            assert phpModule != null;
            suites = new ArrayList<>(Codecept.getSuiteNames(phpModule));
        }
        return Collections.unmodifiableList(suites);
    }

}
