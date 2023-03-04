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
package org.netbeans.modules.versioning.core.api;

import java.io.File;
import java.io.IOException;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author tomas
 */
public class VCSFileProxyTest extends NbTestCase {

    public VCSFileProxyTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp(); 
        File userdir = new File(getWorkDir(), "userdir");
        userdir.mkdirs();
        System.setProperty("netbeans.user", userdir.getAbsolutePath());
        System.setProperty("org.netbeans.modules.masterfs.watcher.disable", "true");
    }
    
    public void testIsDirectoryFolderFO() throws IOException {
        VCSFileProxy proxy = getFolderProxy("directory" + System.currentTimeMillis());
        assertNotNull(proxy);
        assertTrue(proxy.isDirectory());
        assertFalse(proxy.isFile());
    }
    
    public void testIsDirectoryFileFO() throws IOException {
        VCSFileProxy proxy = getFileProxy("file"+ System.currentTimeMillis());
        assertNotNull(proxy);
        assertFalse(proxy.isDirectory());        
        assertTrue(proxy.isFile());        
    }
    
    public void testCachedIsDirectoryChangedForFolderFO() throws IOException {
        VCSFileProxy proxy = getFolderProxy("something"+ System.currentTimeMillis());
        assertNotNull(proxy);
        assertTrue(proxy.isDirectory());
        assertFalse(proxy.isFile());
        
        // delete folder ...
        File f = proxy.toFile();
        assertTrue(f.exists());
        assertTrue(f.isDirectory());
        FileObject fo = FileUtil.toFileObject(f);
        f.delete();
        assertFalse(f.exists());
        // ... and recreate as file
        f.createNewFile();
        assertTrue(f.exists());
        assertTrue(f.isFile());
        fo.refresh();
        
        assertFalse(proxy.isDirectory());
        assertTrue(proxy.isFile());
    }
    
    public void testCachedIsDirectoryChangedForFileFO() throws IOException {
        VCSFileProxy proxy = getFileProxy("something"+ System.currentTimeMillis());
        assertNotNull(proxy);
        assertFalse(proxy.isDirectory());
        assertTrue(proxy.isFile());
        
        // delete file ...
        File f = proxy.toFile();
        assertTrue(f.exists());
        assertTrue(f.isFile());
        FileObject fo = FileUtil.toFileObject(f);
        f.delete();
        assertFalse(f.exists());
        // ... and recreate as folder
        f.mkdirs();
        assertTrue(f.exists());
        assertTrue(f.isDirectory());
        fo.refresh();
        
        assertTrue(proxy.isDirectory());
        assertFalse(proxy.isFile());
    }

    private VCSFileProxy getFolderProxy(String name) throws IOException {
        File d = new File(getWorkDir(), name);
        d.delete();
        d.mkdirs();
        FileObject fo = FileUtil.toFileObject(d);
        assertNotNull(fo);
        VCSFileProxy proxy = VCSFileProxy.createFileProxy(fo);
        return proxy;
    }

    private VCSFileProxy getFileProxy(String name) throws IOException {
        File f = new File(getWorkDir(), name);
        f.delete();
        f.createNewFile();
        FileObject fo = FileUtil.toFileObject(f);
        assertNotNull(fo);
        VCSFileProxy proxy = VCSFileProxy.createFileProxy(fo);
        return proxy;
    }
}
