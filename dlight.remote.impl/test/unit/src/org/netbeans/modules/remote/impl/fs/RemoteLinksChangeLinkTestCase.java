/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.remote.impl.fs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import junit.framework.Test;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.nativeexecution.test.RcFile.FormatException;
import org.netbeans.modules.remote.test.RemoteApiTest;
import org.openide.filesystems.*;

/**
 * There hardly is a way to unit test remote operations.
 * This is just an entry point for manual validation.
 *
 */
public class RemoteLinksChangeLinkTestCase extends RemoteFileTestBase {

    public RemoteLinksChangeLinkTestCase(String testName, ExecutionEnvironment execEnv) throws IOException, FormatException {
        super(testName, execEnv);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        RemoteFileObjectFactory.testSetReportUnexpected(false);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        RemoteFileObjectFactory.testSetReportUnexpected(true);
    }

    @ForAllEnvironments
    public void testChangeDirectoryLink() throws Exception {
        String baseDir = null;
        try {
            baseDir = mkTempAndRefreshParent(true);

            String fileName = "file";
            String realDir1 = baseDir + "/real_dir_1";
            String realDir2 = baseDir + "/real_dir_2";
            String linkDirName = "link_dir";
            String linkDir = baseDir + '/' + linkDirName;
            String realFile1 = realDir1 + "/" + fileName;
            String realFile2 = realDir2 + "/" + fileName;
            String linkFile1 = linkDir + "/" + fileName;

            String creationScript =
                    "cd " + baseDir + "; " +
                    "mkdir -p " + realDir1 + "; " +
                    "mkdir -p " + realDir2 + "; " +
                    "ln -s " + realDir1 + ' ' + linkDirName + "; " +
                    "echo 123 > " + realFile1 + "; " +
                    "echo abc > " + realFile2;

            ProcessUtils.ExitStatus res1 = ProcessUtils.execute(execEnv, "sh", "-c", creationScript);
            assertEquals("Error executing script \"" + creationScript + "\": " + res1.getErrorString(), 0, res1.exitCode);

            FileObject realFO, linkFO, linkDirFO;
            RemoteFileObject oldChildFO;
            
            realFO = getFileObject(realFile1);
            linkFO = getFileObject(linkFile1);
            linkDirFO = getFileObject(linkDir);
            FileObject tmpFo = linkDirFO.getFileObject(fileName);
            assertNotNull(tmpFo);
            assertTrue(tmpFo instanceof RemoteFileObject);
            oldChildFO = (RemoteFileObject) tmpFo;
            RemoteFileObjectBase oldChildFO_implementor = oldChildFO.getImplementor();

            String changeScript =
                    "cd " + baseDir + "; " +
                    "rm " + linkDirName + "; " +
                    "ln -s " + realDir2 + ' ' + linkDirName + "; " +
                    "";

            ProcessUtils.ExitStatus res2 = ProcessUtils.execute(execEnv, "sh", "-c", changeScript);
            assertEquals("Error executing script \"" + creationScript + "\": " + res1.getErrorString(), 0, res1.exitCode);

            RemoteFileObject baseDirFO = getFileObject(baseDir);
            baseDirFO.nonRecursiveRefresh();

            boolean childValid = oldChildFO.isValid();
            boolean newImplementorValid = oldChildFO.getImplementor().isValid();
            boolean oldImplementorValid = oldChildFO_implementor.isValid();
            FileObject oldParent = oldChildFO.getParent();
            boolean parentValid = oldParent.isValid();
            //FileObject[] children = oldParent.getChildren();
            assertFalse("Old implementor should be be invalid", oldImplementorValid);
            assertTrue("New implementor should be valid", newImplementorValid);
            assertTrue("Child should be valid", childValid);
            assertTrue("Parent should be valid", parentValid);
        } finally {
            removeRemoteDirIfNotNull(baseDir);
        }
    }

    @ForAllEnvironments
    public void testChangedLinkListeners() throws Exception {
        String baseDir = null;
        try {
            baseDir = mkTempAndRefreshParent(true);

            String fileName = "file";
            String realDir1 = baseDir + "/real_dir_1";
            String realDir2 = baseDir + "/real_dir_2";
            String linkDirName = "link_dir";
            String linkDir = baseDir + '/' + linkDirName;
            String realFile1 = realDir1 + "/" + fileName;
            String realFile2 = realDir2 + "/" + fileName;
            String linkFile1 = linkDir + "/" + fileName;

            String creationScript =
                    "cd " + baseDir + "; " +
                    "mkdir -p " + realDir1 + "; " +
                    "mkdir -p " + realDir2 + "; " +
                    "ln -s " + realDir1 + ' ' + linkDirName + "; " +
                    "echo 123 > " + realFile1;

            ProcessUtils.ExitStatus res1 = ProcessUtils.execute(execEnv, "sh", "-c", creationScript);
            assertEquals("Error executing script \"" + creationScript + "\": " + res1.getErrorString(), 0, res1.exitCode);

            RemoteFileObject baseDirFO = getFileObject(baseDir);
            RemoteFileObject realDirFO1 = getFileObject(realDir1);
            RemoteFileObject realDirFO2 = getFileObject(realDir2);
            RemoteFileObject linkDirFO = getFileObject(linkDir);
            RemoteFileObject realFileFO1 = getFileObject(realFile1);
            RemoteFileObject realFileFO2 = getFileObject(realFile1);

            final List<FileEvent> eventList = Collections.synchronizedList(new ArrayList<FileEvent>());

            FileChangeListener listener = new FileChangeListener() {
                private void register(FileEvent fe) {
                    eventList.add(fe);
                }
                @Override
                public void fileChanged(FileEvent fe) {
                    register(fe);
                }

                @Override
                public void fileAttributeChanged(FileAttributeEvent fe) {
                    register(fe);
                }

                @Override
                public void fileDataCreated(FileEvent fe) {
                    register(fe);
                }

                @Override
                public void fileFolderCreated(FileEvent fe) {
                    register(fe);
                }

                @Override
                public void fileRenamed(FileRenameEvent fe) {
                    register(fe);
                }

                @Override
                public void fileDeleted(FileEvent fe) {
                    register(fe);
                }
            };
            linkDirFO.addFileChangeListener(listener);
            FileUtil.createData(realDirFO1, "file_2");
            assertFalse("No events came after programmatic file creatin in dir 1", eventList.isEmpty());

            String changeScript =
                    "cd " + baseDir + "; " +
                    "rm " + linkDirName + "; " +
                    "ln -s " + realDir2 + ' ' + linkDirName + "; " +
                    "";

            ProcessUtils.ExitStatus res2 = ProcessUtils.execute(execEnv, "sh", "-c", changeScript);
            assertEquals("Error executing script \"" + creationScript + "\": " + res1.getErrorString(), 0, res1.exitCode);

            baseDirFO.refresh();
            eventList.clear();
            FileUtil.createData(realDirFO1, "file_3");
            assertTrue("Event list should be empty", eventList.isEmpty());
            FileUtil.createData(realDirFO2, "file_4");
            assertFalse("No events came after programmatic file creatin in dir 1", eventList.isEmpty());


        } finally {
            removeRemoteDirIfNotNull(baseDir);
        }
    }

    public static Test suite() {
        return RemoteApiTest.createSuite(RemoteLinksChangeLinkTestCase.class);
    }
}
