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

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import junit.framework.Test;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.nativeexecution.test.RcFile.FormatException;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.netbeans.modules.remote.test.RemoteApiTest;
import org.openide.filesystems.FileObject;
import org.openide.util.Utilities;

/**
 * There hardly is a way to unit test remote operations.
 * This is just an entry point for manual validation.
 *
 */
public class LinkListenersTestCase extends RemoteFileTestBase {

    public LinkListenersTestCase(String testName) {
        super(testName);
    }
    
    public LinkListenersTestCase(String testName, ExecutionEnvironment execEnv) throws IOException, FormatException {
        super(testName, execEnv);
    }


    private void doTestListeners() throws Throwable {
        String baseDir = mkTempAndRefreshParent(true);
        File workDir = getWorkDir();
        File log = new File(workDir, "remote.dat");            
        PrintStream out = new PrintStream(log);
        try {                        
            final String childName = "child_file.h";
            final String subdirName = "child_dir";
            String childLinkName = childName + ".lnk";
            String subirLinkName = subdirName + ".lnk";

            FileObject baseDirFO = getFileObject(baseDir);
            ExecutionEnvironment env = FileSystemProvider.getExecutionEnvironment(baseDirFO);
            
            String[] creationData = new String[] {
                "- " + childName,
                "d " + subdirName,
                "l " + childName + ' ' + childLinkName,
                "l " + subdirName + ' ' + subirLinkName
            };
            createDirStructure(env, baseDir, creationData);
            
            
            baseDirFO.refresh();

            FileObject childLinkFO = baseDirFO.getFileObject(childLinkName);
            assertNotNull(childLinkFO);
            FileObject subdirLinkFO = baseDirFO.getFileObject(subirLinkName);
            assertNotNull(subdirLinkFO);
            FileObject childFO = baseDirFO.getFileObject(childName);
            assertNotNull(childFO);
            FileObject subdirFO = baseDirFO.getFileObject(subdirName);
            assertNotNull(subdirFO);
            
            subdirFO.getChildren(); // otherwise no file creation event occurs
            subdirLinkFO.getChildren(); // otherwise no file creation event occurs
            
            String prefix = baseDirFO.getPath();
            FileSystemProvider.addRecursiveListener(new DumpingFileChangeListener("recursive", prefix, out, true), baseDirFO.getFileSystem(), baseDirFO.getPath());
            baseDirFO.addFileChangeListener(new DumpingFileChangeListener("baseDir", prefix, out, true));
            subdirFO.addFileChangeListener(new DumpingFileChangeListener(subdirFO.getNameExt(), prefix, out, true));
            subdirLinkFO.addFileChangeListener(new DumpingFileChangeListener(subdirLinkFO.getNameExt(), prefix, out, true));
            
            executeInDir(subdirFO.getPath(), env, "touch",  "file_1.h");
            baseDirFO.refresh();
            
            out.close();
            printFile(log, "REMOTE", System.out);
        } finally {
            if (out != null) {
                out.close();
            }
            removeRemoteDirIfNotNull(baseDir);
        }    
    }
    
    @ForAllEnvironments
    public void testLinkListeners() throws Throwable {
        if (Utilities.isWindows()) {
            System.err.printf("Skipping %s test on Windows\n", getClass().getName());
            return;
        }
        doTestListeners();
    }

    public static Test suite() {
        return RemoteApiTest.createSuite(LinkListenersTestCase.class);
    }
}
