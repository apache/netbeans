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

package org.netbeans.modules.mercurial;

import java.util.Map;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author tomas
 */
public class StatusTest extends AbstractHgTestCase {

    public StatusTest(String arg0) {
        super(arg0);
    }

    @Override
    protected void setUp() throws Exception {
        System.setProperty("netbeans.user", new File(getWorkDir().getParentFile(), "userdir").getAbsolutePath());
        super.setUp();
        
        // create
        FileObject fo = FileUtil.toFileObject(getWorkTreeDir());
        
    }

    public void testStatusForRenamedFolder_136448() throws HgException, IOException {
        File folder = createFolder("folder");
        File file1 = createFile(folder, "file1");
        File file2 = createFile(folder, "file2");
        File file3 = createFile(folder, "file3");
        
        commit(folder);
        getCache().refresh(folder); // force refresh
        
        // assert status given from cli
        assertStatus(folder, FileInformation.STATUS_VERSIONED_UPTODATE);
        Map<File, FileInformation> m = HgCommand.getStatus(getWorkTreeDir(), Collections.singletonList(folder), null, null);
        assertEquals(0, m.keySet().size());
                
        // hg move the folder
        File folderenamed = new File(getWorkTreeDir(), "folderenamed");
        HgCommand.doRename(getWorkTreeDir(), folder, folderenamed, null);

        m = HgCommand.getStatus(getWorkTreeDir(), Collections.singletonList(folder), null, null);
        assertEquals(3, m.keySet().size());
        for (File file : m.keySet()) {
            assertStatus(file, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);    
        }        
        m = HgCommand.getStatus(getWorkTreeDir(), Collections.singletonList(folderenamed), null, null);
        assertEquals(3, m.keySet().size());        
        for (File file : m.keySet()) {
            assertStatus(file, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);    
        }                
    }

    public void testStatusCopied () throws HgException, IOException {
        File folder = createFolder("folder");
        File file1 = createFile(folder, "file1");
        File file2 = new File(folder, "file2");

        commit(folder);
        write(file1, "change");
        getCache().refresh(folder); // force refresh
        HgCommand.doCopy(getWorkTreeDir(), file1, file2, false, NULL_LOGGER);
        
        // assert status given from cli
        assertStatus(file1, FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY);
        Map<File, FileInformation> m = HgCommand.getStatus(getWorkTreeDir(), Collections.singletonList(folder), null, null);
        assertEquals(2, m.keySet().size());
        assertEquals(FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY, m.get(file1).getStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, m.get(file2).getStatus());
        assertTrue(m.get(file2).getStatus(null).isCopied());

        // assert status given from cli
        m = HgCommand.getStatus(getWorkTreeDir(), Collections.singletonList(file1), null, null);
        assertEquals(1, m.keySet().size());
        assertEquals(FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY, m.get(file1).getStatus());

        // assert status given from cli
        m = HgCommand.getStatus(getWorkTreeDir(), Collections.singletonList(file2), null, null);
        assertEquals(1, m.keySet().size());
        assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, m.get(file2).getStatus());
        assertTrue(m.get(file2).getStatus(null).isCopied());
    }
    
}
