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
package org.netbeans.modules.turbo.keys;

import junit.framework.TestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileSystem;
import org.netbeans.modules.turbo.Turbo;

import java.io.File;

/**
 * Test DiskFileKey
 *
 * @author Petr Kuzel
 */
public class DiskFileKeyTest extends TestCase {

    private FileSystem fs;

    // called before every method
    protected void setUp() throws Exception {

        // prepare simple LFS

        LocalFileSystem fs = new LocalFileSystem();
        File tmp = new File(System.getProperty("java.io.tmpdir") + File.separator + "turbo-test");
        tmp.mkdir();
        tmp.deleteOnExit();
        File theFile = new File(tmp, "theFile");
        theFile.createNewFile();
        theFile.deleteOnExit();
        fs.setRootDirectory(tmp);

        this.fs = fs;
    }


    /** Test how keys handle FileObject identity problems. */
    public void testMemoryKeys() throws Exception{
        FileObject overlap = fs.getRoot().createFolder("nestedFS2");
        try {
            LocalFileSystem ofs = new LocalFileSystem();
            ofs.setRootDirectory(FileUtil.toFile(overlap));

            ofs.getRoot().createData("data.txt");

            FileObject f1 = fs.findResource("nestedFS2/data.txt");
            FileObject f2 = ofs.findResource("data.txt");

            DiskFileKey k1 = DiskFileKey.createKey(f1);
            DiskFileKey k2 = DiskFileKey.createKey(f2);

            assertTrue(k1.equals(k2));

        } finally {
            overlap.delete(overlap.lock());
        }
    }

    /**
     * Netbeans fileobjects have problems with identity.
     * Two fileobject representing same file are not guaranteed to be equivalent.
     * Known causes: MasterFS fileobject and fileobject from wrapped
     * filesystem (it can be spotted only by FS impl). Overlapping
     * LocalFilesystems.
     * <p>
     * It uncovered Memory.DiskFileKey.hashCode problem!
     */
    public void testDuplicatedFileObject() throws Exception {
        FileObject overlap = fs.getRoot().createFolder("nestedFS");
        try {
            LocalFileSystem ofs = new LocalFileSystem();
            ofs.setRootDirectory(FileUtil.toFile(overlap));

            ofs.getRoot().createData("data.txt");

            FileObject f1 = fs.findResource("nestedFS/data.txt");
            FileObject f2 = ofs.findResource("data.txt");
            assert f1 != f2;
            assert f1.equals(f2) == false;
            Object k1 = DiskFileKey.createKey(f1);
            Object k2 = DiskFileKey.createKey(f2);


            Turbo turbo = Turbo.getDefault();

            turbo.writeEntry(k1, "identity", "clash");
            Object v1 = turbo.readEntry(k1, "identity");
            Object v2 = turbo.readEntry(k2, "identity");

            assertTrue("clash".equals(v1));
            assertTrue("Unexpected value:" + v2, "clash".equals(v2));

            turbo.writeEntry(k2, "identity", "over!");

            v1 = turbo.readEntry(k1, "identity");
            v2 = turbo.readEntry(k2, "identity");

            assertTrue("over!".equals(v1));
            assertTrue("Unexpected value:" + v2, "over!".equals(v2));

        } finally {
            overlap.delete(overlap.lock());
        }
    }

}
