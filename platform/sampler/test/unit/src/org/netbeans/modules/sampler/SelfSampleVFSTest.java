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

package org.netbeans.modules.sampler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class SelfSampleVFSTest extends NbTestCase {
    private FileSystem fs;

    public SelfSampleVFSTest(String s) {
        super(s);
    }

    /** for subclass(es) in uihandler module 
     * @param array of names that shall be visible on the VFS
     * @param files locations of real files that represent content of those names
     */
    protected FileSystem createVFS(String[] names, File[] files) {
        return new SelfSampleVFS(names, files);
    }
    
    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        
        File a = new File(getWorkDir(), "A.txt");
        File b = new File(getWorkDir(), "B.txt");
        
        write(a, "Ahoj");
        write(b, "Kuk");
        
        fs = createVFS(new String[] { "x.pdf", "y.ps" }, new File[] { a, b });
    }
    
    public void testCanList() {
        FileObject[] arr = fs.getRoot().getChildren();
        assertEquals("Two", 2, arr.length);
        assertEquals("x.pdf", arr[0].getNameExt());
        assertEquals("y.ps", arr[1].getNameExt());
    }

    public void testCanReadContent() throws Exception {
        FileObject fo = fs.findResource("x.pdf");
        assertNotNull("File Object found", fo);
        assertEquals("The right content for x.pdf", "Ahoj", fo.asText());
    }

    public void testGetAttribute() throws Exception {
        FileObject fo = fs.findResource("x.pdf");
        assertNull("No attribute value", fo.getAttribute("doesnotexist"));
    }

    
    private static void write(File f, String content) throws IOException {
        FileOutputStream os = new FileOutputStream(f);
        try {
            os.write(content.getBytes());
        } finally {
            os.close();
        }
    }
}
