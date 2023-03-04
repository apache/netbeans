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
package org.netbeans.modules.versioning;

import java.io.IOException;
import org.openide.filesystems.FileStateInvalidException;

import java.io.File;
import org.netbeans.api.queries.VersioningQuery;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.versioning.core.spi.testvcs.TestVCS;
import org.netbeans.modules.versioning.core.spi.testvcs.TestVCSInterceptor;
import org.openide.util.test.MockLookup;

/**
 * 
 * @author Tomas Stupka
 */
public class VersioningQueryTest extends NbTestCase {
    

    public VersioningQueryTest(String testName) {
        super(testName);
        MockLookup.setLayersAndInstances();
        System.setProperty("versioning.no.localhistory.interceptor", "true");
    }

    protected void setUp() throws Exception {
        MockLookup.setLayersAndInstances();
        File userdir = new File(getWorkDir() + "userdir");
        userdir.mkdirs();
        System.setProperty("netbeans.user", userdir.getAbsolutePath());
        super.setUp();
    }

    public void testIsManaged() throws FileStateInvalidException, IOException, Exception {
        File folder = createVersionedFolder();
        folder.mkdirs();
        File file = new File(folder, "somefile");
        file.createNewFile();
        
        assertTrue(VersioningQuery.isManaged(file.toURI()));
    }
    
    public void testIsNotManaged() throws FileStateInvalidException, IOException, Exception {
        File file = new File(getWorkDir(), "somefile");
        file.createNewFile();
        
        assertFalse(VersioningQuery.isManaged(file.toURI()));
    }
    
    public void testRemoteLocation() throws IOException {
        File folder = createVersionedFolder();
        folder.mkdirs();
        File file = new File(folder, "somefile");
        file.createNewFile();
        
        assertEquals(TestVCSInterceptor.REMOTE_LOCATION_PREFIX + file.getName(), VersioningQuery.getRemoteLocation(file.toURI()));
    }
    
    public void testNotVersionedRemoteLocation() throws IOException {
        File file = new File(getWorkDir(), "somefile");
        file.createNewFile();
        
        assertNull(VersioningQuery.getRemoteLocation(file.toURI()));
    }
    
    private File createVersionedFolder() throws IOException {
        File folder = new File(getWorkDir(), TestVCS.VERSIONED_FOLDER_SUFFIX);
        new File(folder, TestVCS.TEST_VCS_METADATA).mkdirs();
        return folder;
    }
    
}
