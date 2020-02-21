/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
