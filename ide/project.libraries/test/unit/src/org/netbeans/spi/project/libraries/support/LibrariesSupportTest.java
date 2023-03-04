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

package org.netbeans.spi.project.libraries.support;

import java.io.File;
import java.net.URI;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

public class LibrariesSupportTest extends NbTestCase {

    public LibrariesSupportTest(String testName) {
        super(testName);
    }            

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    /**
     * Test of convertFilePathToURL method, of class LibrariesSupport.
     */
    public void testConvertFilePathToURI() throws Exception {
        String path = getWorkDirPath()+"/aa/bb/c c.ext".replace('/', File.separatorChar);
        URI u = LibrariesSupport.convertFilePathToURI(path);
        assertEquals(Utilities.toURI(FileUtil.normalizeFile(new File(path))), u);
        path = "../zz/re l.ext".replace('/', File.separatorChar);
        u = LibrariesSupport.convertFilePathToURI(path);
        assertEquals("../zz/re%20l.ext", u.toString());
    }

    /**
     * Test of convertURLToFilePath method, of class LibrariesSupport.
     */
    public void testConvertURIToFilePath() throws Exception{
        URI u = Utilities.toURI(getWorkDir());
        String path = LibrariesSupport.convertURIToFilePath(u);
        assertEquals(getWorkDir().getPath(), path);
        u = new URI(null, null, "../zz/re l.ext", null);
        path = LibrariesSupport.convertURIToFilePath(u);
        assertEquals("../zz/re l.ext".replace('/', File.separatorChar), path);
    }

    /**
     * Test of resolveLibraryEntryFileObject method, of class LibrariesSupport.
     */
    public void testResolveLibraryEntryFileObject() throws Exception {
        File f = new File(this.getWorkDir(), "knihovna.properties");
        File f2 = new File(this.getWorkDir(), "bertie.jar");
        f.createNewFile();
        f2.createNewFile();
        FileObject fo = LibrariesSupport.resolveLibraryEntryFileObject(
                Utilities.toURI(f).toURL(),
                new URI(null, null, "bertie.jar", null));
        assertEquals(f2.getPath(), FileUtil.toFile(fo).getPath());
        fo = LibrariesSupport.resolveLibraryEntryFileObject(
                null, 
                Utilities.toURI(f2));
        assertEquals(f2.getPath(), FileUtil.toFile(fo).getPath());
    }

    public void testResolveLibraryEntryURI() throws Exception {
        File f = new File(this.getWorkDir(), "knihovna.properties");
        File f2 = new File(this.getWorkDir(), "ber tie.jar");
        f.createNewFile();
        f2.createNewFile();
        URI u = LibrariesSupport.resolveLibraryEntryURI(
                Utilities.toURI(f).toURL(),
                new URI(null, null, "ber tie.jar", null));
        assertEquals(Utilities.toURI(new File(f.getParentFile(), f2.getName())), u);
        u = LibrariesSupport.resolveLibraryEntryURI(
                Utilities.toURI(f).toURL(),
                Utilities.toURI(f2));
        assertEquals(Utilities.toURI(f2), u);
        u = LibrariesSupport.resolveLibraryEntryURI(
                Utilities.toURI(f).toURL(),
                new URI(null, null, "ber tie.jar!/main/ja va", null));
        assertEquals(new URI("jar:"+ (Utilities.toURI(new File(f.getParentFile(), f2.getName())).toString()) + "!/main/ja%20va"), u);
        u = LibrariesSupport.resolveLibraryEntryURI(
                Utilities.toURI(f).toURL(),
                new URI(null, null, "../"+getWorkDir().getName()+"/ber tie.jar!/main/ja va", null));
        assertEquals(new URI("jar:"+ (Utilities.toURI(new File(f.getParentFile(), f2.getName())).toString()) + "!/main/ja%20va"), u);
        u = LibrariesSupport.resolveLibraryEntryURI(
                Utilities.toURI(f).toURL(),
                new URI(null, null, "../a folder/", null));
        assertEquals(new URI(Utilities.toURI(new File(getWorkDir().getParentFile(), "a folder")).toString() + "/"), u);
        // UNC paths
        URI uncBaseURI = URI.create("file://computerName/sharedFolder/a/b/c/d.properties");
        URI uncEntryURI = URI.create("e/e.jar");
        URI expectedURI = URI.create("file://computerName/sharedFolder/a/b/c/e/e.jar");
        URI resolvedURI = LibrariesSupport.resolveLibraryEntryURI(uncBaseURI.toURL(), uncEntryURI);
        assertEquals("UNC entry wrongly resolved.", expectedURI, resolvedURI);
        uncEntryURI = new URI(null, null, "e/e.jar!/f f/f", null);
        expectedURI = URI.create("jar:file://computerName/sharedFolder/a/b/c/e/e.jar!/f%20f/f");
        resolvedURI = LibrariesSupport.resolveLibraryEntryURI(uncBaseURI.toURL(), uncEntryURI);
        assertEquals("UNC jar entry wrongly resolved.", expectedURI, resolvedURI);
        uncEntryURI = new URI(null, null, "e/e.jar!/", null);
        expectedURI = URI.create("jar:file://computerName/sharedFolder/a/b/c/e/e.jar!/");
        resolvedURI = LibrariesSupport.resolveLibraryEntryURI(uncBaseURI.toURL(), uncEntryURI);
        assertEquals("UNC jar entry wrongly resolved.", expectedURI, resolvedURI);   
    }

}
