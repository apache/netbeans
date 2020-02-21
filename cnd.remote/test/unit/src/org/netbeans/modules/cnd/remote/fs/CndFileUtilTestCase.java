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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.remote.fs;

import java.io.File;
import java.io.OutputStreamWriter;
import junit.framework.Test;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.remote.test.RemoteDevelopmentTest;
import org.netbeans.modules.cnd.remote.test.RemoteTestBase;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.remote.spi.FileSystemCacheProvider;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;

/**
 *
 */
public class CndFileUtilTestCase extends RemoteTestBase {

    public CndFileUtilTestCase(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ExecutionEnvironment execEnv = getTestExecutionEnvironment();
        ServerList.addServer(execEnv, execEnv.getDisplayName(), null, true, false);
        ConnectionManager.getInstance().connectTo(execEnv);
    }

    @ForAllEnvironments
    public void testExists() throws Exception {
        ExecutionEnvironment execEnv = getTestExecutionEnvironment();
        FileSystem fs = FileSystemProvider.getFileSystem(execEnv);
        String remoteTempDir = null;
        try {
            remoteTempDir = mkTempAndRefreshParent(true);
            FileObject remoteProjectDirBase = getFileObject(remoteTempDir);            
            FileObject file_1 = remoteProjectDirBase.createData("file_1");
            boolean exists = CndFileUtils.exists(fs, file_1.getPath());
            assertTrue(exists);
            file_1.delete();
            final String path_1 = file_1.getPath();
            exists = CndFileUtils.exists(fs, path_1);
            CndUtils.assertTrueInConsole(exists, "CndUtils should report that the file " + path_1 + " still exists");
            CndFileUtils.clearFileExistenceCache();
            exists = CndFileUtils.exists(fs, file_1.getPath());
            assertFalse(exists);
        } finally {
            if (remoteTempDir != null) {
                CommonTasksSupport.rmDir(execEnv, remoteTempDir, true, new OutputStreamWriter(System.err));
            }
        }        
    }

    public static Test suite() {
        return new RemoteDevelopmentTest(CndFileUtilTestCase.class);
    }

}
