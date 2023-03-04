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
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MultiFileSystem;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class ExLocalFileSystemTest extends NbTestCase {

    public ExLocalFileSystemTest(String n) {
        super(n);
    }

    
    public void testDeleteTheDirectory() throws Exception {
        doTest(true);
    }

    public void testJustCheckAttributes() throws Exception {
        doTest(false);
    }
    
    private void doTest(boolean delete) throws Exception {
        clearWorkDir();
        
        File nf = new File(getWorkDir(), "ud");
        File var = new File(nf, "var");
        System.setProperty("netbeans.user", nf.getPath());
        
        FileSystem fs = FileUtil.createMemoryFileSystem();
        FileObject data = FileUtil.createData(fs.getRoot(), "My/Fld/data.txt");
        data.setAttribute("hello", "world");
        
        File root = new File(getWorkDir(), "fs");
        root.mkdirs();
        ExLocalFileSystem fs2 = ExLocalFileSystem.getInstance(root);
        
        MultiFileSystem mfs = new MultiFileSystem(new FileSystem[] { fs2, fs });
        
        FileObject ndata = mfs.findResource(data.getPath());
        assertNotNull("data found", ndata);
        assertEquals("They have the right attribute", "world", ndata.getAttribute("hello"));
     
        ndata.setAttribute("hello", "kuk");
        assertEquals("New attribute value is there", "kuk", ndata.getAttribute("hello"));

        ndata.setAttribute("hello", "buk");
        assertEquals("Newer attribute value is there", "buk", ndata.getAttribute("hello"));
        
        if (delete && var.exists() && !var.delete()) {
            for (File file : var.listFiles()) {
                file.delete();
            }
            assertTrue("Can delete " + var, var.delete());
        }

        ndata.setAttribute("hello", "muk");
        assertEquals("Newest attribute value is there", "muk", ndata.getAttribute("hello"));
    }
    
}