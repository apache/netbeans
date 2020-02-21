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
