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

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import org.netbeans.junit.NbTestCase;
import org.openide.util.BaseUtilities;

/**
 * Test functionality of URLMapper.
 * @author Jesse Glick
 */
public class URLMapperTest extends NbTestCase {

    public URLMapperTest(String name) {
        super(name);
    }
    
    public void testPlusInName() throws IOException, PropertyVetoException {
        clearWorkDir();
        File plus = new File(getWorkDir(), "plus+plus");
        plus.createNewFile();
        LocalFileSystem lfs = new LocalFileSystem();
        lfs.setRootDirectory(getWorkDir());
        Repository.getDefault().addFileSystem(lfs);
        
        URL uPlus = BaseUtilities.toURI(plus).toURL();
        FileObject fo = URLMapper.findFileObject(uPlus);
        assertNotNull("File object found", fo);
        assertEquals("plus+plus", fo.getNameExt());
        
        URL back = URLMapper.findURL(fo, URLMapper.EXTERNAL);
        assertTrue("plus+plus is there", back.getPath().endsWith("plus+plus"));
    }

    /**
     * Check that jar: URLs are correctly mapped back into JarFileSystem resources.
     * @see "#39190"
     */
    public void testJarMapping() throws Exception {
        clearWorkDir();
        File workdir = getWorkDir();
        File jar = new File(workdir, "test.jar");
        String textPath = "x.txt";
        OutputStream os = new FileOutputStream(jar);
        try {
            JarOutputStream jos = new JarOutputStream(os);
            jos.setMethod(ZipEntry.STORED);
            JarEntry entry = new JarEntry(textPath);
            entry.setSize(0L);
            entry.setTime(System.currentTimeMillis());
            entry.setCrc(new CRC32().getValue());
            jos.putNextEntry(entry);
            jos.flush();
            jos.close();
        } finally {
            os.close();
        }
        assertTrue("JAR was created", jar.isFile());
        assertTrue("JAR is not empty", jar.length() > 0L);
        JarFileSystem jfs = new JarFileSystem();
        jfs.setJarFile(jar);
        Repository.getDefault().addFileSystem(jfs);
        FileObject rootFO = jfs.getRoot();
        FileObject textFO = jfs.findResource(textPath);
        assertNotNull("JAR contains a/b.txt", textFO);
        String rootS = "jar:" + BaseUtilities.toURI(jar) + "!/";
        URL rootU = new URL(rootS);
        URL textU = new URL(rootS + textPath);
        assertEquals("correct FO -> URL for root", rootU, URLMapper.findURL(rootFO, URLMapper.EXTERNAL));
        assertEquals("correct FO -> URL for " + textPath, textU, URLMapper.findURL(textFO, URLMapper.EXTERNAL));
        assertTrue("correct URL -> FO for root", Arrays.asList(URLMapper.findFileObjects(rootU)).contains(rootFO));
        assertTrue("correct URL -> FO for " + textPath, Arrays.asList(URLMapper.findFileObjects(textU)).contains(textFO));
    }

    public void testNbhostURLs() throws Exception { // #207690
        FileObject r = FileUtil.getConfigRoot();
        check(r);
        FileObject d = r.createFolder("some folder");
        check(d);
        FileObject f = d.createData("file #1");
        check(f);
        new XMLFileSystem().getRoot().toURI(); // anonymous, cannot round-trip
    }
    private static void check(FileObject f) throws Exception {
        URL u = f.toURL();
        assertEquals(u.toURI(), f.toURI());
        assertEquals(u.toString(), f, URLMapper.findFileObject(u));
    }
    
}
