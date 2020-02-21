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

package org.netbeans.modules.cnd.remote.mapper;

import junit.framework.Test;
import org.netbeans.modules.cnd.api.remote.HostInfoProvider;
import org.netbeans.modules.cnd.api.remote.PathMap;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.remote.test.RemoteDevelopmentTest;
import org.netbeans.modules.cnd.remote.server.RemoteServerRecord;
import org.netbeans.modules.cnd.remote.test.RemoteTestBase;
import org.netbeans.modules.cnd.remote.sync.RfsSyncFactory;
import org.netbeans.modules.cnd.remote.sync.SharedSyncFactory;
import org.netbeans.modules.cnd.spi.remote.RemoteSyncFactory;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.openide.util.Utilities;

/**
 */
public class IncludeMappingsTestCase extends RemoteTestBase {

    public IncludeMappingsTestCase(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }

    private void doTestIncludeMappings(String syncFactoryID, boolean customizable) throws Exception {
        ExecutionEnvironment execEnv = getTestExecutionEnvironment();

        RemoteServerRecord record = (RemoteServerRecord) ServerList.get(getTestExecutionEnvironment());        
        RemoteSyncFactory syncFactory = RemoteSyncFactory.fromID(syncFactoryID);
        assertNotNull("Null RemoteSyncFactory by ID " + syncFactoryID, syncFactory);
        record.setSyncFactory(syncFactory);
        assertEquals(customizable, syncFactory.isPathMappingCustomizable());

        PathMap mapper = HostInfoProvider.getMapper(execEnv);

        String remoteSyncRoot = RemotePathMap.getRemoteSyncRoot(execEnv);
        assertNotNull("remote sync root is null", remoteSyncRoot);
        String localSyncRoot = RemotePathMap.getLocalSyncRoot(execEnv);
        assertNotNull("local sync root is null", localSyncRoot);

        String remotePath;
        String localPath;

//        remotePath = "/usr/include/stdio.h";
//        localPath = mapper.getLocalPath(remotePath, true).replace('\\', '/');
//        assertNotNull("Mapper returned null for " + remotePath, localPath);
//        assertTrue("Local path for " + remotePath + " should start with " + localSyncRoot + ", but it is " + localPath,
//                localPath.startsWith(localSyncRoot));

        if (customizable) {
            String localRoot = "/export/home";
            String remoteRoot = "/net/myhost/export/home";
            RemotePathMap.getPathMap(execEnv).addMapping(localRoot, remoteRoot);
            String path = "/username/temp.tmp";
            String referenceLocalPath = localRoot + path;
            remotePath = remoteRoot + path;
            localPath = mapper.getLocalPath(remotePath).replace('\\', '/');
            System.err.printf("Mapped %s:%s to %s\n", execEnv, remotePath, localPath);
            assertNotNull("Mapper returned null for " + remotePath, localPath);
            assertEquals("Local path ", referenceLocalPath, localPath);
        } else {
            String referenceLocalPath;
            String convertedReferenceLocalPath;
            if (Utilities.isWindows()) {
                referenceLocalPath = "C:/Documents and Settings/User/temp.tmp";
                convertedReferenceLocalPath = "/C/Documents and Settings/User/temp.tmp";
            } else {
                referenceLocalPath = "/home/username/temp.tmp";
                convertedReferenceLocalPath = referenceLocalPath;
            }
            remotePath = remoteSyncRoot + convertedReferenceLocalPath;
            localPath = mapper.getLocalPath(remotePath).replace('\\', '/');
            System.err.printf("Mapped %s:%s to %s\n", execEnv, remotePath, localPath);
            assertNotNull("Mapper returned null for " + remotePath, localPath);
            assertEquals("Local path ", referenceLocalPath, localPath);
        }
    }

    @ForAllEnvironments
    public void testFixedIncludeMappings() throws Exception {
        doTestIncludeMappings(RfsSyncFactory.ID, false);
    }

    @ForAllEnvironments
    public void testSharedIncludeMappings() throws Exception {
        doTestIncludeMappings(SharedSyncFactory.ID, true);
    }

    public static Test suite() {
        return new RemoteDevelopmentTest(IncludeMappingsTestCase.class);
    }

}
