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

package org.netbeans.modules.cnd.remote.sync;

import java.io.File;
import java.io.PrintWriter;
import java.util.Collections;
import junit.framework.Test;
import org.netbeans.modules.cnd.remote.server.RemoteServerList;
import org.netbeans.modules.cnd.remote.server.RemoteServerRecord;
import org.netbeans.modules.cnd.remote.test.RemoteDevelopmentTest;
import org.netbeans.modules.cnd.spi.remote.RemoteSyncFactory;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Test for ScpSyncWorker
 */
public class FtpSyncWorkerTestCase extends AbstractSyncWorkerTestCase {

    public FtpSyncWorkerTestCase(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }

    @Override
    RemoteSyncFactory getSyncFactory() {
        return RemoteSyncFactory.fromID(FtpSyncFactory.ID);
    }

    @Override
    BaseSyncWorker createWorker(File src, ExecutionEnvironment execEnv, 
            PrintWriter out, PrintWriter err, FileObject privProjectStorageDir) {
        return new FtpSyncWorker(execEnv, out, err, privProjectStorageDir,
                Collections.singletonList(FSPath.toFSPath(FileUtil.toFileObject(FileUtil.normalizeFile(src)))),
                Collections.<FSPath>emptyList());
    }

    @Override
    protected String getTestNamePostfix() {
        return "ftp";
    }

    public static Test suite() {
        return new RemoteDevelopmentTest(FtpSyncWorkerTestCase.class);
    }
}
