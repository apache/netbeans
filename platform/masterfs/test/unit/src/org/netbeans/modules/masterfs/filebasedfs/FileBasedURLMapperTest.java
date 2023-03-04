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
package org.netbeans.modules.masterfs.filebasedfs;

import java.io.File;
import java.net.URI;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.filesystems.FileUtil;
import java.net.URL;
import org.openide.util.BaseUtilities;

/**
 * @author Radek Matous
 */
public class FileBasedURLMapperTest extends NbTestCase {

    public FileBasedURLMapperTest(String testName) {
        super(testName);
    }

    public void testGetURL64012() throws Exception {
        int type = URLMapper.NETWORK;        
        FileObject fo = FileUtil.toFileObject(getWorkDir());        
        assertNotNull(fo);
        
        URLMapper instance = new FileBasedURLMapper();        
        URL result = instance.getURL(fo, type);
        assertNull(result);//NOI18N
    }

    public void testGetURL155742() throws Exception {
        clearWorkDir();
        File f = new File(getWorkDir(), "dummy");
        assertTrue(f.mkdir());
        FileObject fo = FileUtil.toFileObject(f);
        assertNotNull(fo);
        f.delete();
        URLMapper instance = new FileBasedURLMapper();
        URL result = instance.getURL(fo, URLMapper.INTERNAL);
        assertTrue("Folder URL must always end with slash.", result.toExternalForm().endsWith("/"));
    }
    
    public void testSlashifyUNCPath() throws Exception {
        String unc = "\\\\192.168.0.201\\data\\services\\web\\com_resource\\";
        URI uri = FileBasedURLMapper.toURI(unc, true, '\\');
        final URI norm = BaseUtilities.normalizeURI(uri);

        assertTrue("Is normalized: " + uri + " == " + norm, uri.equals(norm));
        assertEquals("192.168.0.201", uri.getHost());
        assertEquals("/data/services/web/com_resource/", uri.getPath());
    }

    public void testWSLPath() throws Exception {
        String wsl = "\\\\wsl$\\Target\\";
        URI uri = FileBasedURLMapper.toURI(wsl, true, '\\');
        final URI converted = uri.toURL().toURI();
        final URI norm = BaseUtilities.normalizeURI(uri);

        assertTrue("same after converted to URL and back: " + uri + " == " + converted, uri.equals(converted));
        assertTrue("Is normalized: " + uri + " == " + norm, uri.equals(norm));
        assertNull(uri.getHost());
        assertEquals("//wsl$/Target/", uri.getPath());
    }

}
