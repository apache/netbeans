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

package org.netbeans.spi.settings;

import java.io.*;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.LocalFileSystem;
import org.openide.util.Lookup;

/**
 *
 * @author  Jan Pokorsky
 */
public class ConvertorTest extends NbTestCase {

    FileSystem fs;
    FileObject contextFO;

    /** Creates a new instance of EnvTest */
    public ConvertorTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        File work = getWorkDir();
        LocalFileSystem lfs = new LocalFileSystem();
        lfs.setRootDirectory(work);
        contextFO = lfs.getRoot().createData("context", "settings");
        fs = lfs;
    }
    
    public void testFindWriterContext() throws Exception {
        Reader r = new java.io.InputStreamReader(contextFO.getInputStream());
        Reader cr = org.netbeans.modules.settings.ContextProvider.createReaderContextProvider(r, contextFO);
        Lookup l = Convertor.findContext(cr);
        assertNotNull(l);
        FileObject src = (FileObject) l.lookup(FileObject.class);
        assertNotNull(src);
        assertEquals(contextFO.getPath(), src.getPath());
    }
    
    public void testFindReaderContext() throws Exception {
        org.openide.filesystems.FileLock lock = contextFO.lock();
        try {
            Writer w = new java.io.OutputStreamWriter(contextFO.getOutputStream(lock));
            Writer cw = org.netbeans.modules.settings.ContextProvider.createWriterContextProvider(w, contextFO);
            Lookup l = Convertor.findContext(cw);
            assertNotNull(l);
            FileObject src = (FileObject) l.lookup(FileObject.class);
            assertNotNull(src);
            assertEquals(contextFO.getPath(), src.getPath());
        } finally {
            lock.releaseLock();
        }
    }
    
}
