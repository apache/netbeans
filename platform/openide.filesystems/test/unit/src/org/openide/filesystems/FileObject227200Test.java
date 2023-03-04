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
package org.openide.filesystems;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author jhavlin
 */
public class FileObject227200Test extends NbTestCase {

    private LocalFileSystem lfs;
    private static final Logger LOG
            = Logger.getLogger(FileObject227200Test.class.getName());

    public FileObject227200Test(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        TestUtilHid.destroyLocalFileSystem(getName());
        lfs = (LocalFileSystem) TestUtilHid.createLocalFileSystem(getName(),
                new String[]{getName()});
    }

    @Override
    protected void tearDown() throws Exception {
        for (FileObject fo : lfs.getRoot().getChildren()) {
            fo.delete();
        }
    }

    public void test227200() throws FileStateInvalidException, IOException,
            InterruptedException {

        final FileObject rootFO = lfs.getRoot();
        final File rootF = FileUtil.toFile(rootFO);
        System.out.println(rootF.getAbsolutePath());

        assertNotNull("Root File shouldn't be null", rootF);
        assertNotNull("Root FileObject shouldn't be null", rootFO);

        // Create a folder (using java.io.File API)
        final String folderName = "childFolder";
        final File subDir = new File(rootF, folderName);
        boolean creationResult = subDir.mkdir();
        assertTrue(creationResult);

        rootFO.refresh();
        // Create FileObject for existing folder.
        FileObject fo = rootFO.getFileObject(folderName);
        assertNotNull(fo);

        // Remove the backing folder.
        subDir.delete();

        // Get FileObject for the removed folder.
        fo = rootFO.getFileObject(folderName);
        assertNotNull(fo);

        if (!fo.isValid()) {
            LOG.log(Level.INFO, "FO {0} is invalid, it's OK.", folderName);
        } else if (!fo.isFolder()) {
            LOG.log(Level.INFO, "FO {0} is data, try refresh.", folderName);
            File createAgain = new File(rootF, fo.getNameExt());
            assertFalse(createAgain.exists());
            assertTrue(createAgain.mkdir());
            rootFO.refresh();
            FileObject refreshed = rootFO.getFileObject(fo.getNameExt());
            assertTrue("New folder should be valid.", refreshed.isValid());
            assertTrue("New folder's java.io.File should be a directory",
                    FileUtil.toFile(refreshed).isDirectory());
            assertTrue("New folder's FileObject should be a folder",
                    refreshed.isFolder());
        } else {
            LOG.log(Level.INFO, "FO {0} is valid folder, not covered by this"
                    + " test.", folderName);
        }
    }
}
