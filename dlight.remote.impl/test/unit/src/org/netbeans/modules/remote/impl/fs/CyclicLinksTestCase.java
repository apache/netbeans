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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import junit.framework.Test;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.PasswordManager;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.nativeexecution.test.RcFile.FormatException;
import org.netbeans.modules.remote.impl.fs.server.FSSTransport;
import org.netbeans.modules.remote.test.RemoteApiTest;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * There hardly is a way to unit test remote operations.
 * This is just an entry point for manual validation.
 *
 */
public class CyclicLinksTestCase extends RemoteFileTestBase {

//    static {
//        System.setProperty("remote.fs_server.verbose", "4");
//        System.setProperty("remote.fs_server.suppress.stderr", "false");
//        System.setProperty("remote.fs_server.verbose.response", "true");
//        System.setProperty("rfs.vcs.cache", "false");
//    }

    public CyclicLinksTestCase(String testName) {
        super(testName);
    }
    
    public CyclicLinksTestCase(String testName, ExecutionEnvironment execEnv) throws IOException, FormatException {
        super(testName, execEnv);
    }


    @ForAllEnvironments
    public void test_iz_269195() throws Exception {
        String baseDir = null;
        try {
            baseDir = mkTempAndRefreshParent(true);
            String linkName = "cyclic_link";
            executeInDir(baseDir, "ln", "-s", "./", linkName);
            //RemoteFileObject linkFO1 = getFileObject(baseDir + "/" + linkName);
            //RemoteFileObject linkFO2 = getFileObject(baseDir + "/" + linkName + "/" + linkName);
            RemoteFileObject linkFO3 = getFileObject(baseDir + "/" + linkName + "/" + linkName + "/" + linkName);
            assertFalse("canWrite() should return false for a cyclic link " + linkFO3, linkFO3.canWrite());
        } finally {
            if (baseDir != null) {
                ProcessUtils.ExitStatus res = ProcessUtils.execute(getTestExecutionEnvironment(), "chmod", "-R", "700", baseDir);
                removeRemoteDirIfNotNull(baseDir);
            }
            FSSTransport.getInstance(execEnv).testSetCleanupUponStart(true);
        }        
    }
  
    @ForAllEnvironments
    public void test_iz_269198() throws Exception {
        String baseDir = null;
        try {
            baseDir = mkTempAndRefreshParent(true);
            executeInDir(baseDir, "ln", "-s", "./", "cyclic_link1");
            executeInDir(baseDir, "ln", "-s", "./", "cyclic_link2");
            RemoteFileObject baseFO = getFileObject(baseDir);
            baseFO.refresh();
            AtomicInteger maxNestedLevel = new AtomicInteger(0);
            AtomicReference<String> deepestPath = new AtomicReference();
            recurse(baseFO, new AtomicInteger(0), deepestPath, maxNestedLevel, 5);
            System.err.println("Max nestng level " + maxNestedLevel + " directory is " + deepestPath);
        } finally {
            if (baseDir != null) {
                ProcessUtils.ExitStatus res = ProcessUtils.execute(getTestExecutionEnvironment(), "chmod", "-R", "700", baseDir);
                removeRemoteDirIfNotNull(baseDir);
            }
            FSSTransport.getInstance(execEnv).testSetCleanupUponStart(true);
        }        
    }

    private void recurse(RemoteFileObject fo, AtomicInteger currNestedLevel, 
            AtomicReference<String> deepestPath, AtomicInteger maxNestedLevel, int maxAllowedNestedLevel) {
        if (fo.isFolder()) {
            RemoteFileObject[] children = fo.getChildren();
            if (children == null || children.length == 0) {
                return;
            }
            currNestedLevel.incrementAndGet();
            if (currNestedLevel.get() > maxNestedLevel.get()) {
                maxNestedLevel.set(currNestedLevel.get());
                deepestPath.set(children[0].getPath());
                if (maxNestedLevel.get() > maxAllowedNestedLevel) {
                    assertTrue("Maximim allowed nesting " + maxAllowedNestedLevel + " exceeded at path " + deepestPath, false);
                }
            }
            try {
                for (RemoteFileObject child : children) {
                    recurse(child, currNestedLevel, deepestPath, maxNestedLevel, maxAllowedNestedLevel);
                }
            } finally {
                currNestedLevel.decrementAndGet();
            }
        }
    }

    public static Test suite() {
        return RemoteApiTest.createSuite(CyclicLinksTestCase.class);
    }
}
