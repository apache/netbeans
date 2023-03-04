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
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class LocalFileSystemAttrTest extends NbTestCase {
    private LFS testedFS;

    public LocalFileSystemAttrTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        testedFS = new LFS();
        testedFS.setRootDirectory(getWorkDir());
    }
    
    public void testRenameWhenTargetFileExists() throws Exception {
        FileObject fo = FileUtil.createData(testedFS.getRoot(), "A/B/C.java");
        fo.setAttribute("test", "value");
        File parent = FileUtil.toFile(fo.getParent());
        final File attrs = new File(parent, ".nbattrs");
        assertTrue("Exists " + attrs, attrs.exists());
        File attrsbak = new File(parent, ".nbattrs~");
        assertFalse("Backup file does not exists", attrsbak.exists());
        testedFS.hook = new FileSystem.AtomicAction() {
            @Override
            public void run() throws IOException {
                attrs.createNewFile();
            }
        };
        fo.setAttribute("test", "nova");
        assertEquals("nova", fo.getAttribute("test"));
    }    
    
    private static final class LFS extends LocalFileSystem {
        AtomicAction hook;
        
        @Override
        protected void rename(String oldName, String newName) throws IOException {
            AtomicAction h = hook;
            hook = null;
            if (h != null) {
                h.run();
            }
            super.rename(oldName, newName);
        }
        
    }
}
