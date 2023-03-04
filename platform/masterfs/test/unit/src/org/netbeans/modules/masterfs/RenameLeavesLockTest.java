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

package org.netbeans.modules.masterfs;

import java.io.File;
import java.util.Arrays;
import java.util.logging.Level;
import org.openide.filesystems.FileObject;
import java.util.logging.Logger;
import org.netbeans.junit.*;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileUtil;

/** Simulating issue 109462.
 */
public class RenameLeavesLockTest extends NbTestCase {
    Logger LOG;
    
    public RenameLeavesLockTest(String name) {
        super(name);
    }

    protected Level logLevel() {
        return Level.WARNING;
    }
    
    

    protected void setUp() throws Exception {
        clearWorkDir();
        
        LOG = Logger.getLogger("test." + getName());
    }
    public void testRenameBehaviour() throws Exception {
        File dir = new File(getWorkDir(), "dir");
        dir.mkdirs();
        File fJava = new File(dir, "F.java");
        fJava.createNewFile();

        //LocalFileSystem lfs = new LocalFileSystem();
        //lfs.setRootDirectory(getWorkDir());
        FileObject root = FileUtil.toFileObject(getWorkDir());
        //FileObject root = lfs.getRoot();
        assertNotNull("root found", root);
        
        FileObject f = root.getFileObject("dir/F.java");
        assertNotNull("file found", f);
        {
            String[] all = dir.list();
            assertEquals("One: " + Arrays.asList(all), 1, all.length);
            assertEquals("F.java", all[0]);
        }
        FileLock lock = f.lock();
        assertTrue(f.isLocked());
        f.rename(lock, "Jarda", "java");
        f.rename(lock, "F", "java");
        f.rename(lock, "Jarda", "java");
        assertTrue(f.isLocked());
        lock.releaseLock();
        assertFalse(f.isLocked());
        

        //Issue 109462, this is failing:
        {
            String[] all = dir.list();
            assertEquals("One: " + Arrays.asList(all), 1, all.length);
            assertEquals("Jarda.java", all[0]);
        }
    }
    
}
