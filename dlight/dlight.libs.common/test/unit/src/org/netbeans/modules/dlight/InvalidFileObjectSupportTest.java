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

package org.netbeans.modules.dlight;

import org.netbeans.modules.dlight.libs.common.InvalidFileObjectSupport;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.URLStreamHandlerFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Lookup;

/**
 *
 */
public class InvalidFileObjectSupportTest {

    static {
        // otherwise we get java.net.MalformedURLException: unknown protocol
        // even if we register via @URLStreamHandlerRegistration annotation
        URL.setURLStreamHandlerFactory(Lookup.getDefault().lookup(URLStreamHandlerFactory.class));
    }    

    @Test
    public void testInvalidFileObjectURL() throws Exception {
        // see #270390 - StackOverflowError at java.io.UnixFileSystem.getBooleanAttributes
        FileSystem dummyFS = InvalidFileObjectSupport.getDummyFileSystem();
        FileObject invalidFO = InvalidFileObjectSupport.getInvalidFileObject(dummyFS, "/inexistent");
        final URL url = invalidFO.getURL();
        FileObject foundFO = URLMapper.findFileObject(url);
        //assertEquals("Invalid and found by URL ", invalidFO, foundFO);
    }

    @Test
    public void testInvalidFileObject() throws Exception {
        File file = File.createTempFile("qwe", "asd");
        FileObject origFo = null;
        String path = null;
        FileSystem fs = null;
        try {
            origFo = FileUtil.toFileObject(FileUtil.normalizeFile(file)); // FileUtil SIC!
            assertNotNull(origFo);
            path = origFo.getPath();
            fs = origFo.getFileSystem();
        } finally {
            file.delete();
        }
        FileObject invalidFo1 = InvalidFileObjectSupport.getInvalidFileObject(fs, path);
        URI uri1 = invalidFo1.toURI(); // just to check that there is no assertions
        URL url1 = invalidFo1.toURL(); // just to check that there is no assertions
        FileObject invalidFo2 = InvalidFileObjectSupport.getInvalidFileObject(fs, path);
        URI uri2 = invalidFo2.toURI(); // just to check that there is no assertions
        URL url2 = invalidFo2.toURL(); // just to check that there is no assertions
        assertTrue(invalidFo1 == invalidFo2);
        assertFalse(invalidFo1.isValid());
        assertEquals(origFo.getName(), invalidFo1.getName());
        assertEquals(origFo.getExt(), invalidFo1.getExt());
        String p1 = origFo.getPath();
        String p2 = invalidFo1.getPath();
        boolean eq = p1.equals(p2);
        assertEquals(origFo.getPath(), invalidFo1.getPath());
        assertEquals(origFo.getNameExt(), invalidFo1.getNameExt());
        assertEquals(origFo.getFileSystem(), invalidFo1.getFileSystem());
        FileObject invalidFo4 = InvalidFileObjectSupport.getInvalidFileObject(fs, "/tmp/foo.bar.cpp");
        assertNotNull(invalidFo4);
        assertEquals("getName()", "foo.bar", invalidFo4.getName());
        assertEquals("getExt()", "cpp", invalidFo4.getExt());
        FileObject invalidFo5 = InvalidFileObjectSupport.getInvalidFileObject(fs, "/tmp/qwe.asd/foo1.bar1.cc");
        assertNotNull(invalidFo5);
        assertEquals("getName()", "foo1.bar1", invalidFo5.getName());
        assertEquals("getExt()", "cc", invalidFo5.getExt());
    }
    
    @Test
    public void testInvalidFileObjectParent() throws Exception {
        File file = File.createTempFile("qwe", "asd");
        FileObject origFo = null;
        String path = null;
        FileSystem fs = null;
        try {
            origFo = FileUtil.toFileObject(file); // FileUtil SIC!
            assertNotNull(origFo);
            path = origFo.getPath();
            fs = origFo.getFileSystem();
        } finally {
            file.delete();
        }
        FileObject invalidFo = InvalidFileObjectSupport.getInvalidFileObject(fs, path);
        assertNotNull(invalidFo);
        FileObject parent = invalidFo.getParent();
        assertNotNull(parent);
        assertTrue(parent.isValid());
    }
    
}
