/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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
import java.net.URL;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.masterfs.filebasedfs.FileBasedFileSystem;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;

/**
 * @author Radek Matous
 */
public class URLMapperTest extends NbTestCase {
    private static FileSystem mfs;
    public URLMapperTest(String name) {
        super(name);
        mfs = FileBasedFileSystem.getInstance();
    }

    public void testURLMapperCallingFromMetaInfLookup() {
        Lookup lkp = Lookups.metaInfServices(Thread.currentThread().getContextClassLoader());
        Object obj = lkp.lookup(Object.class);
        assertNotNull(obj);
        assertEquals(MyInstance2.class, obj.getClass());
    }
    
    public void testNormalize8_3Names() throws Exception {
        if (!Utilities.isWindows()) {
            return;
        }
        
        clearWorkDir();
        File f = new File(getWorkDir(), "my new temp folder with long name");
        f.mkdirs();
        
        File shrtF = new File(getWorkDir(), "MYNEWT~1");
        assertTrue("Short name exists", shrtF.isDirectory());
        
        final URL u = shrtF.toURI().toURL();
        final FileObject fo = URLMapper.findFileObject(u);
        File found = FileUtil.toFile(fo);
        
        assertEquals("The same file as original", f, found);
    }

    @ServiceProvider(service=Object.class)
    public static class MyInstance2 {
        public MyInstance2() {
            super();
            testURLMapper();
        }

        private static void testURLMapper() {            
            assertNotNull(mfs);
            FileObject[] children = mfs.getRoot().getChildren();
            for (int i = 0; i < children.length; i++) {
                java.io.File file = FileUtil.toFile(children[i]);
                assertNotNull(file);
                assertNotNull(FileUtil.toFileObject(file));
            }
        }

    }

}
