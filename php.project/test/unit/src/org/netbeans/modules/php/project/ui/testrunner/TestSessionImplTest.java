/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.project.ui.testrunner;

import java.io.File;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.gsf.testrunner.ui.api.Manager;
import org.netbeans.modules.gsf.testrunner.api.TestSession;
import org.netbeans.modules.php.api.editor.PhpType;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.util.PhpTestCase;
import org.netbeans.modules.php.project.util.TestUtils;
import org.netbeans.modules.php.spi.testing.PhpTestingProvider;
import org.netbeans.modules.php.spi.testing.create.CreateTestsResult;
import org.netbeans.modules.php.spi.testing.locate.Locations;
import org.netbeans.modules.php.spi.testing.locate.TestLocator;
import org.netbeans.modules.php.spi.testing.run.TestRunException;
import org.netbeans.modules.php.spi.testing.run.TestRunInfo;
import org.netbeans.modules.php.spi.testing.run.TestSuite;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class TestSessionImplTest extends PhpTestCase {

    public TestSessionImplTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        clearWorkDir();
    }

    public void testFrozen() throws Exception {
        PhpProject project = TestUtils.createPhpProject(getWorkDir());
        DummyTestingProvider dummyTestingProvider = new DummyTestingProvider();
        TestSessionImpl testSessionImpl = new TestSessionImpl(Manager.getInstance(), new TestSession("dummySession", project, TestSession.SessionType.TEST), dummyTestingProvider);
        dummyTestingProvider.runTests(null, null, null);
        FileObject projectDirectory = project.getProjectDirectory();
        TestSuite testSuite = testSessionImpl.addTestSuite("MyTest", projectDirectory.createData("MyTest", "php"));
        testSuite.addTestCase("testMe", "DUMMY");
        File testFile = new File(getWorkDir(), "MyTest.php");
        assertFalse(testFile.exists());
        assertTrue(testFile.createNewFile());
        FileObject fo = FileUtil.toFileObject(testFile);
        assertNotNull(fo);
        testSessionImpl.freeze();
        Exception exception = null;
        try {
            testSessionImpl.addTestSuite("YourTest", fo);
            fail("Should not get here");
        } catch (IllegalStateException ex) {
            // expected
            exception = ex;
        }
        assertNotNull(exception);
        exception = null;
        try {
            testSuite.addTestCase("testMe2", "DUMMY");
            fail("Should not get here");
        } catch (IllegalStateException ex) {
            // expected
            exception = ex;
        }
        assertNotNull(exception);
    }

    //~ Inner classes

    private static final class DummyTestingProvider implements PhpTestingProvider {

        @Override
        public String getIdentifier() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getDisplayName() {
            return "Dummy Testing Provider";
        }

        @Override
        public boolean isTestFile(PhpModule phpModule, FileObject fileObj) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isTestCase(PhpModule phpModule, PhpType.Method method) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void runTests(PhpModule phpModule, TestRunInfo runInfo, org.netbeans.modules.php.spi.testing.run.TestSession testSession) throws TestRunException {
            // noop
        }

        @Override
        public TestLocator getTestLocator(PhpModule phpModule) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public CreateTestsResult createTests(PhpModule phpModule, List<FileObject> files, Map<String, Object> configurationPanelProperties) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isCoverageSupported(PhpModule phpModule) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Locations.Line parseFileFromOutput(String line) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public ProjectCustomizer.CompositeCategoryProvider createCustomizer(PhpModule phpModule) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }

}
