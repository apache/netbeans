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

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 *
 * @author Radek Matous
 */
public class MemoryFSTestHid extends TestBaseHid {

    /** Creates a new instance of MemoryFSTestHid */
    public MemoryFSTestHid(String testName) {
        super(testName);
    }

    protected String[] getResources(String testName) {
        return new String[]{};
    }


    public void test58331() throws Exception {
        FileObject p = this.testedFS.getRoot();
        FileObject fo = p.createData("test58331");//NOI18N
        assertEquals(fo.getParent(), p);
        String n = fo.getName();
        fo.delete();
        fo.refresh();
        fo.isFolder(); 
        p.createData(n);
    }

    public void testRootAttributes () throws Exception {
        FileObject file = FileUtil.createData(this.testedFS.getRoot(), "/folder/file");
        assertNotNull(file);
        FileObject root = this.testedFS.getRoot();
        assertNotNull(root);
        file.setAttribute("name", "value");
        assertEquals(file.getAttribute("name"), "value");
        root.setAttribute("rootName", "rootValue");
        assertEquals(root.getAttribute("rootName"), "rootValue");        
    }

    public void testURLs() throws Exception {
        FileObject file = FileUtil.createData(testedFS.getRoot(), "/folder/file");
        OutputStream os = file.getOutputStream();
        os.write("hello".getBytes());
        os.close();
        file.setAttribute("mimeType", "text/x-hello");
        URL u = file.toURL();
        assertEquals("/folder/file", u.getPath());
        URLConnection conn = u.openConnection();
        conn.connect();
        assertEquals(5, conn.getContentLength());
        assertEquals(file.lastModified().getTime(), conn.getLastModified());
        assertEquals("text/x-hello", conn.getContentType());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        FileUtil.copy(conn.getInputStream(), baos);
        assertEquals("hello", baos.toString());
        assertEquals(file, URLMapper.findFileObject(u));
        assertEquals(null, URLMapper.findURL(file, URLMapper.EXTERNAL));
        assertEquals(null, URLMapper.findURL(file, URLMapper.NETWORK));
        assertEquals(u, new URL(file.getParent().toURL(), file.getNameExt()));
        assertEquals(testedFS.getRoot(), URLMapper.findFileObject(testedFS.getRoot().toURI().toURL()));
        assertEquals(file.getParent(), URLMapper.findFileObject(file.getParent().toURI().toURL()));
    }

}
