/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
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
