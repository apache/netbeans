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
