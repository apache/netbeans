/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.web.common.api;

import java.net.URL;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.test.MockLookup;

public class ServerURLMappingTest extends NbTestCase {

    private Project testProject1;
    
    public ServerURLMappingTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockLookup.init();
        FileObject fo = FileUtil.toFileObject(getWorkDir());
        fo = fo.createFolder(""+System.currentTimeMillis());
        FileObject proj1 = FileUtil.createFolder(fo, "proj1");
        testProject1 = new WebServerTest.TestProject(proj1);

        MockLookup.setInstances(new WebServerTest.FileOwnerQueryImpl(testProject1, testProject1));
    }

    public void testFromServer() throws Exception {
        URL serverURL = testProject1.getProjectDirectory().toURL();
        FileObject result = ServerURLMapping.fromServer(testProject1, ServerURLMapping.CONTEXT_PROJECT_SOURCES, serverURL);
        assertEquals(serverURL.toURI().toASCIIString(), result.toURI().toASCIIString());

        URL serverURL2 = new URL(serverURL.toURI().toASCIIString() + "?something");
        result = ServerURLMapping.fromServer(testProject1, ServerURLMapping.CONTEXT_PROJECT_SOURCES, serverURL2);
        assertEquals(serverURL.toURI().toASCIIString(), result.toURI().toASCIIString());
    }
}
