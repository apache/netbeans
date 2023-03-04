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
package org.netbeans.modules.localhistory;

import java.io.File;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 * @author tomas
 */
public class IsManagedTest extends NbTestCase {

    public IsManagedTest(String name) {
        super(name);
    }
    
    public void testIsManaged() {
        assertTrue(LocalHistory.getInstance().isManaged(VCSFileProxy.createFileProxy(new File("/some/path/file.txt"))));
        assertTrue(LocalHistory.getInstance().isManaged(VCSFileProxy.createFileProxy(new File("some/path/file.txt"))));
        assertTrue(LocalHistory.getInstance().isManaged(VCSFileProxy.createFileProxy(new File("path/file.txt"))));
        assertTrue(LocalHistory.getInstance().isManaged(VCSFileProxy.createFileProxy(new File("file.txt"))));
        assertTrue(LocalHistory.getInstance().isManaged(VCSFileProxy.createFileProxy(new File("file"))));
        
        assertTrue(LocalHistory.getInstance().isManaged(VCSFileProxy.createFileProxy(new File("file.nblh~"))));
    }
    
    public void testTmpFileSuffix() {
        assertFalse(LocalHistory.getInstance().isManaged(VCSFileProxy.createFileProxy(new File("/some/path/file.txt.0.nblh~"))));
        assertFalse(LocalHistory.getInstance().isManaged(VCSFileProxy.createFileProxy(new File("/some/path/file.txt.1.nblh~"))));
        assertFalse(LocalHistory.getInstance().isManaged(VCSFileProxy.createFileProxy(new File("/some/path/file.txt.10.nblh~"))));
        assertFalse(LocalHistory.getInstance().isManaged(VCSFileProxy.createFileProxy(new File("/some/path/file.txt.100.nblh~"))));
        assertFalse(LocalHistory.getInstance().isManaged(VCSFileProxy.createFileProxy(new File("/some/path/file.txt.0.nblh~"))));
        assertFalse(LocalHistory.getInstance().isManaged(VCSFileProxy.createFileProxy(new File("some/path/file.txt.0.nblh~"))));
        assertFalse(LocalHistory.getInstance().isManaged(VCSFileProxy.createFileProxy(new File("path/file.txt.0.nblh~"))));
        assertFalse(LocalHistory.getInstance().isManaged(VCSFileProxy.createFileProxy(new File("file.txt.0.nblh~"))));
        assertFalse(LocalHistory.getInstance().isManaged(VCSFileProxy.createFileProxy(new File("file.0.nblh~"))));
    }
    
    public void testMetadata() {
        assertMetadata(".svn");
        assertMetadata("_svn");
        assertMetadata("CVS");
        assertMetadata(".hg");
    }

    private void assertMetadata(String metadata) {
        assertFalse(LocalHistory.getInstance().isManaged(VCSFileProxy.createFileProxy(new File("/some/path/" + metadata + "/whatever/path"))));
        assertFalse(LocalHistory.getInstance().isManaged(VCSFileProxy.createFileProxy(new File("/some/path/" + metadata + "/whatever"))));
        assertFalse(LocalHistory.getInstance().isManaged(VCSFileProxy.createFileProxy(new File("/some/path/" + metadata))));
        assertFalse(LocalHistory.getInstance().isManaged(VCSFileProxy.createFileProxy(new File(metadata))));
    }
}
