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
package org.netbeans.modules.subversion.remote;

import static junit.framework.Assert.assertNull;
import junit.framework.Test;
import org.netbeans.api.queries.VersioningQuery;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.test.ClassForAllEnvironments;
import static org.netbeans.modules.subversion.remote.RemoteVersioningTestBase.addTest;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.filesystems.FileObject;

/**
 *
 * 
 */
@ClassForAllEnvironments(section = "remote.svn")
public class InterceptorAttributeTest extends RemoteVersioningTestBase {

    public InterceptorAttributeTest(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }
    
    public static Test suite() {
        NbTestSuite suite = new NbTestSuite();
        addTest(suite, InterceptorAttributeTest.class, "getWrongAttribute");
        addTest(suite, InterceptorAttributeTest.class, "getRemoteLocationAttribute");
        addTest(suite, InterceptorAttributeTest.class, "getIsManaged");
        return(suite);
    }
    
    public void getWrongAttribute() throws Exception {
        if (skipTest()) {
            return;
        }
        VCSFileProxy file = VCSFileProxy.createFileProxy(wc, "attrfile");
        VCSFileProxySupport.createNew(file);
        FileObject fo = file.toFileObject();

        String str = (String) fo.getAttribute("peek-a-boo");
        assertNull(str);
    }

    public void getRemoteLocationAttribute() throws Exception {
        if (skipTest()) {
            return;
        }
        VCSFileProxy file = VCSFileProxy.createFileProxy(wc, "attrfile");
        VCSFileProxySupport.createNew(file);
        FileObject fo = file.toFileObject();

        String str = (String) fo.getAttribute(PROVIDED_EXTENSIONS_REMOTE_LOCATION);
        assertNotNull(str);
        assertEquals(repoUrl.toString(), str);
    }

    public void getIsManaged() throws Exception {
        if (skipTest()) {
            return;
        }
        // unversioned file
        VCSFileProxy file = VCSFileProxy.createFileProxy(dataRootDir, "unversionedfile");
        VCSFileProxySupport.createNew(file);

        boolean versioned = VersioningQuery.isManaged(file.toURI());
        assertFalse(versioned);

        // metadata folder
        file = VCSFileProxy.createFileProxy(wc, ".svn");

        versioned = VersioningQuery.isManaged(file.toURI());
        assertTrue(versioned);

        // metadata file
        file = VCSFileProxy.createFileProxy(file, "entries");

        versioned = VersioningQuery.isManaged(file.toURI());
        assertTrue(versioned);

        // versioned file
        file = VCSFileProxy.createFileProxy(wc, "attrfile");
        VCSFileProxySupport.createNew(file);

        versioned = VersioningQuery.isManaged(file.toURI());
        assertTrue(versioned);
    }
}
