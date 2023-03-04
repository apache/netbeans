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

package org.netbeans.modules.refactoring.spi;

import java.io.IOException;
import java.io.OutputStream;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.refactoring.spi.BackupFacility2.Handle;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jan Becicka
 */
public class BackupFacilityTest extends NbTestCase {

    FileObject f;
    FileObject f2;
    private FileObject folder;
    
    public BackupFacilityTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        FileObject workdir = FileUtil.toFileObject(getWorkDir());
        f = FileUtil.createData(workdir, "test");
        folder = FileUtil.createFolder(workdir, "test2");
        f2 = FileUtil.createData(folder, "test");
        OutputStream outputStream = f.getOutputStream();
        outputStream.write("test".getBytes());
        outputStream.close();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void test93390() throws IOException {
        Handle transactionId = BackupFacility2.getDefault().backup(f2);
        f2.delete();
        folder.delete();
        assertFalse(f2.isValid());
        transactionId.restore();
        FileObject newone = FileUtil.toFileObject(FileUtil.toFile(f2));
        assertTrue(newone.isValid());
    }

    public void testBackupRestore() throws Exception {
        Handle transactionId = BackupFacility2.getDefault().backup(f);
        f.delete();
        assertFalse(f.isValid());
        transactionId.restore();
        FileObject newone = FileUtil.toFileObject(FileUtil.toFile(f));
        assertTrue(newone.isValid());
    }

    public void testClear() throws IOException {
        Handle transactionId = BackupFacility2.getDefault().backup(f);
        f.delete();
        assertFalse(f.isValid());
        BackupFacility2.getDefault().clear();
        try {
            transactionId.restore();
        } catch (IllegalArgumentException iae) {
            return;
        }
        fail("clear failed");
    }

    public void testGetDefault() {
        assertNotNull(BackupFacility2.getDefault());
    }

}
