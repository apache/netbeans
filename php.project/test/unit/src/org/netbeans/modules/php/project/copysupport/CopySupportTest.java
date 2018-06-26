/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
