/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.remote.impl.fs;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import junit.framework.Test;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.PasswordManager;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.remote.test.RemoteApiTest;
import org.openide.filesystems.FileObject;

/**
 *
 */
public class DeleteOnExitTestCase extends RemoteFileTestBase {
    
    private Properties oldProps;
    private List<String> logsToremove = new ArrayList<>();
    private final long testCreationTimeStamp = System.currentTimeMillis();

    public DeleteOnExitTestCase(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }

    protected Properties setProperties(String... props) {
        if (props.length % 2 == 1) {
            throw new IllegalArgumentException("Incorrect number of parameters");
        }
        Properties oldValues = System.getProperties();
        for (int i = 0; i < props.length; i+=2) {
            String key = props[i];
            String value = props[i+1];
            System.setProperty(key, value);
        }
        return oldValues;
    }
    
    private String getStdErrFileName(String postfix) {
        StringBuilder sb = new StringBuilder("/tmp/fs_server_err_");
        sb.append(getName().replace(" ", "").replace("[", "_").replace("]", "")).append('_');
        String buildTag = System.getenv("BUILD_TAG");
        if (buildTag == null) {
            buildTag = System.getenv("USER") + '_' + testCreationTimeStamp;
        }
        if (buildTag != null) {
            sb.append(buildTag).append('_');
        }
        sb.append(postfix).append(".txt");
        logsToremove.add(sb.toString());
        return sb.toString();
    }
    
    @Override
    protected void setUp() throws Exception {
        oldProps = setProperties(
            "remote.fs_server.log", "true",
            "remote.fs_server.verbose", "4",
            "remote.fs_server.log", "true"
        );
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        System.setProperties(oldProps);
        super.tearDown();
    }

    private void traceCanDeleteOnDisconnect() {
        System.err.println(execEnv.toString() + ": " +
                (RemoteFileSystemTransport.canDeleteOnDisconnect(execEnv) ? 
                "Deleting on disconnect via fs_server" : 
                "Alternative delete on disconnect implementation"));
    }

    @ForAllEnvironments
    public void testDeleteOnExit() throws Exception {
        System.setProperty("remote.fs_server.redirect.err", getStdErrFileName("1"));
        reconnect(200, true);
        String dir = null;
        boolean success = false;
        try {
            dir = mkTempAndRefreshParent(true);
            RemoteFileObject dirFO = (RemoteFileObject) getFileObject(dir);
            
            String path1 = dir + "/file1.dat";
            String path2 = dir + "/file2.dat";
            runScript("echo xxx > " + path1 + "; echo xxx > " + path2);            
            ProcessUtils.ExitStatus status = ProcessUtils.execute(execEnv, "ls", path1, path2);
            assertTrue("Error creating temp files", status.isOK());
            
            traceCanDeleteOnDisconnect();
            dirFO.getFileSystem().deleteOnDisconnect(path1);
            dirFO.getFileSystem().deleteOnDisconnect(path2);
            sleep(250);
            System.setProperty("remote.fs_server.redirect.err", getStdErrFileName("2"));
            reconnect(200, true);
            assertRemoved(500, 60, path1, path2);
            success = true;
        } finally {
            removeRemoteDirIfNotNull(dir);
            if (success && !logsToremove.isEmpty()) {
                System.setProperty("remote.fs_server.redirect.err", "false");
                reconnect(200, true);
                System.err.println("Removing fs_server stderr files " + toString(logsToremove));
                final ProcessUtils.ExitStatus rc = ProcessUtils.execute(execEnv, "rm", logsToremove.toArray(new String[logsToremove.size()]));
                if (!rc.isOK()) {
                    System.err.println("Error removing files " + toString(logsToremove) + ": " + rc.getErrorString());
                }
                logsToremove.clear();
            }
        }
    }

    @Override
    protected void reconnect(int timeout, boolean resetFileSystem) throws Exception {
        sleep(timeout);
        RemoteFileSystemTransport.shutdown(execEnv);
        super.reconnect(timeout, resetFileSystem);
    }

    private CharSequence toString(List<String> l) {
        StringBuilder sb = new StringBuilder();
        for (String s : l) {
            sb.append(s).append(' ');
        }
        return sb;
    }

    private void assertRemoved(int timeout, int attempts, String...paths) {
        StringBuilder message = new StringBuilder("Files should be removed: ");
        boolean first = true;
        for (String path : paths) {            
            if (!first) {
                message.append(", ");
            }
            first = false;
            message.append(path);
        }
        assertExec(message.toString(), false, timeout, attempts, "ls", paths);
    }
    
    private void assertExec(String failureMessage, boolean expectSuccess, int timeout, int attempts, String cmd, String...args) {
        for (int i = 0; i < attempts; i++) {
            ProcessUtils.ExitStatus status = ProcessUtils.execute(execEnv, cmd, args);
            if (status.isOK() == expectSuccess) {
                return;
            }
            sleep(timeout);
        }        
        assertTrue(failureMessage, false);
    }

    public static Test suite() {
        return RemoteApiTest.createSuite(DeleteOnExitTestCase.class);
    }
}
