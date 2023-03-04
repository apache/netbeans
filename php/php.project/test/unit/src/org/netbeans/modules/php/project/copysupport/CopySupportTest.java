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
package org.netbeans.modules.php.project.copysupport;

import java.io.File;
import java.util.Collections;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties;
import org.netbeans.modules.php.project.util.PhpTestCase;
import org.netbeans.modules.php.project.util.TestUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class CopySupportTest extends PhpTestCase {

    public CopySupportTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
    }

    public void testProjectOpenAndClose() throws Exception {
        createAndOpenAndCloseProject();
    }

    public void testProjectSeveralOpenAndClose() throws Exception {
        final PhpProject phpProject = createAndOpenAndCloseProject();
        for (int i = 0; i < 10; i++) {
            openAndCloseProject(phpProject);
        }
    }

    public void testIssue192386() throws Exception {
        // XXX no idea how to reproduce the bug, let try it for several times
        for (int i = 0; i < 100; ++i) {
            createAndOpenAndCloseProject();
        }
    }

    public void testIssue192386LoggingOpen() throws Exception {
        final PhpProject project = TestUtils.createPhpProject(getWorkDir());
        final CopySupport copySupport = project.getCopySupport();

        dummyMethod(new Runnable() {
            @Override
            public void run() {
                copySupport.projectOpened();
            }
        });
        try {
            copySupport.projectOpened();
            fail("Should not get here.");
        } catch (IllegalStateException ise) {
            Throwable cause = ise.getCause();
            assertNotNull("Exception should have cause", cause);
            checkStackTrace(cause, CopySupportTest.class.getName(), "dummyMethod");
            assertEquals("Call stack should contain 1 element", 1, copySupport.callStack.size());
            assertEquals("Copy support should be opened twice", 2, copySupport.opened.get());
            assertEquals("Copy support should not be closed at all", 0, copySupport.closed.get());
        }
    }

    public void testIssue192386LoggingClose() throws Exception {
        final PhpProject project = TestUtils.createPhpProject(getWorkDir());
        final CopySupport copySupport = project.getCopySupport();

        copySupport.projectOpened();
        copySupport.projectClosed();
        try {
            copySupport.projectClosed();
            fail("Should not get here.");
        } catch (EmptyStackException ese) {
            assertTrue("Call stack should be empty", copySupport.callStack.empty());
            assertEquals("Copy support should be opened once", 1, copySupport.opened.get());
            assertEquals("Copy support should be closed twice", 2, copySupport.closed.get());
        }
    }

    public void testDefaultCopyFilesOnOpen() throws Exception {
        PhpProject project = TestUtils.createPhpProject(getWorkDir());
        TestUtils.openPhpProject(project);
        assertFalse(ProjectPropertiesSupport.isCopySourcesEnabled(project));
        assertFalse(ProjectPropertiesSupport.isCopySourcesOnOpen(project));
    }

    public void testCopyFilesOnOpen() throws Exception {
        PhpProject project = TestUtils.createPhpProject(getWorkDir());
        // copy target
        File copyTarget = new File(getWorkDir(), project.getName() + "-copy");
        assertFalse(copyTarget.exists());
        // props
        Map<String, String> props = new HashMap<>();
        props.put(PhpProjectProperties.COPY_SRC_FILES, Boolean.TRUE.toString());
        props.put(PhpProjectProperties.COPY_SRC_TARGET, copyTarget.getAbsolutePath());
        props.put(PhpProjectProperties.COPY_SRC_ON_OPEN, Boolean.TRUE.toString());
        PhpProjectProperties.save(project, Collections.<String, String>emptyMap(), props);
        // create file
        FileObject testFile = project.getProjectDirectory().createData("test", "php");

        TestUtils.openPhpProject(project);
        TestUtils.waitCopySupportFinished();

        assertTrue(ProjectPropertiesSupport.isCopySourcesEnabled(project));
        assertTrue(ProjectPropertiesSupport.isCopySourcesOnOpen(project));
        assertTrue(copyTarget.isDirectory());
        FileObject copyTargetFo = FileUtil.toFileObject(copyTarget);
        assertNotNull(copyTargetFo);
        FileObject[] copyChildren = copyTargetFo.getChildren();
        assertEquals(1, copyChildren.length);
        FileObject copyTestFile = copyChildren[0];
        assertEquals(testFile.getNameExt(), copyTestFile.getNameExt());
    }

    public void testCopyFilesOnSave() throws Exception {
        PhpProject project = TestUtils.createPhpProject(getWorkDir());
        // copy target
        File copyTarget = new File(getWorkDir(), project.getName() + "-copy");
        assertFalse(copyTarget.exists());
        // props
        Map<String, String> props = new HashMap<>();
        props.put(PhpProjectProperties.COPY_SRC_FILES, Boolean.TRUE.toString());
        props.put(PhpProjectProperties.COPY_SRC_TARGET, copyTarget.getAbsolutePath());
        props.put(PhpProjectProperties.COPY_SRC_ON_OPEN, Boolean.TRUE.toString());
        PhpProjectProperties.save(project, Collections.<String, String>emptyMap(), props);

        TestUtils.openPhpProject(project);
        TestUtils.waitCopySupportFinished();

        assertTrue(ProjectPropertiesSupport.isCopySourcesEnabled(project));
        assertTrue(ProjectPropertiesSupport.isCopySourcesOnOpen(project));
        assertTrue(copyTarget.isDirectory());
        assertEquals(0, copyTarget.list().length);

        // create file
        FileObject testFile = project.getProjectDirectory().createData("test", "php");

        TestUtils.waitCopySupportFinished();

        FileObject copyTargetFo = FileUtil.toFileObject(copyTarget);
        assertNotNull(copyTargetFo);
        FileObject[] copyChildren = copyTargetFo.getChildren();
        assertEquals(1, copyChildren.length);
        FileObject copyTestFile = copyChildren[0];
        assertEquals(testFile.getNameExt(), copyTestFile.getNameExt());
    }

    private PhpProject createAndOpenAndCloseProject() throws Exception {
        return openAndCloseProject(TestUtils.createPhpProject(getWorkDir()));
    }

    private PhpProject openAndCloseProject(PhpProject project) throws Exception {
        final CopySupport copySupport = project.getCopySupport();
        assertCopySupportClosed(copySupport);

        TestUtils.openPhpProject(project);
        assertCopySupportOpened(copySupport);

        TestUtils.closePhpProject(project);
        assertCopySupportClosed(copySupport);
        return project;
    }

    private void assertCopySupportOpened(CopySupport copySupport) {
        final int opened = copySupport.opened.get();
        final int closed = copySupport.closed.get();
        String msg = "Copy support should be opened (opened: " + opened + ", closed: " + closed + ").";
        assertTrue(msg, copySupport.projectOpened);
        assertEquals(msg, 1, opened - closed);
        assertEquals("Call stack should contain one element", 1, copySupport.callStack.size());
    }

    private void assertCopySupportClosed(CopySupport copySupport) {
        final int opened = copySupport.opened.get();
        final int closed = copySupport.closed.get();
        String msg = "Copy support should be closed (opened: " + opened + ", closed: " + closed + ").";
        assertFalse(msg, copySupport.projectOpened);
        assertEquals(msg, opened, closed);
        assertTrue("Call stack should be empty", copySupport.callStack.empty());
    }

    private void dummyMethod(Runnable action) {
        action.run();
    }

    private void checkStackTrace(Throwable throwable, String className, String methodName) {
        boolean found = false;
        for (StackTraceElement element : throwable.getStackTrace()) {
            if (element.getMethodName().equals(element.getMethodName())
                    && element.getClassName().equals(className)) {
                found = true;
                break;
            }
        }
        assertTrue(className + "::" + methodName + " should be found", found);
    }

}
